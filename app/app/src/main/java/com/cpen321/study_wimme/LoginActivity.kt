package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val SERVER_URL = BuildConfig.SERVER_URL // Update with your server URL
        
        // Static method for signing out
        fun signOut(activity: AppCompatActivity) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestIdToken("${BuildConfig.WEB_CLIENT_ID}")
                .build()
                
            val googleSignInClient = GoogleSignIn.getClient(activity, gso)
            googleSignInClient.signOut().addOnCompleteListener(activity) {
                // Clear shared preferences
                val sharedPreferences = activity.getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                
                // Navigate to login screen
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                activity.finish()
                
                // Show toast
                Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Google sign in success: ${account.id}")
                handleSignInResult(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Request email only
            .requestProfile() // Request basic profile
            .requestId() // Request Google ID
            .requestIdToken("${BuildConfig.WEB_CLIENT_ID}")
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            // User is already signed in, verify with backend if needed
            verifyUserWithBackend(account)
        }

        // Set up the Google Sign-In Button
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        // Save user info to shared preferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("googleId", account.id)
        editor.putString("email", account.email)
        editor.putString("displayName", account.displayName ?: "User")
        editor.apply()

        // Save to backend
        saveGoogleIdToUser(account.id!!, account.email!!, account.displayName ?: "User")
    }

    private fun verifyUserWithBackend(account: GoogleSignInAccount) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Request to verify user exists in backend
                val url = URL("$SERVER_URL/api/auth/verify?googleId=${account.id}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // User exists in backend, proceed to main activity
                    withContext(Dispatchers.Main) {
                        startMainActivity()
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    // User doesn't exist in backend, save them
                    saveGoogleIdToUser(account.id!!, account.email!!, account.displayName ?: "User")
                } else {
                    // Handle other error cases
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error verifying user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveGoogleIdToUser(googleId: String, email: String, displayName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                Log.d(TAG, "Making backend request to save user data")
                // Prepare JSON data
                val jsonData = JSONObject()
                jsonData.put("googleId", googleId)
                jsonData.put("email", email)
                jsonData.put("displayName", displayName)

                // Make HTTP request to your server
                val url = URL("$SERVER_URL/api/auth/google")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                Log.d(TAG, "JSON data: $jsonData")

                // Send data
                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonData.toString())
                outputStream.flush()

                // Get response
                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    Log.d(TAG, "User data saved successfully")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startMainActivity()
                    }
                } else {
                    Log.e(TAG, "Error saving user data: $responseCode")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error saving user data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, SessionsListActivity::class.java)
        startActivity(intent)
        finish()
    }
}