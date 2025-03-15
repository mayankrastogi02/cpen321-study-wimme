package com.cpen321.study_wimme

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EditGroupActivity : AppCompatActivity() {

    private val TAG = "EditGroupActivity"
    private lateinit var group: Group
    private lateinit var friendList: ArrayList<Friend>
    private val friendsMap = mutableMapOf<Friend, Boolean>()
    private lateinit var groupMembersRecyclerView: RecyclerView
    private lateinit var saveChangesButton: Button
    private lateinit var adapter: EditFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)

        group = intent.getSerializableExtra("group") as Group
        friendList = intent.getSerializableExtra("friends") as ArrayList<Friend>

        Log.d(TAG, "$group")
        Log.d(TAG, "$friendList")

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val deleteButton: ImageButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            deleteGroup(group.id)
            finish()
        }

        // Initialize the map with the current selections
        friendList.forEach { friend ->
            friendsMap[friend] = group.members.any { it.id == friend.id }
        }

        adapter = EditFriendAdapter(friendsMap) { friend, isSelected ->
            friendsMap[friend] = isSelected
        }

        val groupNameTextView: TextView = findViewById(R.id.groupNameTextView)
        groupNameTextView.text = group.name

        groupMembersRecyclerView = findViewById(R.id.groupMembersRecyclerView)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        // Initialize RecyclerView and Adapter with group members
        groupMembersRecyclerView.layoutManager = LinearLayoutManager(this)

        groupMembersRecyclerView.adapter = adapter

        saveChangesButton.setOnClickListener {
            // Implement save changes logic here
            val selectedFriends = friendsMap.filterValues { it }.keys.toList()
            saveGroup(group.id, selectedFriends)

            Log.d(TAG, "$selectedFriends")

            finish()
        }
    }

    private fun saveGroup(groupId: String, members: List<Friend>) {
        val memberIdsList = members.map { it.id }
        val memberIdsJSONArray = JSONArray(memberIdsList)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/group/${groupId}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("members", memberIdsJSONArray)
                }

                Log.d(TAG, "$memberIdsList")
                Log.d(TAG, "$jsonData")

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@EditGroupActivity, "Group updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditGroupActivity, "Failed to update group", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error editing group", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGroupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteGroup(groupId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/group/${groupId}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@EditGroupActivity, "Group deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditGroupActivity, "Failed to delete group", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error deleting group", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGroupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}