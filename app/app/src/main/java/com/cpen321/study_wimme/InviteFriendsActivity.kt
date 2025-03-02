package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
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
import java.net.HttpURLConnection
import java.net.URL

class InviteFriendsActivity : AppCompatActivity() {

    private val TAG = "FriendSelectActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var continueButton: Button
    private val selectedFriends = mutableListOf<Friend>()

    // Session data passed from CreateSessionActivity
    private lateinit var sessionName: String
    private lateinit var sessionDescription: String
    private var sessionLatitude: Double = 0.0
    private var sessionLongitude: Double = 0.0
    private var sessionStartDate: Long = 0L
    private var sessionEndDate: Long = 0L
    private var isPublic: Boolean = false
    private lateinit var sessionSubject: String
    private lateinit var sessionFaculty: String
    private var sessionYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_friends)

        // Get session data from intent
        sessionName = intent.getStringExtra("SESSION_NAME") ?: ""
        sessionDescription = intent.getStringExtra("SESSION_DESCRIPTION") ?: ""
        sessionLatitude = intent.getDoubleExtra("SESSION_LATITUDE", 0.0)
        sessionLongitude = intent.getDoubleExtra("SESSION_LONGITUDE", 0.0)
        sessionStartDate = intent.getLongExtra("SESSION_START_DATE", 0L)
        sessionEndDate = intent.getLongExtra("SESSION_END_DATE", 0L)
        isPublic = intent.getBooleanExtra("SESSION_IS_PUBLIC", false)
        sessionSubject = intent.getStringExtra("SESSION_SUBJECT") ?: ""
        sessionFaculty = intent.getStringExtra("SESSION_FACULTY") ?: ""
        sessionYear = intent.getIntExtra("SESSION_YEAR", 1)

        recyclerView = findViewById(R.id.friendsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        continueButton = findViewById(R.id.continueButton)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Set up continue button
        continueButton.setOnClickListener {
            continueWithSelectedFriends()
        }

        // Fetch friends
        fetchFriends()
    }

    private fun fetchFriends() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.GONE

        val userId = LoginActivity.getCurrentUserId(this)

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/user/friends?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val friendsArray = jsonResponse.getJSONArray("friends")
                    val fetchedFriends = ArrayList<Friend>()

                    for (i in 0 until friendsArray.length()) {
                        val friendObj = friendsArray.getJSONObject(i)
                        val friend = Friend(
                            friendObj.getString("_id"),
                            friendObj.getString("userName"),
                            friendObj.getString("firstName"),
                            friendObj.getString("lastName"),
                            friendObj.optString("year", ""),
                            friendObj.optString("faculty", ""),
                            friendObj.optString("interests", "")
                        )
                        fetchedFriends.add(friend)
                    }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE

                        if (fetchedFriends.isEmpty()) {
                            emptyStateTextView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyStateTextView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            setupAdapter(fetchedFriends)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@InviteFriendsActivity,
                            "Failed to fetch friends",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching friends", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@InviteFriendsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun setupAdapter(friends: List<Friend>) {
        val adapter = InviteFriendsAdapter(friends) { friend, isSelected ->
            if (isSelected) {
                selectedFriends.add(friend)
            } else {
                selectedFriends.remove(friend)
            }
            updateContinueButtonText()
        }
        recyclerView.adapter = adapter
    }

    private fun updateContinueButtonText() {
        continueButton.text = if (selectedFriends.isEmpty()) {
            "Continue without invites"
        } else {
            "Invite ${selectedFriends.size} friend${if (selectedFriends.size > 1) "s" else ""}"
        }
    }

    private fun continueWithSelectedFriends() {
        // Return selected friend IDs to CreateSessionActivity
        val intent = Intent()
        val friendIds = selectedFriends.map { it.id }.toTypedArray()

        // Pass back session data and selected friends
        intent.putExtra("SELECTED_FRIEND_IDS", friendIds)
        intent.putExtra("SESSION_NAME", sessionName)
        intent.putExtra("SESSION_DESCRIPTION", sessionDescription)
        intent.putExtra("SESSION_LATITUDE", sessionLatitude)
        intent.putExtra("SESSION_LONGITUDE", sessionLongitude)
        intent.putExtra("SESSION_START_DATE", sessionStartDate)
        intent.putExtra("SESSION_END_DATE", sessionEndDate)
        intent.putExtra("SESSION_IS_PUBLIC", isPublic)
        intent.putExtra("SESSION_SUBJECT", sessionSubject)
        intent.putExtra("SESSION_FACULTY", sessionFaculty)
        intent.putExtra("SESSION_YEAR", sessionYear)

        setResult(RESULT_OK, intent)
        finish()
    }
}