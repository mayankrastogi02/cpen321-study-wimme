package com.cpen321.study_wimme

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FriendRequestsActivity : AppCompatActivity() {

    private val TAG = "FriendRequestsActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var backButton: ImageButton
    private var friendRequests = ArrayList<FriendRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_requests)

        recyclerView = findViewById(R.id.friendRequestsRecyclerView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            finish()
        }

        // Fetch friend requests from the server
        fetchFriendRequests()
    }

    private fun fetchFriendRequests() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/user/friendRequests?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    val friendRequestsArray = jsonResponse.getJSONArray("friendRequests")
                    val requests = ArrayList<FriendRequest>()

                    for (i in 0 until friendRequestsArray.length()) {
                        val requestObj = friendRequestsArray.getJSONObject(i)
                        val request = FriendRequest(
                            requestObj.getString("_id"),
                            requestObj.getString("userName"),
                            requestObj.getString("firstName"),
                            requestObj.getString("lastName")
                        )
                        requests.add(request)
                    }

                    withContext(Dispatchers.Main) {
                        friendRequests = requests
                        updateUI()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FriendRequestsActivity, "Error fetching friend requests", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching friend requests", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendRequestsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI() {
        if (friendRequests.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE

            recyclerView.adapter = FriendRequestAdapter(friendRequests) { request, accepted ->
                handleFriendRequest(request._id, accepted)
            }
        }
    }

    private fun handleFriendRequest(friendId: String, accepted: Boolean) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/user/friend")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val requestData = JSONObject().apply {
                    put("userId", userId)
                    put("friendId", friendId)
                    put("accepted", accepted)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                val message = if (accepted) "Friend request accepted" else "Friend request rejected"

                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@FriendRequestsActivity, message, Toast.LENGTH_SHORT).show()
                        // Remove the handled request from the list
                        friendRequests.removeAll { it._id == friendId }
                        updateUI()
                    } else {
                        Toast.makeText(this@FriendRequestsActivity, "Error processing friend request", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling friend request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendRequestsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class FriendRequest(
    val _id: String,
    val userName: String,
    val firstName: String,
    val lastName: String
)