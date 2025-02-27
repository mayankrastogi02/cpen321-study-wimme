package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class UserSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
        
        // Setup back button if it exists
        findViewById<ImageButton>(R.id.backButton)?.setOnClickListener {
            finish()
        }
        
        // Set up logout button
        findViewById<Button>(R.id.logoutButton)?.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        // Call the static signOut method from LoginActivity companion object
        LoginActivity.signOut(this)
    }
}