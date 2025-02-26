package com.cpen321.study_wimme

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FriendsInfoActivity : AppCompatActivity() {

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