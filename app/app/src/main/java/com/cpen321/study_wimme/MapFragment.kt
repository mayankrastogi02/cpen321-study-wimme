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
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            // Get the session associated with the marker
            val session = marker.tag as? SessionDto
            session?.let {
                // Show a dialog asking the user if they want to join the session
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(it.name)
                    .setMessage("Would you like to join this session?\n\n${it.description}")
                    .setPositiveButton("Join") { dialog, which ->
                        // TODO: Call API to join the session using its id
                        Toast.makeText(requireContext(), "Joining session...", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            // Optionally, show the marker's info window as well.
            marker.showInfoWindow()
            true // Consume the event.
        }
    }
    private fun fetchNearbySessions(latitude: Double, longitude: Double, radius: Double) {
        val url = "${BuildConfig.SERVER_URL}/session/nearbySessions?latitude=$latitude&longitude=$longitude&radius=$radius"
        Log.d("MapFragment", "Fetching sessions from URL: $url")

        // Launch a coroutine to fetch sessions in background
        lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) {
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

            sessions.forEach { session ->
                // session.location.coordinates: [longitude, latitude]
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
    }
}

// Data classes for JSON parsing using Gson
data class NearbySessionsResponse(
    val sessions: List<SessionDto>
)

data class SessionDto(
    val name: String,
    val description: String,
    val location: LocationDto
)

data class LocationDto(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
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
