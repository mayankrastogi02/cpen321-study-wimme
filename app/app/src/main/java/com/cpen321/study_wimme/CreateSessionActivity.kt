package com.cpen321.study_wimme

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CreateSessionActivity : AppCompatActivity() {
    private var sessionVisibility: SessionVisibility = SessionVisibility.PRIVATE
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private val calendar = Calendar.getInstance()
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0

    companion object {
        private const val LOCATION_PICKER_REQUEST_CODE = 1001
        private const val FRIEND_SELECT_REQUEST_CODE = 1002
        private const val TAG = "CreateSessionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)

        val nameInput = findViewById<TextInputEditText>(R.id.sessionNameInput)
        val startTimeInput = findViewById<TextInputEditText>(R.id.startTimeInput)
        val endTimeInput = findViewById<TextInputEditText>(R.id.endTimeInput)
        val locationInput = findViewById<TextInputEditText>(R.id.sessionLocationInput)
        val descriptionInput = findViewById<TextInputEditText>(R.id.sessionDescriptionInput)
        val subjectInput = findViewById<TextInputEditText>(R.id.subjectInput)
        val facultyInput = findViewById<TextInputEditText>(R.id.facultyInput)
        val yearInput = findViewById<TextInputEditText>(R.id.yearInput)
        val visibilityGroup = findViewById<MaterialButtonToggleGroup>(R.id.visibilityToggleGroup)
        val hostButton = findViewById<MaterialButton>(R.id.hostButton)

        setupVisibilityToggle(visibilityGroup)
        setupDateTimePickers(startTimeInput, endTimeInput)
        setupLocationPicker(locationInput)
        setupHostButton(hostButton, nameInput, descriptionInput, subjectInput, facultyInput, yearInput)
    }

    private fun setupVisibilityToggle(visibilityGroup: MaterialButtonToggleGroup) {
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
    }

    private fun setupDateTimePickers(startTimeInput: TextInputEditText, endTimeInput: TextInputEditText) {
        startTimeInput.setOnClickListener {
            showDateTimePicker { selectedDate ->
                startDateMillis = selectedDate.time
                startTimeInput.setText(SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(selectedDate))
            }
        }

        endTimeInput.setOnClickListener {
            showDateTimePicker { selectedDate ->
                endDateMillis = selectedDate.time
                endTimeInput.setText(SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(selectedDate))
            }
        }
    }

    private fun setupLocationPicker(locationInput: TextInputEditText) {
        locationInput.setOnClickListener {
            Log.d(TAG, "Location field tapped")
            startActivityForResult(Intent(this, MapLocationPickerActivity::class.java), LOCATION_PICKER_REQUEST_CODE)
        }
    }

    private fun setupHostButton(
        hostButton: MaterialButton,
        nameInput: TextInputEditText,
        descriptionInput: TextInputEditText,
        subjectInput: TextInputEditText,
        facultyInput: TextInputEditText,
        yearInput: TextInputEditText
    ) {
        hostButton.setOnClickListener {
            val name = nameInput.text?.toString()
            val description = descriptionInput.text?.toString()
            val subject = subjectInput.text?.toString()
            val faculty = facultyInput.text?.toString()
            val yearString = yearInput.text?.toString()

            if (!validateInputs(name, startDateMillis, endDateMillis, selectedLatitude, selectedLongitude, subject, faculty, yearString)) {
                return@setOnClickListener
            }

            val year = yearString!!.toInt()

            if (sessionVisibility == SessionVisibility.PRIVATE) {
                val intent = Intent(this, InviteFriendsActivity::class.java)
                intent.putExtra("SESSION_NAME", name)
                intent.putExtra("SESSION_DESCRIPTION", description ?: "")
                intent.putExtra("SESSION_LATITUDE", selectedLatitude)
                intent.putExtra("SESSION_LONGITUDE", selectedLongitude)
                intent.putExtra("SESSION_START_DATE", startDateMillis)
                intent.putExtra("SESSION_END_DATE", endDateMillis)
                intent.putExtra("SESSION_IS_PUBLIC", false)
                intent.putExtra("SESSION_SUBJECT", subject)
                intent.putExtra("SESSION_FACULTY", faculty)
                intent.putExtra("SESSION_YEAR", year)
                startActivityForResult(intent, FRIEND_SELECT_REQUEST_CODE)
            } else {
                createSessionOnServer(
                    name = name!!,
                    description = description ?: "",
                    latitude = selectedLatitude!!,
                    longitude = selectedLongitude!!,
                    startDate = Date(startDateMillis),
                    endDate = Date(endDateMillis),
                    isPublic = true,
                    subject = subject!!,
                    faculty = faculty!!,
                    year = year,
                    invitees = arrayListOf()
                )
            }
        }
    }

    private fun validateInputs(
        name: String?,
        startDateMillis: Long,
        endDateMillis: Long,
        selectedLatitude: Double?,
        selectedLongitude: Double?,
        subject: String?,
        faculty: String?,
        yearString: String?
    ): Boolean {
        val nameInput = findViewById<TextInputEditText>(R.id.sessionNameInput)
        val startTimeInput = findViewById<TextInputEditText>(R.id.startTimeInput)
        val endTimeInput = findViewById<TextInputEditText>(R.id.endTimeInput)
        val subjectInput = findViewById<TextInputEditText>(R.id.subjectInput)
        val facultyInput = findViewById<TextInputEditText>(R.id.facultyInput)
        val yearInput = findViewById<TextInputEditText>(R.id.yearInput)

        if (name.isNullOrEmpty()) {
            nameInput.error = "Session name is required"
            return false
        }

        if (startDateMillis == 0L) {
            startTimeInput.error = "Start time is required"
            return false
        }

        if (endDateMillis == 0L) {
            endTimeInput.error = "End time is required"
            return false
        }

        if (startDateMillis >= endDateMillis) {
            endTimeInput.error = "End time must be after start time"
            return false
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            // Inform the user to pick a location first
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            return false
        }

        if (subject.isNullOrEmpty()) {
            subjectInput.error = "Subject is required"
            return false
        }

        if (faculty.isNullOrEmpty()) {
            facultyInput.error = "Faculty is required"
            return false
        }

        if (yearString.isNullOrEmpty()) {
            yearInput.error = "Year is required"
            return false
        }

        val year = yearString.toIntOrNull()
        if (year == null || year < 1) {
            yearInput.error = "Valid year is required"
            return false
        }

        return true
    }

    private fun showDateTimePicker(onDateSelected: (Date) -> Unit) {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Date picker dialog
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Time picker dialog
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute)
                onDateSelected(selectedCalendar.time)
            }, currentHour, currentMinute, false).show()
        }, currentYear, currentMonth, currentDay).show()
    }

    private fun createSessionOnServer(
        name: String,
        description: String,
        latitude: Double,
        longitude: Double,
        startDate: Date,
        endDate: Date,
        isPublic: Boolean,
        subject: String,
        faculty: String,
        year: Int,
        invitees: ArrayList<String>
    ) {
        val userId = LoginActivity.getCurrentUserId(this)

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Creating session with hostId: $userId")
                val url = URL("${BuildConfig.SERVER_URL}/session")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = createSessionJsonData(
                    name, description, userId, latitude, longitude, startDate, endDate, isPublic, subject, faculty, year, invitees
                )

                val jsonString = jsonData.toString()
                Log.d(TAG, "Request body: $jsonString")

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonString)
                writer.flush()

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                val responseBody = if (responseCode >= 400) {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
                } else {
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                Log.d(TAG, "Response body: $responseBody")

                handleServerResponse(responseCode, responseBody, name, description, latitude, longitude, startDate, endDate, isPublic)
                connection.disconnect()
            } catch (e: IOException) {
                Log.e(TAG, "Network error creating session", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateSessionActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                Log.e(TAG, "JSON error creating session", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateSessionActivity, "JSON error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createSessionJsonData(
        name: String,
        description: String,
        userId: String,
        latitude: Double,
        longitude: Double,
        startDate: Date,
        endDate: Date,
        isPublic: Boolean,
        subject: String,
        faculty: String,
        year: Int,
        invitees: ArrayList<String>
    ): JSONObject {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return JSONObject().apply {
            put("name", name)
            put("description", description)
            put("hostId", userId)
            put("location", JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().apply {
                    put(longitude)
                    put(latitude)
                })
            })
            put("dateRange", JSONObject().apply {
                put("startDate", dateFormat.format(startDate))
                put("endDate", dateFormat.format(endDate))
            })
            put("isPublic", isPublic)
            put("subject", subject)
            put("faculty", faculty)
            put("year", year)
            put("invitees", JSONArray().apply {
                invitees.forEach { friendId ->
                    put(friendId)
                }
            })
        }
    }

    private suspend fun handleServerResponse(
        responseCode: Int,
        responseBody: String,
        name: String,
        description: String,
        latitude: Double,
        longitude: Double,
        startDate: Date,
        endDate: Date,
        isPublic: Boolean
    ) {
        withContext(Dispatchers.Main) {
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val responseJson = JSONObject(responseBody)
                val sessionJson = responseJson.optJSONObject("session")

                if (sessionJson != null) {
                    val sessionLocation = "${latitude.toString().take(7)}, ${longitude.toString().take(7)}"
                    val sessionTime = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                        .format(startDate) + " - " +
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endDate)

                    val session = Session(
                        name = name,
                        time = sessionTime,
                        location = sessionLocation,
                        description = description,
                        visibility = if (isPublic) SessionVisibility.PUBLIC else SessionVisibility.PRIVATE
                    )

                    setResult(RESULT_OK, Intent().apply {
                        putExtra("SESSION_NAME", session.name)
                        putExtra("SESSION_TIME", session.time)
                        putExtra("SESSION_LOCATION", session.location)
                        putExtra("SESSION_DESCRIPTION", session.description)
                        putExtra("SESSION_VISIBILITY", session.visibility)
                    })

                    Toast.makeText(this@CreateSessionActivity, "Session created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateSessionActivity, "Unexpected server response format", Toast.LENGTH_SHORT).show()
                }
            } else {
                try {
                    val errorJson = JSONObject(responseBody)
                    val errorMessage = errorJson.optString("message", "Failed to create session")
                    Toast.makeText(this@CreateSessionActivity, errorMessage, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    Toast.makeText(this@CreateSessionActivity, "Failed to create session: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }
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
                findViewById<TextInputEditText>(R.id.sessionLocationInput).setText("Lat: ${lat.toString().take(7)}, Lng: ${lng.toString().take(7)}")
            }
        }
        else if (requestCode == FRIEND_SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get all the session data and selected friends
            val name = data?.getStringExtra("SESSION_NAME") ?: ""
            val description = data?.getStringExtra("SESSION_DESCRIPTION") ?: ""
            val latitude = data?.getDoubleExtra("SESSION_LATITUDE", 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra("SESSION_LONGITUDE", 0.0) ?: 0.0
            val startDate = Date(data?.getLongExtra("SESSION_START_DATE", 0L) ?: 0L)
            val endDate = Date(data?.getLongExtra("SESSION_END_DATE", 0L) ?: 0L)
            val subject = data?.getStringExtra("SESSION_SUBJECT") ?: ""
            val faculty = data?.getStringExtra("SESSION_FACULTY") ?: ""
            val year = data?.getIntExtra("SESSION_YEAR", 1) ?: 1

            // Get selected friend IDs
            val selectedFriendIds = data?.getStringArrayExtra("SELECTED_FRIEND_IDS")?.toList() ?: listOf()

            // Create the session with selected invitees
            createSessionOnServer(
                name = name,
                description = description,
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
                isPublic = false,
                subject = subject,
                faculty = faculty,
                year = year,
                invitees = ArrayList(selectedFriendIds)
            )
        }
    }
}