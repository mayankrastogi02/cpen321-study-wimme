package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton

class MapLocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private lateinit var confirmButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location_picker)

        // Set up confirm button
        confirmButton = findViewById(R.id.confirmButton)
        confirmButton.setOnClickListener {
            selectedLatLng?.let {
                val resultIntent = Intent().apply {
                    putExtra("LATITUDE", it.latitude)
                    putExtra("LONGITUDE", it.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        // Initialize map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set default location (example: Vancouver)
        val defaultLatLng = LatLng(49.2827, -123.1207)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 14f))

        googleMap.setOnMapClickListener { latLng ->
            // Clear previous marker and add a new one
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLatLng = latLng
        }
    }
}
