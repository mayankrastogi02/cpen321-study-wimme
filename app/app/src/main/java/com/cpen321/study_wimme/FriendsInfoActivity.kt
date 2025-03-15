package com.cpen321.study_wimme

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FriendsInfoActivity : AppCompatActivity() {

    private val TAG = "FriendsInfoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_info)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val friend = intent.getSerializableExtra("friend") as? Friend

        if (friend != null) {
            val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
            val firstNameEditText = findViewById<TextInputEditText>(R.id.firstNameEditText)
            val lastNameEditText = findViewById<TextInputEditText>(R.id.lastNameEditText)
            val yearEditText = findViewById<TextInputEditText>(R.id.yearEditText)
            val programEditText = findViewById<TextInputEditText>(R.id.programEditText)
            val interestsEditText = findViewById<TextInputEditText>(R.id.interestsEditText)
            val removeFriendButton = findViewById<MaterialButton>(R.id.removeFriendButton)

            usernameEditText.setText(friend.username)
            firstNameEditText.setText(friend.firstName)
            lastNameEditText.setText(friend.lastName)
            yearEditText.setText(friend.year)
            programEditText.setText(friend.program)
            interestsEditText.setText(friend.interests)

            // Disable editing
            usernameEditText.isEnabled = false
            firstNameEditText.isEnabled = false
            lastNameEditText.isEnabled = false
            yearEditText.isEnabled = false
            programEditText.isEnabled = false
            interestsEditText.isEnabled = false

            removeFriendButton.setOnClickListener {
                showRemoveFriendConfirmation(friend)
            }
        } else {
            Toast.makeText(this, "Error: Could not retrieve friend details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showRemoveFriendConfirmation(friend: Friend) {
        AlertDialog.Builder(this)
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove ${friend.firstName} ${friend.lastName} from your friends?")
            .setPositiveButton("Remove") { _, _ ->
                removeFriend(friend.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeFriend(friendId: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/user/removeFriend")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("friendId", friendId)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@FriendsInfoActivity, "Friend removed successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@FriendsInfoActivity, "Failed to remove friend", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error removing friend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FriendsInfoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}