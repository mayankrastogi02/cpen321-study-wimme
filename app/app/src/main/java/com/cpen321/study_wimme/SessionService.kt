import android.util.Log
import com.cpen321.study_wimme.BuildConfig
import com.cpen321.study_wimme.NearbySessionsResponse
import com.cpen321.study_wimme.SessionDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SessionService {
    private val client = OkHttpClient()

    suspend fun fetchNearbySessions(
        latitude: Double,
        longitude: Double,
        radius: Double,
        userId: String
    ): FetchSessionsResult = withContext(Dispatchers.IO) {
        val url = "${BuildConfig.SERVER_URL}/session/nearbySessions?latitude=$latitude&longitude=$longitude&radius=$radius&userId=$userId"
        Log.d("SessionService", "Fetching sessions from URL: $url")
        val request = Request.Builder().url(url).get().build()
        try {
            val response = client.newCall(request).execute()
            Log.d("SessionService", "Response code: ${response.code}")
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                response.close()
                FetchSessionsResult(sessions = emptyList(), errorMessage = "Error ${response.code}: $errorBody. Please return to home screen and try again.")
            } else {
                val bodyString = response.body?.string() ?: ""
                response.close()
                val sessions = parseSessionsJson(bodyString)
                FetchSessionsResult(sessions = sessions)
            }
        } catch (e: Exception) {
            Log.e("SessionService", "Error fetching sessions", e)
            FetchSessionsResult(sessions = emptyList(), errorMessage = "Error occurred, please return to home screen and try again")
        }
    }

    suspend fun joinSession(sessionId: String, userId: String): JoinSessionResult = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("userId", userId) }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val joinUrl = "${BuildConfig.SERVER_URL}/session/$sessionId/join"
        Log.d("SessionService", "Join URL: $joinUrl")
        val request = Request.Builder().url(joinUrl).put(requestBody).build()
        try {
            val response = client.newCall(request).execute()
            return@withContext if (response.isSuccessful) {
                response.close()
                JoinSessionResult(true)
            } else {
                val errorBody = response.body?.string() ?: ""
                var errorMessage = "Failed to join session."
                if (errorBody.isNotEmpty()) {
                    try {
                        val errorJson = JSONObject(errorBody)
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("SessionService", "Error parsing error message", e)
                    }
                }
                response.close()
                JoinSessionResult(false, errorMessage)
            }
        } catch (e: Exception) {
            Log.e("SessionService", "Error joining session", e)
            return@withContext JoinSessionResult(false, "Error joining session: ${e.message}")
        }
    }

    suspend fun leaveSession(sessionId: String, userId: String): LeaveSessionResult = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("userId", userId) }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val leaveUrl = "${BuildConfig.SERVER_URL}/session/$sessionId/leave"
        Log.d("SessionService", "Leave URL: $leaveUrl")
        val request = Request.Builder().url(leaveUrl).put(requestBody).build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.close()
                LeaveSessionResult(true)
            } else {
                val errorBody = response.body?.string() ?: ""
                var errorMessage = "Failed to leave session."
                if (errorBody.isNotEmpty()) {
                    try {
                        val errorJson = JSONObject(errorBody)
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("SessionService", "Error parsing error message", e)
                    }
                }
                response.close()
                LeaveSessionResult(false, errorMessage)
            }
        } catch (e: Exception) {
            Log.e("SessionService", "Error leaving session", e)
            LeaveSessionResult(false, "Error leaving session: ${e.message}")
        }
    }

    suspend fun deleteSession(sessionId: String): DeleteSessionResult = withContext(Dispatchers.IO) {
        val deleteUrl = "${BuildConfig.SERVER_URL}/session/$sessionId"
        Log.d("SessionService", "Delete URL: $deleteUrl")
        val request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.close()
                DeleteSessionResult(true)
            } else {
                val errorBody = response.body?.string() ?: ""
                var errorMessage = "Failed to delete session."
                if (errorBody.isNotEmpty()) {
                    try {
                        val errorJson = JSONObject(errorBody)
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.getString("message")
                        }
                    } catch (e: Exception) {
                        Log.e("SessionService", "Error parsing error message", e)
                    }
                }
                response.close()
                DeleteSessionResult(false, errorMessage)
            }
        } catch (e: Exception) {
            Log.e("SessionService", "Error deleting session", e)
            DeleteSessionResult(false, "Error deleting session: ${e.message}")
        }
    }

    data class DeleteSessionResult(
        val success: Boolean,
        val errorMessage: String? = null
    )

    data class JoinSessionResult(
        val success: Boolean,
        val errorMessage: String? = null
    )

    data class LeaveSessionResult(
        val success: Boolean,
        val errorMessage: String? = null
    )

    data class FetchSessionsResult(
        val sessions: List<SessionDto>? = null,
        val errorMessage: String? = null
    )

    private fun parseSessionsJson(jsonString: String): List<SessionDto> {
        return try {
            val gson = Gson()
            val response = gson.fromJson(jsonString, NearbySessionsResponse::class.java)
            response.sessions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
