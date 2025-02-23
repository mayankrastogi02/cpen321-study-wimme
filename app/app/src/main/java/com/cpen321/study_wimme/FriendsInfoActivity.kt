package com.cpen321.study_wimme

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FriendsInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_info)

        val friend = intent.getSerializableExtra("friend") as? Friend

        if (friend != null) {
            val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
            val firstNameTextView = findViewById<TextView>(R.id.firstNameTextView)
            val lastNameTextView = findViewById<TextView>(R.id.lastNameTextView)
            val yearTextView = findViewById<TextView>(R.id.yearTextView)
            val programTextView = findViewById<TextView>(R.id.programTextView)
            val interestsTextView = findViewById<TextView>(R.id.interestsTextView)

            val removeFriendButton = findViewById<Button>(R.id.removeFriendButton)

            usernameTextView.text = "Username: ${friend.username}"
            firstNameTextView.text = "First Name: ${friend.firstName}"
            lastNameTextView.text = "Last Name: ${friend.lastName}"
            yearTextView.text = "Year: ${friend.year}"
            programTextView.text = "Program: ${friend.program}"
            interestsTextView.text = "Interests: ${friend.interests}"

            removeFriendButton.setOnClickListener {
                // Implement your logic to remove the friend
                Toast.makeText(this, "Removing friend: ${friend.username}", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after removing the friend
            }
        } else {
            Toast.makeText(this, "Error: Could not retrieve friend details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}