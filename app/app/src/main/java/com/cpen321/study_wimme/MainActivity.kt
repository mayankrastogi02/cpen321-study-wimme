package com.cpen321.study_wimme

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Thread.sleep(1000)
        installSplashScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = "android.permission.POST_NOTIFICATIONS"
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }
        getCurrentToken { token ->
            Log.d("FCM", "Retrieved token: $token")
        }
        startActivity(Intent(this, SessionsListActivity::class.java))
    }

    fun getCurrentToken(callback: (String?) -> Unit) {
        // Check local storage first
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val localToken = sharedPreferences.getString("fcm_token", null)

        if (localToken != null) {
            callback(localToken)
        } else {
            // Fetch the token from Firebase
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    // Save the token locally
                    sharedPreferences.edit().putString("fcm_token", token).apply()
                    callback(token)
                } else {
                    callback(null)
                }
            }
        }
    }
}