package com.cpen321.study_wimme.helpers

import android.util.Log
import com.cpen321.study_wimme.Session
import com.cpen321.study_wimme.SessionVisibility
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object SessionParsingHelper {
    private const val TAG = "SessionParsingHelper"

    fun parseSessions(response: String): ArrayList<Session> {
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

    private fun formatLocation(latitude: Double, longitude: Double): String {
        return "Lat: ${latitude.toString().take(7)}, Lng: ${longitude.toString().take(7)}"
    }
}
