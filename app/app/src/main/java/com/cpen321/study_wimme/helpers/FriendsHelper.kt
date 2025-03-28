package com.cpen321.study_wimme.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cpen321.study_wimme.Friend
import com.cpen321.study_wimme.Group
import com.cpen321.study_wimme.GroupMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object FriendsHelper {
    private const val TAG = "FriendsHelper"

    fun parseFriends(friendsArray: JSONArray): ArrayList<Friend> {
        val fetchedFriends = ArrayList<Friend>()
        for (i in 0 until friendsArray.length()) {
            val friendObj = friendsArray.getJSONObject(i)
            val friend = Friend(
                friendObj.getString("_id"),
                friendObj.getString("userName"),
                friendObj.getString("firstName"),
                friendObj.getString("lastName"),
                friendObj.optString("year", ""),
                friendObj.optString("faculty", ""),
                friendObj.optString("interests", "")
            )
            fetchedFriends.add(friend)
        }
        return fetchedFriends
    }

    fun parseGroups(groupsArray: JSONArray): ArrayList<Group> {
        val fetchedGroups = ArrayList<Group>()
        for (i in 0 until groupsArray.length()) {
            val groupObj = groupsArray.getJSONObject(i)
            val membersArray = groupObj.getJSONArray("members")
            val membersArrayList = ArrayList<GroupMember>()

            for (j in 0 until membersArray.length()) {
                val memberObj = membersArray.getJSONObject(j)
                val member = GroupMember(
                    memberObj.getString("_id"),
                    memberObj.getString("userName")
                )
                membersArrayList.add(member)
            }

            val group = Group(
                groupObj.getString("_id"),
                groupObj.getString("name"),
                membersArrayList
            )
            fetchedGroups.add(group)
        }
        return fetchedGroups
    }

    fun fetchUserIdFromGoogleId(context: Context, googleId: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/auth/verify?googleId=$googleId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d(TAG, "Verify API response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonResponse = JSONObject(response.toString())

                    if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("_id")) {
                        val mongoUserId = jsonResponse.getJSONObject("data").getString("_id")

                        val sharedPreferences = context.getSharedPreferences(
                            "user_prefs",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        val editor = sharedPreferences.edit()
                        editor.putString("userId", mongoUserId)
                        editor.apply()

                        Log.d(TAG, "Saved MongoDB user ID: $mongoUserId")
                        withContext(Dispatchers.Main) {
                            callback(mongoUserId)
                        }
                    } else {
                        Log.e(TAG, "MongoDB user ID not found in response")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_SHORT).show()
                            callback(null)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to verify user")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to verify user", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error fetching user ID", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
        }
    }
}
