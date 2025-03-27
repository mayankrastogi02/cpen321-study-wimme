package com.cpen321.study_wimme.helpers

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.cpen321.study_wimme.CreateSessionActivity
import com.cpen321.study_wimme.SessionVisibility
import com.cpen321.study_wimme.models.Session
import com.cpen321.study_wimme.models.SessionDetails
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object SessionHelper {
    private const val TAG = "SessionHelper"

    fun createSessionJsonData(sessionDetails: SessionDetails, userId: String): JSONObject {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return JSONObject().apply {
            put("name", sessionDetails.name)
            put("description", sessionDetails.description)
            put("hostId", userId)
            put("location", JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().apply {
                    put(sessionDetails.longitude)
                    put(sessionDetails.latitude)
                })
            })
            put("dateRange", JSONObject().apply {
                put("startDate", dateFormat.format(sessionDetails.startDate))
                put("endDate", dateFormat.format(sessionDetails.endDate))
            })
            put("isPublic", sessionDetails.isPublic)
            put("subject", sessionDetails.subject)
            put("faculty", sessionDetails.faculty)
            put("year", sessionDetails.year)
            put("invitees", JSONArray().apply {
                sessionDetails.invitees.forEach { friendId ->
                    put(friendId)
                }
            })
        }
    }

    suspend fun handleServerResponse(
        context: Context,
        responseCode: Int,
        responseBody: String,
        sessionDetails: SessionDetails
    ) {
        with(context as CreateSessionActivity) {
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val responseJson = JSONObject(responseBody)
                val sessionJson = responseJson.optJSONObject("session")

                if (sessionJson != null) {
                    val sessionLocation = "${sessionDetails.latitude.toString().take(7)}, ${sessionDetails.longitude.toString().take(7)}"
                    val sessionTime = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                        .format(sessionDetails.startDate) + " - " +
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(sessionDetails.endDate)

                    val session = Session(
                        name = sessionDetails.name,
                        time = sessionTime,
                        location = sessionLocation,
                        description = sessionDetails.description,
                        visibility = if (sessionDetails.isPublic) SessionVisibility.PUBLIC else SessionVisibility.PRIVATE
                    )

                    setResult(RESULT_OK, Intent().apply {
                        putExtra("SESSION_NAME", session.name)
                        putExtra("SESSION_TIME", session.time)
                        putExtra("SESSION_LOCATION", session.location)
                        putExtra("SESSION_DESCRIPTION", session.description)
                        putExtra("SESSION_VISIBILITY", session.visibility)
                    })

                    Toast.makeText(this, "Session created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Unexpected server response format", Toast.LENGTH_SHORT).show()
                }
            } else {
                try {
                    val errorJson = JSONObject(responseBody)
                    val errorMessage = errorJson.optString("message", "Failed to create session")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    Toast.makeText(this, "Failed to create session: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
