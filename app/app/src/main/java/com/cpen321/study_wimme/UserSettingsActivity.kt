package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
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

class UserSettingsActivity : AppCompatActivity() {

    private val TAG = "UserSettingsActivity"
    private var isCompletingProfile = false
    private lateinit var usernameInput: TextInputEditText
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var facultyInput: TextInputEditText
    private lateinit var yearInput: TextInputEditText
    private lateinit var interestsInput: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        isCompletingProfile = intent.getBooleanExtra("COMPLETE_PROFILE", false)

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameEditText)
        firstNameInput = findViewById(R.id.firstNameEditText)
        lastNameInput = findViewById(R.id.lastNameEditText)
        facultyInput = findViewById(R.id.programEditText)
        yearInput = findViewById(R.id.yearEditText)
        interestsInput = findViewById(R.id.interestsEditText)
        saveButton = findViewById(R.id.saveButton)
        logoutButton = findViewById(R.id.logoutButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        backButton = findViewById(R.id.backButton)

        // Setup back button functionality
        if (isCompletingProfile) {
            // Hide back button if completing profile for the first time
            backButton.visibility = View.GONE
            // Also hide delete account button for new users
            deleteAccountButton.visibility = View.GONE
        } else {
            backButton.setOnClickListener {
                finish() // Just go back if not completing profile
            }
        }

        // Load user data from sharedPreferences for initial values
        loadUserData()

        // Set up save button
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveUserProfile()
            }
        }

        // Set up logout button
        logoutButton.setOnClickListener {
            logout()
        }

        // Set up logout button
        logoutButton.setOnClickListener {
            logout()
        }

        // Set up delete account button - add debug logs
        deleteAccountButton.setOnClickListener {
            Log.d(TAG, "Delete account button clicked")
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        Log.d(TAG, "Showing delete confirmation dialog")
        
        // Create and configure the AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, which ->
                Log.d(TAG, "Delete confirmed")
                deleteUserAccount()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                Log.d(TAG, "Delete canceled")
            }
            .setCancelable(false) // Prevent dismissing by tapping outside
            .show() // Important! This displays the dialog
    }

    private fun deleteUserAccount() {
        val userId = LoginActivity.getCurrentUserId(this)
        
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/user")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("userId", userId)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@UserSettingsActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        
                        // Clear user data from SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        sharedPreferences.edit().clear().apply()
                        
                        // Sign out from Google
                        LoginActivity.signOut(this@UserSettingsActivity)
                        
                        // Redirect to login screen
                        val intent = Intent(this@UserSettingsActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Try to read error message from response
                        try {
                            val errorStream = connection.errorStream ?: connection.inputStream
                            val errorResponse = errorStream.bufferedReader().use { it.readText() }
                            val jsonError = JSONObject(errorResponse)
                            val errorMessage = jsonError.optString("message", "Failed to delete account")
                            Toast.makeText(this@UserSettingsActivity, errorMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@UserSettingsActivity, "Failed to delete account: $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting account", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserSettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserData() {
        val googleId = LoginActivity.getCurrentUserGoogleId(this)
        if (googleId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load basic info from shared preferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val displayName = sharedPreferences.getString("displayName", "") ?: ""
        val email = sharedPreferences.getString("email", "") ?: ""

        // If this is a new user setting up their profile, pre-populate with info from Google
        if (isCompletingProfile) {
            val nameParts = displayName.split(" ")
            if (nameParts.isNotEmpty()) {
                firstNameInput.setText(nameParts[0])
                if (nameParts.size > 1) {
                    lastNameInput.setText(nameParts.subList(1, nameParts.size).joinToString(" "))
                }
            }

            // Set default username from email
            val defaultUsername = email.split("@").firstOrNull() ?: ""
            usernameInput.setText(defaultUsername)
        }

        // If user is editing an existing profile, fetch the full profile data from server
        if (!isCompletingProfile) {
            fetchUserProfile(googleId)
        }
    }

    private fun fetchUserProfile(googleId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/auth/verify?googleId=$googleId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val userData = jsonResponse.getJSONObject("data")

                    withContext(Dispatchers.Main) {
                        usernameInput.setText(userData.optString("userName", ""))
                        firstNameInput.setText(userData.optString("firstName", ""))
                        lastNameInput.setText(userData.optString("lastName", ""))
                        facultyInput.setText(userData.optString("faculty", ""))
                        yearInput.setText(userData.optInt("year", 1).toString())
                        interestsInput.setText(userData.optString("interests", ""))
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UserSettingsActivity,
                        "Error loading profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (usernameInput.text.toString().trim().isEmpty()) {
            usernameInput.error = "Username is required"
            isValid = false
        }

        if (firstNameInput.text.toString().trim().isEmpty()) {
            firstNameInput.error = "First name is required"
            isValid = false
        }

        if (lastNameInput.text.toString().trim().isEmpty()) {
            lastNameInput.error = "Last name is required"
            isValid = false
        }

        if (facultyInput.text.toString().trim().isEmpty()) {
            facultyInput.error = "Faculty is required"
            isValid = false
        }

        if (yearInput.text.toString().trim().isEmpty()) {
            yearInput.error = "Year is required"
            isValid = false
        } else {
            try {
                val year = yearInput.text.toString().toInt()
                if (year < 1) {
                    yearInput.error = "Year must be at least 1"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                yearInput.error = "Year must be a number"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveUserProfile() {
        val googleId = LoginActivity.getCurrentUserGoogleId(this)
        if (googleId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val username = usernameInput.text.toString().trim()
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val faculty = facultyInput.text.toString().trim()
        val year = yearInput.text.toString().trim().toInt()
        val interests = interestsInput.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/auth/profile/$googleId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("userName", username)
                    put("firstName", firstName)
                    put("lastName", lastName)
                    put("faculty", faculty)
                    put("year", year)
                    put("interests", interests)
                }

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonData.toString())
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response to get user ID
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonResponse = JSONObject(response.toString())

                    // Check if the response contains user data with _id
                    if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("_id")) {
                        val userId = jsonResponse.getJSONObject("data").getString("_id")

                        // Save the user ID in SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userId", userId)
                        editor.apply()

                        Log.d(TAG, "Saved user ID: $userId")
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@UserSettingsActivity,
                            "Profile saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Log all preferences for debugging
                        LoginActivity.logAllPreferences(this@UserSettingsActivity)

                        if (isCompletingProfile) {
                            // Navigate to main activity after completing profile setup
                            val intent =
                                Intent(this@UserSettingsActivity, SessionsListActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Just finish this activity and go back
                            finish()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@UserSettingsActivity,
                            "Failed to save profile: $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UserSettingsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun logout() {
        LoginActivity.signOut(this)
    }

    // Override back button behavior for new users completing their profile
    override fun onBackPressed() {
        if (isCompletingProfile) {
            Toast.makeText(this, "Please complete your profile first", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }
}