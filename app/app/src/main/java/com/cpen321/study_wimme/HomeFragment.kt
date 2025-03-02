package com.cpen321.study_wimme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var sessionsAdapter: SessionAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var profileIcon: ImageView
    private lateinit var addSessionFab: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private lateinit var fetchSessionsButton: Button
    private var isLoading = false
    private val sessionsList = ArrayList<Session>()
    private var currentVisibility = SessionVisibility.PRIVATE

    // Register for activity result
    private val createSessionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Process the result - you already have this implementation
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
        fetchSessionsButton = view.findViewById(R.id.fetchSessionsButton)

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

        sessionsAdapter.setOnSessionClickListener { session ->
            // Get session ID and other detailed info from your data source
            val sessionId = session.id // Make sure Session class has an ID field
            
            // Launch SessionDetailsActivity with session details
            val intent = Intent(requireContext(), SessionDetailsActivity::class.java).apply {
                putExtra("SESSION_NAME", session.name)
                putExtra("SESSION_TIME", session.time)
                putExtra("SESSION_LOCATION", session.location)
                putExtra("SESSION_DESCRIPTION", session.description)
                putExtra("SESSION_SUBJECT", session.subject) // You'll need to add these fields to Session
                putExtra("SESSION_FACULTY", session.faculty)
                putExtra("SESSION_YEAR", session.year)
                putExtra("SESSION_HOST", session.hostName)
                putExtra("SESSION_ID", sessionId)
            }
            startActivity(intent)
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

        // Set up fetch sessions button
        fetchSessionsButton.setOnClickListener {
            if (!isLoading) {
                fetchSessions(true) // Pass true to show a loading indicator
            }
        }

        // Load sessions
        fetchSessions(false)

        return view
    }
    
    override fun onResume() {
        super.onResume()
        // We might not need to automatically fetch on resume if we have the fetch button
        // But keeping it could be useful for when returning from other screens
        if (LoginActivity.getCurrentUserId(requireActivity()) != null && !isLoading) {
            fetchSessions(false)
        }
    }

    private fun fetchSessions(showLoading: Boolean) {
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        if (userId == null) {
            showEmptyState("You need to be logged in to see sessions")
            return
        }
        
        if (showLoading) {
            // Show loading state
            isLoading = true
            fetchSessionsButton.isEnabled = false
            fetchSessionsButton.text = "Loading..."
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/session/availableSessions?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Response: $response")
                    
                    val jsonResponse = JSONObject(response)
                    val sessionsArray = jsonResponse.getJSONArray("sessions")
                    
                    val fetchedSessions = ArrayList<Session>()
                    for (i in 0 until sessionsArray.length()) {
                        try {
                            val sessionObj = sessionsArray.getJSONObject(i)
                            
                            // Get name, description
                            val name = sessionObj.getString("name")
                            val description = sessionObj.optString("description", "")
                            
                            // Parse date range
                            val dateRangeObj = sessionObj.getJSONObject("dateRange")
                            val startDate = dateRangeObj.getString("startDate")
                            val endDate = dateRangeObj.getString("endDate")
                            
                            // Format date and time for display
                            val formattedTime = formatSessionTime(startDate, endDate)
                            
                            // Get location from GeoJSON format
                            val locationObj = sessionObj.getJSONObject("location")
                            val coordinates = locationObj.getJSONArray("coordinates")
                            val longitude = coordinates.getDouble(0)
                            val latitude = coordinates.getDouble(1)
                            val formattedLocation = formatLocation(latitude, longitude)
                            
                            // Check if public/private
                            val isPublic = sessionObj.getBoolean("isPublic")

                            // Inside the loop that processes sessions in fetchSessions
                            val subject = sessionObj.optString("subject", "")
                            val faculty = sessionObj.optString("faculty", "")
                            val year = sessionObj.optString("year", "").toString()

                            // Get host information if available
                            val hostObj = sessionObj.optJSONObject("hostId")
                            val hostName = if (hostObj != null) {
                                "${hostObj.optString("firstName", "")} ${hostObj.optString("lastName", "")}"
                            } else {
                                "Unknown Host"
                            }

                            val session = Session(
                                id = sessionObj.getString("_id"),
                                name = name,
                                time = formattedTime,
                                location = formattedLocation,
                                description = description,
                                visibility = if (isPublic) SessionVisibility.PUBLIC else SessionVisibility.PRIVATE,
                                subject = subject,
                                faculty = faculty,
                                year = year,
                                hostName = hostName
                            )
                            
                            fetchedSessions.add(session)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing session", e)
                            // Continue to next session if one fails to parse
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        sessionsList.clear()
                        sessionsList.addAll(fetchedSessions)
                        updateSessionsDisplay()
                        
                        // Reset loading state
                        isLoading = false
                        fetchSessionsButton.isEnabled = true
                        fetchSessionsButton.text = "Fetch New Sessions"
                        
                        // Show success message if this was a manual refresh
                        if (showLoading) {
                            Toast.makeText(context, 
                                "Sessions updated! Found ${fetchedSessions.size} sessions", 
                                Toast.LENGTH_SHORT).show()
                        }
                        
                        Log.d(TAG, "Loaded ${fetchedSessions.size} sessions")
                    }
                } else {
                    // Handle error response
                    val errorMessage = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e(TAG, "Error response: $errorMessage")
                    
                    withContext(Dispatchers.Main) {
                        showEmptyState("Error loading sessions (${responseCode})")
                        
                        // Reset loading state
                        isLoading = false
                        fetchSessionsButton.isEnabled = true
                        fetchSessionsButton.text = "Fetch New Sessions"
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching sessions", e)
                withContext(Dispatchers.Main) {
                    showEmptyState("Error: ${e.message}")
                    
                    // Reset loading state
                    isLoading = false
                    fetchSessionsButton.isEnabled = true
                    fetchSessionsButton.text = "Fetch New Sessions"
                }
            }
        }
    }

    // Helper method for date formatting
    private fun formatSessionTime(startDate: String, endDate: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val startDateTime = dateFormat.parse(startDate)
            val endDateTime = dateFormat.parse(endDate)
            
            val displayFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US)
            displayFormat.timeZone = TimeZone.getDefault() // Convert to local time
            
            val endTimeOnlyFormat = SimpleDateFormat("h:mm a", Locale.US)
            endTimeOnlyFormat.timeZone = TimeZone.getDefault()
            
            displayFormat.format(startDateTime) + " - " + endTimeOnlyFormat.format(endDateTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting session time", e)
            "Time not specified"
        }
    }

    // Helper method for location formatting
    private fun formatLocation(latitude: Double, longitude: Double): String {
        return "Lat: ${latitude.toString().take(7)}, Lng: ${longitude.toString().take(7)}"
    }

    private fun updateSessionsDisplay() {
        // Filter sessions based on current visibility
        val filteredSessions = sessionsList.filter { it.visibility == currentVisibility }
        
        if (filteredSessions.isEmpty()) {
            showEmptyState("No ${if(currentVisibility == SessionVisibility.PRIVATE) "private" else "public"} sessions found")
        } else {
            emptyStateTextView.visibility = View.GONE
            sessionsRecyclerView.visibility = View.VISIBLE
            
            // Update adapter with filtered sessions
            sessionsAdapter.updateSessions(filteredSessions)
        }
    }
    
    private fun showEmptyState(message: String) {
        sessionsRecyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
        emptyStateTextView.text = message
    }
    
    // Method to expose the adapter to parent activity
    fun getSessionsAdapter(): SessionAdapter {
        return sessionsAdapter
    }
}