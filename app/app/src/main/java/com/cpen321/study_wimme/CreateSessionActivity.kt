package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText

class CreateSessionActivity : AppCompatActivity() {
    private var sessionVisibility: SessionVisibility = SessionVisibility.PRIVATE
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    companion object {
        private const val LOCATION_PICKER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val nameInput = findViewById<TextInputEditText>(R.id.sessionNameInput)
        val timeInput = findViewById<TextInputEditText>(R.id.sessionTimeInput)
        val locationInput = findViewById<TextInputEditText>(R.id.sessionLocationInput)
        val descriptionInput = findViewById<TextInputEditText>(R.id.sessionDescriptionInput)
        val visibilityGroup = findViewById<MaterialButtonToggleGroup>(R.id.visibilityToggleGroup)
        val hostButton = findViewById<MaterialButton>(R.id.hostButton)

        // Set initial visibility toggle state
        visibilityGroup.check(R.id.privateButton)

        visibilityGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                sessionVisibility = when (checkedId) {
                    R.id.privateButton -> SessionVisibility.PRIVATE
                    R.id.publicButton -> SessionVisibility.PUBLIC
                    else -> SessionVisibility.PRIVATE
                }
            }
        }

        // Launch location picker when tapping the location input field
        locationInput.setOnClickListener {
            Log.d("CreateSession", "Location field tapped")
            startActivityForResult(Intent(this, MapLocationPickerActivity::class.java), LOCATION_PICKER_REQUEST_CODE)
        }

        hostButton.setOnClickListener {
            if (selectedLatitude == null || selectedLongitude == null) {
                // Inform the user to pick a location first
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val session = Session(
                name = nameInput.text?.toString() ?: "",
                time = timeInput.text?.toString() ?: "",
                location = locationInput.text?.toString() ?: "",
                description = descriptionInput.text?.toString() ?: "",
                visibility = sessionVisibility
            )

            setResult(RESULT_OK, Intent().apply {
                putExtra("SESSION_NAME", session.name)
                putExtra("SESSION_TIME", session.time)
                putExtra("SESSION_LATITUDE", selectedLatitude)
                putExtra("SESSION_LATITUDE", selectedLongitude)
                putExtra("SESSION_DESCRIPTION", session.description)
                putExtra("SESSION_VISIBILITY", session.visibility)
            })
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            val lat = data?.getDoubleExtra("LATITUDE", 0.0)
            val lng = data?.getDoubleExtra("LONGITUDE", 0.0)
            if (lat != null && lng != null) {
                selectedLatitude = lat
                selectedLongitude = lng
                findViewById<TextInputEditText>(R.id.sessionLocationInput).setText("Lat: $lat, Lng: $lng")
            }
        }
    }
}