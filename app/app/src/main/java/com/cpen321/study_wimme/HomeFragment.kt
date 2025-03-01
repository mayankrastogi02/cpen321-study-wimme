package com.cpen321.study_wimme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var sessionsAdapter: SessionAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var profileIcon: ImageView
    private lateinit var addSessionFab: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private val sessionsList = ArrayList<Session>()
    private var currentVisibility = SessionVisibility.PRIVATE

    // Register for activity result to handle session creation result
    private val createSessionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            // Extract session data from result
            val name = data.getStringExtra("SESSION_NAME") ?: return@registerForActivityResult
            val time = data.getStringExtra("SESSION_TIME") ?: return@registerForActivityResult
            val location = data.getStringExtra("SESSION_LOCATION") ?: return@registerForActivityResult
            val description = data.getStringExtra("SESSION_DESCRIPTION") ?: ""
            val visibility = data.getSerializableExtra("SESSION_VISIBILITY") as? SessionVisibility
                ?: SessionVisibility.PRIVATE

            // Create session object
            val newSession = Session(
                name = name,
                time = time,
                location = location,
                description = description,
                visibility = visibility
            )

            // Add to list and update adapter
            sessionsList.add(newSession)
            updateSessionsDisplay()

            // Log success
            Log.d(TAG, "Added new session: $name")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize UI components
        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView)
        visibilityToggleGroup = view.findViewById(R.id.visibilityToggleGroup)
        profileIcon = view.findViewById(R.id.profileIcon)
        addSessionFab = view.findViewById(R.id.addSessionFab)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        // Set up RecyclerView
        sessionsRecyclerView.layoutManager = LinearLayoutManager(context)
        sessionsAdapter = SessionAdapter()
        sessionsRecyclerView.adapter = sessionsAdapter

        // Set up visibility toggle
        visibilityToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentVisibility = when (checkedId) {
                    R.id.privateButton -> SessionVisibility.PRIVATE
                    R.id.publicButton -> SessionVisibility.PUBLIC
                    else -> SessionVisibility.PRIVATE
                }
                updateSessionsDisplay()
            }
        }

        // Set default visibility
        visibilityToggleGroup.check(R.id.privateButton)

        // Set up profile icon click
        profileIcon.setOnClickListener {
            val intent = Intent(context, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        // Set up add session button
        addSessionFab.setOnClickListener {
            val intent = Intent(context, CreateSessionActivity::class.java)
            createSessionLauncher.launch(intent)
        }

        // Load sessions
        fetchSessions()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Reload sessions when coming back to this fragment
        fetchSessions()
    }

    private fun fetchSessions() {
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        if (userId == null) {
            showEmptyState("You need to be logged in to see sessions")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/session?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val sessionsArray = jsonResponse.getJSONArray("sessions")

                    val fetchedSessions = ArrayList<Session>()
                    for (i in 0 until sessionsArray.length()) {
                        val sessionObj = sessionsArray.getJSONObject(i)

                        // Parse date range
                        val dateRangeObj = sessionObj.getJSONObject("dateRange")
                        val startDate = dateRangeObj.getString("startDate")
                        val endDate = dateRangeObj.getString("endDate")

                        // Format date and time for display
                        val formattedTime = formatSessionTime(startDate, endDate)

                        // Format location for display
                        val latitude = sessionObj.getDouble("latitude")
                        val longitude = sessionObj.getDouble("longitude")
                        val formattedLocation = formatLocation(latitude, longitude)

                        val session = Session(
                            name = sessionObj.getString("name"),
                            time = formattedTime,
                            location = formattedLocation,
                            description = sessionObj.getString("description"),
                            visibility = if (sessionObj.getBoolean("isPublic"))
                                SessionVisibility.PUBLIC else SessionVisibility.PRIVATE
                        )
                        fetchedSessions.add(session)
                    }

                    withContext(Dispatchers.Main) {
                        sessionsList.clear()
                        sessionsList.addAll(fetchedSessions)
                        updateSessionsDisplay()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showEmptyState("Error loading sessions")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching sessions", e)
                withContext(Dispatchers.Main) {
                    showEmptyState("Error: ${e.message}")
                }
            }
        }
    }

    fun getSessionsAdapter(): SessionAdapter {
        return sessionsAdapter
    }

    private fun updateSessionsDisplay() {
        val filteredSessions = sessionsList.filter { it.visibility == currentVisibility }
        
        if (filteredSessions.isEmpty()) {
            showEmptyState("No ${if(currentVisibility == SessionVisibility.PRIVATE) "private" else "public"} sessions found")
        } else {
            emptyStateTextView.visibility = View.GONE
            sessionsRecyclerView.visibility = View.VISIBLE
            sessionsAdapter.updateSessions(filteredSessions)
        }
    }

    private fun showEmptyState(message: String) {
        sessionsRecyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
        emptyStateTextView.text = message
    }

    private fun formatSessionTime(startDate: String, endDate: String): String {
        // This is a simplified version - you might want to use proper date parsing
        // For now, just extract data for display
        // Example format: Mar 15, 2024 2:30 PM - 4:30 PM
        return try {
            val start = startDate.substring(0, 16).replace("T", " ")
            val end = endDate.substring(11, 16)
            "$start - $end"
        } catch (e: Exception) {
            "Time not specified"
        }
    }

    private fun formatLocation(latitude: Double, longitude: Double): String {
        return "Lat: ${latitude.toString().take(7)}, Lng: ${longitude.toString().take(7)}"
    }
}