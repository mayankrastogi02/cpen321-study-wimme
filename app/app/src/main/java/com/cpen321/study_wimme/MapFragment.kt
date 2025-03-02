package com.cpen321.study_wimme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val client = OkHttpClient()
    private var currentLocationLatLng: LatLng? = null
    private var allSessions: List<SessionDto> = emptyList()
    private var sessionFilter: String = "both"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.sessionToggleGroup)
        toggleGroup?.addOnButtonCheckedListener { group, _, _ ->
            sessionFilter = when (group.checkedButtonId) {
                R.id.publicButton -> "public"
                R.id.privateButton -> "private"
                else -> "both"
            }
            updateMapMarkers(filterSessions())
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }

        // Use FusedLocationProviderClient to fetch the last known location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                currentLocationLatLng = currentLatLng
                val currentLocation = googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                currentLocation?.showInfoWindow()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                fetchNearbySessions(location.latitude, location.longitude, 3000.0)
            }
        }

        // Set a marker click listener for join option
        googleMap.setOnMarkerClickListener { marker ->
            // If the marker's title is "Current Location", ignore it.
            if (marker.title == "Current Location") {
                marker.showInfoWindow()
                return@setOnMarkerClickListener true
            }

            val session = marker.tag as? SessionDto
            session?.let {
                val message = """
                    Would you like to join this session?
                    
                    ${it.description}
                    
                    Start Date: ${it.dateRange.startDate}
                    End Date: ${it.dateRange.endDate}
                """.trimIndent()
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(it.name)
                    .setMessage(message)
                    .setPositiveButton("Join") { dialog, which ->
                        val userId = LoginActivity.getCurrentUserId(requireActivity())
                        Log.d("MapFragment", "Current user ID: $userId")
                        Toast.makeText(requireContext(), "Joining session...", Toast.LENGTH_SHORT).show()

                        if (userId != null) {
                            joinSession(it.sessionId, userId)
                        } else {
                            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            // Optionally, show the marker's info window as well.
            marker.showInfoWindow()
            true
        }
    }

    private fun filterSessions(): List<SessionDto> {
        return when (sessionFilter) {
            "public" -> allSessions.filter { it.isPublic }
            "private" -> allSessions.filter { !it.isPublic }
            else -> allSessions
        }
    }

    private fun updateMapMarkers(sessions: List<SessionDto>) {
        googleMap.clear()

        // Re-add the current location marker
        currentLocationLatLng?.let { latLng ->
            val currentMarker = googleMap.addMarker(
                MarkerOptions().position(latLng).title("Current Location")
            )
            currentMarker?.showInfoWindow()
        }

        sessions.forEach { session ->
            Log.d("MapFragment", "Session: ${session.name}, Coordinates: ${session.location.coordinates}")
            val lng = session.location.coordinates[0]
            val lat = session.location.coordinates[1]
            val sessionLatLng = LatLng(lat, lng)
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(sessionLatLng)
                    .title(session.name)
                    .snippet(session.description)
            )
            marker?.tag = session
        }
    }

    private fun fetchNearbySessions(latitude: Double, longitude: Double, radius: Double) {
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        val url = "${BuildConfig.SERVER_URL}/session/nearbySessions?latitude=$latitude&longitude=$longitude&radius=$radius&userId=$userId"
        Log.d("MapFragment", "Fetching sessions from URL: $url")

        // Launch a coroutine to fetch sessions in background
        lifecycleScope.launch {
            allSessions = withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                try {
                    val response = client.newCall(request).execute()
                    Log.d("MapFragment", "Response code: ${response.code}")
                    if (!response.isSuccessful) {
                        response.close()
                        emptyList<SessionDto>()
                    } else {
                        val bodyString = response.body?.string() ?: ""
                        response.close()
                        parseSessionsJson(bodyString)
                    }
                } catch (e: Exception) {
                    Log.e("MapFragment", "JSON parsing error", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error occurred, please return to home screen and try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    emptyList<SessionDto>()
                }
            }
            updateMapMarkers(filterSessions())
        }
    }

    private fun joinSession(sessionId: String, userId: String) {
        lifecycleScope.launch {
            // Create JSON body with the userId
            val json = JSONObject().apply { put("userId", userId) }
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            // Build the join URL using the session ID
            val joinUrl = "${BuildConfig.SERVER_URL}/session/$sessionId/join"
            Log.d("MapFragment", "Join URL: $joinUrl")

            // Build the PUT request
            val request = Request.Builder()
                .url(joinUrl)
                .put(requestBody)
                .build()

            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Joined session successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Try to extract a detailed error message from the response
                    val errorBody = response.body?.string()
                    var errorMessage = "Failed to join session."
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val errorJson = JSONObject(errorBody)
                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message")
                            }
                        } catch (e: Exception) {
                            Log.e("MapFragment", "Error parsing error message", e)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            } catch (e: Exception) {
                Log.e("MapFragment", "Error joining session", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error joining session: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Data classes for JSON parsing using Gson
data class NearbySessionsResponse(
    val sessions: List<SessionDto>
)

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class LocationDto(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
)

data class SessionDto(
    @SerializedName("_id")
    val sessionId: String,
    val name: String,
    val description: String,
    val location: LocationDto,
    val dateRange: DateRange,
    val isPublic: Boolean,
)

fun parseSessionsJson(jsonString: String): List<SessionDto> {
    return try {
        val gson = Gson()
        val response = gson.fromJson(jsonString, NearbySessionsResponse::class.java)
        response.sessions
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
