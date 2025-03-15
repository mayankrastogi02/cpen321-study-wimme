package com.cpen321.study_wimme

import SessionService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SessionDetailsActivity : AppCompatActivity() {

    private val TAG = "SessionDetailsActivity"

    private lateinit var sessionNameTextView: TextView
    private lateinit var sessionTimeTextView: TextView
    private lateinit var sessionLocationTextView: TextView
    private lateinit var sessionDescriptionTextView: TextView
    private lateinit var subjectTextView: TextView
    private lateinit var facultyTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var hostTextView: TextView
    private lateinit var joinButton: MaterialButton
    private lateinit var leaveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Save sessionId and hostID for API calls
    private var sessionId: String? = null
    private var sessionHostId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_details)

        // Initialize views
        sessionNameTextView = findViewById(R.id.sessionNameTextView)
        sessionTimeTextView = findViewById(R.id.sessionTimeTextView)
        sessionLocationTextView = findViewById(R.id.sessionLocationTextView)
        sessionDescriptionTextView = findViewById(R.id.sessionDescriptionTextView)
        subjectTextView = findViewById(R.id.subjectTextView)
        facultyTextView = findViewById(R.id.facultyTextView)
        yearTextView = findViewById(R.id.yearTextView)
        hostTextView = findViewById(R.id.hostTextView)
        joinButton = findViewById(R.id.joinButton)
        leaveButton = findViewById(R.id.leaveButton)
        deleteButton = findViewById(R.id.deleteButton)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Get session data from intent
        val sessionName = intent.getStringExtra("SESSION_NAME") ?: "Unknown Session"
        val sessionTime = intent.getStringExtra("SESSION_TIME") ?: ""
        val sessionLocation = intent.getStringExtra("SESSION_LOCATION") ?: ""
        val sessionDescription = intent.getStringExtra("SESSION_DESCRIPTION") ?: ""
        val sessionSubject = intent.getStringExtra("SESSION_SUBJECT") ?: ""
        val sessionFaculty = intent.getStringExtra("SESSION_FACULTY") ?: ""
        val sessionYear = intent.getStringExtra("SESSION_YEAR") ?: ""
        val sessionHost = intent.getStringExtra("SESSION_HOST") ?: "Unknown Host"
        sessionId = intent.getStringExtra("SESSION_ID")
        sessionHostId = intent.getStringExtra("HOST_ID")

        // Set data to views
        sessionNameTextView.text = sessionName
        sessionTimeTextView.text = sessionTime
        sessionLocationTextView.text = sessionLocation
        sessionDescriptionTextView.text = sessionDescription
        subjectTextView.text = sessionSubject
        facultyTextView.text = sessionFaculty
        yearTextView.text = sessionYear
        hostTextView.text = "Hosted by: $sessionHost"

        val currentUserId = LoginActivity.getCurrentUserId(this)

        // Set up join,delete and leave button
        if (currentUserId != null && sessionHostId != null && currentUserId == sessionHostId) {
            joinButton.visibility = View.GONE
            leaveButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE

            deleteButton.setOnClickListener {
                deleteSession()
            }
        } else {
            joinButton.visibility = View.VISIBLE
            leaveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE

            joinButton.setOnClickListener { joinSession() }
            leaveButton.setOnClickListener { leaveSession() }
        }
    }

    //TODO:: For future move this to sessionService (or use the preexisting logic there)
    private fun joinSession() {
        if (sessionId == null) {
            Toast.makeText(this, "Session ID is missing. Cannot join.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = LoginActivity.getCurrentUserId(this)
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to join a session", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        progressBar.visibility = View.VISIBLE
        joinButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/session/${sessionId}/join")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Create request body
                val jsonBody = JSONObject().apply {
                    put("userId", userId)
                }

                // Send request
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonBody.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    joinButton.isEnabled = true

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@SessionDetailsActivity,
                            "Successfully joined session!", Toast.LENGTH_SHORT).show()
//                        This does not work as expected at the moment
//                        joinButton.text = "Joined"
//                        joinButton.isEnabled = false
                    } else {
                        // Read error message
                        val errorStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
                        val response = errorStream.bufferedReader().use { it.readText() }

                        try {
                            val errorJson = JSONObject(response)
                            val errorMessage = errorJson.optString("message", "Failed to join session")
                            Toast.makeText(this@SessionDetailsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        } catch (e: JSONException) {
                            Toast.makeText(this@SessionDetailsActivity,
                                "Error joining session: $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error joining session", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    joinButton.isEnabled = true
                    Toast.makeText(this@SessionDetailsActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun leaveSession() {
        val userId = LoginActivity.getCurrentUserId(this)
        if (sessionId == null || userId == null) {
            Toast.makeText(this, "Session ID or User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a progress indicator and disable the leave button
        progressBar.visibility = View.VISIBLE
        leaveButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val result = SessionService.leaveSession(sessionId!!, userId)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                leaveButton.isEnabled = true
                if (result.success) {
                    Toast.makeText(this@SessionDetailsActivity, "Left session successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SessionDetailsActivity, result.errorMessage ?: "Failed to leave session.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteSession() {
        val currentUserId = LoginActivity.getCurrentUserId(this)
        if (sessionId == null || currentUserId == null || sessionHostId == null) {
            Toast.makeText(this, "Session ID or User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUserId != sessionHostId) {
            Toast.makeText(this, "Only the host can delete this session.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        progressBar.visibility = View.VISIBLE
        deleteButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val result = SessionService.deleteSession(sessionId!!)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                deleteButton.isEnabled = true
                if (result.success) {
                    Toast.makeText(this@SessionDetailsActivity, "Session deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@SessionDetailsActivity, result.errorMessage ?: "Failed to delete session.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}