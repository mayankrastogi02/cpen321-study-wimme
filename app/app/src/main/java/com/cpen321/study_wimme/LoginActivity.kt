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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val SERVER_URL = BuildConfig.SERVER_URL
        
        // Static method for signing out
        fun signOut(activity: AppCompatActivity) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
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
                
                Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Static method to get current user's Google ID
        fun getCurrentUserGoogleId(activity: AppCompatActivity): String? {
            val sharedPreferences = activity.getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
            return sharedPreferences.getString("googleId", null)
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
            .requestEmail()
            .requestProfile()
            .requestId()
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
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

        // Check with backend if the user exists and if profile is created
        verifyUserWithBackend(account)
    }

    private fun verifyUserWithBackend(account: GoogleSignInAccount) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Request to verify user exists in backend
                val url = URL("$SERVER_URL/api/auth/verify?googleId=${account.id}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d(TAG, "Verify response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    
                    val jsonResponse = JSONObject(response.toString())
                    val profileCreated = jsonResponse.getBoolean("profileCreated")

                    // Save the MongoDB user ID to SharedPreferences
                    if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("_id")) {
                        val mongoUserId = jsonResponse.getJSONObject("data").getString("_id")
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        sharedPreferences.edit().putString("userId", mongoUserId).apply()
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (profileCreated) {
                            // Profile is created, proceed to main activity
                            startSessionsListActivity()
                        } else {
                            // Profile is not created, go to settings
                            startUserSettingsActivity()
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    // User doesn't exist in backend, create a new user
                    createNewUser(account)
                } else {
                    // Handle other error cases
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error verifying user", Toast.LENGTH_SHORT).show()
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

    private fun createNewUser(account: GoogleSignInAccount) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Creating new user in backend")
                // Prepare JSON data
                val jsonData = JSONObject().apply {
                    put("googleId", account.id)
                    put("email", account.email)
                    put("displayName", account.displayName ?: "User")
                }

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
                Log.d(TAG, "Create user response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    // New user created, go to settings to complete profile
                    withContext(Dispatchers.Main) {
                        startUserSettingsActivity()
                    }
                } else {
                    Log.e(TAG, "Error creating user: $responseCode")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error creating user", Toast.LENGTH_SHORT).show()
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

    private fun startSessionsListActivity() {
        val intent = Intent(this, SessionsListActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun startUserSettingsActivity() {
        val intent = Intent(this, UserSettingsActivity::class.java)
        intent.putExtra("COMPLETE_PROFILE", true)
        startActivity(intent)
        finish()
    }
}