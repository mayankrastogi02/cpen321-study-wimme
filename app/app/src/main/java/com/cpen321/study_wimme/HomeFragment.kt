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
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var sessionsAdapter: SessionAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var sessionFilterToggleGroup: MaterialButtonToggleGroup
    private lateinit var profileIcon: ImageView
    private lateinit var addSessionFab: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private lateinit var fetchSessionsButton: FloatingActionButton
    private var isLoading = false
    private val sessionsList = ArrayList<Session>()
    private var currentVisibility = SessionVisibility.PRIVATE
    private var currentSessionFilter = SessionFilter.FIND

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
        initializeUIComponents(view)
        setUpRecyclerView()
        setUpToggleGroups()
        setUpClickListeners()
        fetchAllSessions(false)
        return view
    }

    private fun initializeUIComponents(view: View) {
        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView)
        visibilityToggleGroup = view.findViewById(R.id.visibilityToggleGroup)
        sessionFilterToggleGroup = view.findViewById(R.id.sessionFilterToggleGroup)
        profileIcon = view.findViewById(R.id.profileIcon)
        addSessionFab = view.findViewById(R.id.addSessionFab)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        fetchSessionsButton = view.findViewById(R.id.fetchSessionsButton)
    }

    private fun setUpRecyclerView() {
        sessionsRecyclerView.layoutManager = LinearLayoutManager(context)
        sessionsAdapter = SessionAdapter()
        sessionsRecyclerView.adapter = sessionsAdapter
    }

    private fun setUpToggleGroups() {
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

        sessionFilterToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentSessionFilter = when (checkedId) {
                    R.id.findButton -> SessionFilter.FIND
                    R.id.joinedButton -> SessionFilter.JOINED
                    R.id.hostedButton -> SessionFilter.HOSTED
                    else -> SessionFilter.FIND
                }
                updateSessionsDisplay()
            }
        }

        visibilityToggleGroup.check(R.id.privateButton)
        visibilityToggleGroup.isSelectionRequired = true
        sessionFilterToggleGroup.check(R.id.findButton)
        sessionFilterToggleGroup.isSelectionRequired = true
    }

    private fun setUpClickListeners() {
        profileIcon.setOnClickListener {
            val intent = Intent(context, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        addSessionFab.setOnClickListener {
            val intent = Intent(context, CreateSessionActivity::class.java)
            createSessionLauncher.launch(intent)
        }

        fetchSessionsButton.setOnClickListener {
            if (!isLoading) {
                fetchAllSessions(true)
            }
        }

        sessionsAdapter.setOnSessionClickListener { session ->
            Log.d("HomeFragment", "Host ID: ${session.hostId}")
            val intent = Intent(requireContext(), SessionDetailsActivity::class.java).apply {
                putExtra("SESSION_NAME", session.name)
                putExtra("SESSION_TIME", session.time)
                putExtra("SESSION_LOCATION", session.location)
                putExtra("SESSION_DESCRIPTION", session.description)
                putExtra("SESSION_SUBJECT", session.subject)
                putExtra("SESSION_FACULTY", session.faculty)
                putExtra("SESSION_YEAR", session.year)
                putExtra("SESSION_HOST", session.hostName)
                putExtra("SESSION_ID", session.id)
                putExtra("HOST_ID", session.hostId)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // We might not need to automatically fetch on resume if we have the fetch button
        // But keeping it could be useful for when returning from other screens
        if (LoginActivity.getCurrentUserId(requireActivity()) != null && !isLoading) {
            fetchAllSessions(false)
        }
    }

    private fun fetchAllSessions(showLoading: Boolean) {
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        if (userId == null) {
            showEmptyState("You need to be logged in to see sessions")
            return
        }

        if (showLoading) {
            isLoading = true
            fetchSessionsButton.isEnabled = false
            LoaderDialog.show(requireContext()) // Show loader
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/session/availableSessions/${userId}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Response: $response")

                    val fetchedSessions = parseSessions(response)
                    withContext(Dispatchers.Main) {
                        updateUIWithFetchedSessions(fetchedSessions, showLoading)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        handleErrorResponse(connection, responseCode)
                    }
                }
                connection.disconnect()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    handleNetworkError(e)
                }
            } catch (e: JSONException) {
                withContext(Dispatchers.Main) {
                    handleJSONError(e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    LoaderDialog.hide() // Hide loader
                }
            }
        }
    }

    private suspend fun handleErrorResponse(connection: HttpURLConnection, responseCode: Int) {
        val errorMessage =
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
        Log.e(TAG, "Error response: $errorMessage")
        showEmptyState("Error loading sessions (${responseCode})")
        isLoading = false
        fetchSessionsButton.isEnabled = true
    }

    private suspend fun handleNetworkError(e: IOException) {
        Log.e(TAG, "Network error fetching sessions", e)
        showEmptyState("Network error: ${e.message}")
        isLoading = false
        fetchSessionsButton.isEnabled = true
    }

    private suspend fun handleJSONError(e: JSONException) {
        Log.e(TAG, "JSON error fetching sessions", e)
        showEmptyState("JSON error: ${e.message}")
        isLoading = false
        fetchSessionsButton.isEnabled = true
    }

    private fun parseSessions(response: String): ArrayList<Session> {
        val jsonResponse = JSONObject(response)
        val sessionsArray = jsonResponse.getJSONArray("sessions")
        val fetchedSessions = ArrayList<Session>()

        for (i in 0 until sessionsArray.length()) {
            try {
                val sessionObj = sessionsArray.getJSONObject(i)
                val session = parseSessionObject(sessionObj)
                fetchedSessions.add(session)
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing session", e)
            } catch (e: ParseException) {
                Log.e(TAG, "Error parsing session date", e)
            }
        }
        return fetchedSessions
    }

    private fun parseSessionObject(sessionObj: JSONObject): Session {
        val name = sessionObj.getString("name")
        val description = sessionObj.optString("description", "")
        val dateRangeObj = sessionObj.getJSONObject("dateRange")
        val startDate = dateRangeObj.getString("startDate")
        val endDate = dateRangeObj.getString("endDate")
        val formattedTime = formatSessionTime(startDate, endDate)
        val locationObj = sessionObj.getJSONObject("location")
        val coordinates = locationObj.getJSONArray("coordinates")
        val longitude = coordinates.getDouble(0)
        val latitude = coordinates.getDouble(1)
        val formattedLocation = formatLocation(latitude, longitude)
        val isPublic = sessionObj.getBoolean("isPublic")
        val visibility = if (isPublic) SessionVisibility.PUBLIC else SessionVisibility.PRIVATE
        val subject = sessionObj.optString("subject", "")
        val faculty = sessionObj.optString("faculty", "")
        val year = sessionObj.optString("year", "").toString()
        val hostObj = sessionObj.optJSONObject("hostId")
        val hostId = hostObj?.optString("_id", "")
        val hostName = if (hostObj != null) {
            "${hostObj.optString("firstName", "")} ${hostObj.optString("lastName", "")}"
        } else {
            "Unknown Host"
        }
        val participantsArray = sessionObj.getJSONArray("participants")
        val participants = List(participantsArray.length()) { p ->
            participantsArray.getString(p)
        }

        return Session(
            id = sessionObj.getString("_id"),
            name = name,
            time = formattedTime,
            location = formattedLocation,
            description = description,
            visibility = visibility,
            subject = subject,
            faculty = faculty,
            year = year,
            hostName = hostName,
            hostId = hostId ?: "",
            participants = participants
        )
    }

    private fun updateUIWithFetchedSessions(
        fetchedSessions: ArrayList<Session>,
        showLoading: Boolean
    ) {
        sessionsList.clear()
        sessionsList.addAll(fetchedSessions)
        updateSessionsDisplay()
        isLoading = false
        fetchSessionsButton.isEnabled = true
        if (showLoading) {
            Toast.makeText(
                context,
                "Sessions updated!",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.d(TAG, "Loaded ${fetchedSessions.size} sessions")
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
        } catch (e: ParseException) {
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
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        val privateOrPublicSessions = sessionsList.filter { it.visibility == currentVisibility }
        var filteredSessions = privateOrPublicSessions
        Log.d(TAG, "UserId: ${userId}")
        Log.d(TAG, "Filtered sessions: ${filteredSessions}")

        if (currentSessionFilter == SessionFilter.FIND) {
            filteredSessions = privateOrPublicSessions.filter { it.hostId != userId }
        } else if (currentSessionFilter == SessionFilter.HOSTED) {
            filteredSessions = privateOrPublicSessions.filter { it.hostId == userId }
        } else if (currentSessionFilter == SessionFilter.JOINED) {
            filteredSessions = privateOrPublicSessions.filter { it.participants.contains(userId) }
        }

        Log.d(TAG, "Total sessions: ${sessionsList.size}")
        Log.d(
            TAG,
            "Public sessions: ${sessionsList.count { it.visibility == SessionVisibility.PUBLIC }}"
        )
        Log.d(
            TAG,
            "Private sessions: ${sessionsList.count { it.visibility == SessionVisibility.PRIVATE }}"
        )
        Log.d(
            TAG,
            "Currently showing ${currentVisibility} sessions: ${privateOrPublicSessions.size}"
        )

        if (filteredSessions.isEmpty()) {
            showEmptyState("No sessions found")
        } else {
            emptyStateTextView.visibility = View.GONE
            sessionsRecyclerView.visibility = View.VISIBLE

            // Filter public sessions and determine the top 3 recommended session IDs
            val publicSessions =
                sessionsList.filter { it.visibility == SessionVisibility.PUBLIC && it.hostId != userId }
            val recommendedSessionIds = publicSessions.take(3).map { it.id }.toSet()

            // Update adapter with filtered sessions and recommended IDs
            sessionsAdapter.updateSessions(filteredSessions, recommendedSessionIds)
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