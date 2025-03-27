package com.cpen321.study_wimme.helpers

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object UserAccountHelper {
    private const val TAG = "UserAccountHelper"

    data class Response(val isSuccessful: Boolean, val errorMessage: String? = null)

    fun deleteAccount(userId: String, serverUrl: String): Response {
        return try {
            val url = URL("$serverUrl/user")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonData = JSONObject().apply {
                put("userId", userId)
            }

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonData.toString())
            writer.flush()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Response(true)
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errorResponse = errorStream.bufferedReader().use { it.readText() }
                val jsonError = JSONObject(errorResponse)
                val errorMessage = jsonError.optString("message", "Failed to delete account")
                Response(false, errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Response(false, e.message)
        }
    }

    suspend fun updateUserProfile(googleId: String, userProfileData: JSONObject, serverUrl: String): Int {
        val url = URL("$serverUrl/auth/profile/$googleId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val outputStream = OutputStreamWriter(connection.outputStream)
        outputStream.write(userProfileData.toString())
        outputStream.flush()

        val responseCode = connection.responseCode
        connection.disconnect()
        return responseCode
    }
}
