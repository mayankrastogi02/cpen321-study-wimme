package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FriendsFragment : Fragment() {

    private val TAG = "FriendsFragment"
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendList: ArrayList<Friend>
    private lateinit var groupList: ArrayList<Group>
    private lateinit var adapter: FriendAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var friendRequestsButton: ImageButton

    // Views for adding a friend
    private var addFriendStub: ViewStub? = null
    private var addFriendView: View? = null
    private var addFriendEditText: EditText? = null
    private var addFriendButton: Button? = null

    // Views for creating a group
    private var createGroupStub: ViewStub? = null
    private var createGroupView: View? = null
    private var createGroupEditText: EditText? = null
    private var createGroupButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        LoginActivity.logAllPreferences(requireActivity())

        recyclerView = view.findViewById(R.id.friendsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        friendRequestsButton = view.findViewById(R.id.friendRequestsButton)

        friendList = ArrayList()
        groupList = ArrayList()

        // Set up friend requests button
        friendRequestsButton.setOnClickListener {
            val intent = Intent(context, FriendRequestsActivity::class.java)
            startActivity(intent)
        }

        // Initialize adapter with empty list, will be updated when data is fetched
        adapter = FriendAdapter(friendList) { item ->
            when (item) {
                is Friend -> {
                    val intent = Intent(context, FriendsInfoActivity::class.java)
                    intent.putExtra("friend", item)
                    startActivity(intent)
                }
                is Group -> {
                    val intent = Intent(context, EditGroupActivity::class.java)
                    intent.putExtra("group", item)
                    startActivity(intent)
                }
            }
        }
        recyclerView.adapter = adapter

        visibilityToggleGroup = view.findViewById(R.id.friendsGroupsToggle)

        // Initialize ViewStubs
        addFriendStub = view.findViewById(R.id.addFriendStub)
        createGroupStub = view.findViewById(R.id.createGroupStub)

        visibilityToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsButton -> {
                        showAddFriendLayout()
                        hideCreateGroupLayout()
                        fetchFriends()
                    }
                    R.id.groupsButton -> {
                        hideAddFriendLayout()
                        showCreateGroupLayout()
                        fetchGroups()
                    }
                }
            }
        }

        visibilityToggleGroup.check(R.id.friendsButton)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        if (visibilityToggleGroup.checkedButtonId == R.id.friendsButton) {
            fetchFriends()
        } else {
            fetchGroups()
        }
    }

    private fun fetchFriends() {
        // Get userId using our new helper method
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        
        // Log the userId for debugging
        Log.d(TAG, "Fetching friends with userId: $userId")

        if (userId == null) {
            // If userId is null, try to get it from GoogleId
            val googleId = LoginActivity.getCurrentUserGoogleId(requireActivity())
            Log.d(TAG, "userId is null, googleId: $googleId")
            
            if (googleId != null) {
                // Try to fetch userId from backend using googleId
                fetchUserIdFromGoogleId(googleId)
            } else {
                Toast.makeText(context, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show()
                // Navigate back to login
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/user/friends?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d(TAG, "Friends API response code: $responseCode")
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val friendsArray = jsonResponse.getJSONArray("friends")
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

                    withContext(Dispatchers.Main) {
                        friendList.clear()
                        friendList.addAll(fetchedFriends)
                        adapter = FriendAdapter(friendList) { item ->
                            when (item) {
                                is Friend -> {
                                    val intent = Intent(context, FriendsInfoActivity::class.java)
                                    intent.putExtra("friend", item)
                                    startActivity(intent)
                                }
                            }
                        }
                        recyclerView.adapter = adapter
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch friends", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching friends", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Helper method to fetch userId using googleId
    private fun fetchUserIdFromGoogleId(googleId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/auth/verify?googleId=$googleId")
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

                    // Extract MongoDB user ID from response
                    if (jsonResponse.has("data") && jsonResponse.getJSONObject("data").has("_id")) {
                        val mongoUserId = jsonResponse.getJSONObject("data").getString("_id")

                        // Save MongoDB ID to SharedPreferences
                        val sharedPreferences = requireActivity().getSharedPreferences(
                            "user_prefs",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        val editor = sharedPreferences.edit()
                        editor.putString("userId", mongoUserId)
                        editor.apply()

                        Log.d(TAG, "Saved MongoDB user ID: $mongoUserId")

                        // Now fetch friends with the retrieved userId
                        withContext(Dispatchers.Main) {
                            fetchFriends()
                        }
                    } else {
                        Log.e(TAG, "MongoDB user ID not found in response")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to verify user")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to verify user", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user ID", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchGroups() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/group/${userId}")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val groupsArray = jsonResponse.getJSONArray("groups")
                    val fetchedGroups = ArrayList<Group>()

                    for (i in 0 until groupsArray.length()) {
                        val groupObj = groupsArray.getJSONObject(i)

                        val membersArray = groupObj.getJSONArray("members")
                        val membersArrayList = ArrayList<GroupMember>()

                        for (j in 0 until membersArray.length())  {
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

                    withContext(Dispatchers.Main) {
                        groupList.clear()
                        groupList.addAll(fetchedGroups)
                        adapter = FriendAdapter(groupList) { item ->
                            when (item) {
                                is Group -> {
                                    val intent = Intent(context, EditGroupActivity::class.java)
                                    intent.putExtra("group", item)
                                    intent.putExtra("friends", friendList)
                                    startActivity(intent)
                                }
                            }
                        }
                        recyclerView.adapter = adapter
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch groups", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching groups", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddFriendLayout() {
        if (addFriendView == null) {
            addFriendView = addFriendStub?.inflate()
            addFriendEditText = addFriendView?.findViewById(R.id.addFriendEditText)
            addFriendButton = addFriendView?.findViewById(R.id.addFriendButton)

            addFriendButton?.setOnClickListener {
                val username = addFriendEditText?.text.toString()
                if (username.isNotEmpty()) {
                    sendFriendRequest(username)
                } else {
                    Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            addFriendView?.visibility = View.VISIBLE
        }
    }

    private fun hideAddFriendLayout() {
        addFriendView?.visibility = View.GONE
    }

    private fun showCreateGroupLayout() {
        if (createGroupView == null) {
            createGroupView = createGroupStub?.inflate()
            createGroupEditText = createGroupView?.findViewById(R.id.createGroupEditText)
            createGroupButton = createGroupView?.findViewById(R.id.createGroupButton)

            createGroupButton?.setOnClickListener {
                val groupName = createGroupEditText?.text.toString()
                if (groupName.isNotEmpty()) {
                    createGroup(groupName)
                } else {
                    Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            createGroupView?.visibility = View.VISIBLE
        }
    }

    private fun hideCreateGroupLayout() {
        createGroupView?.visibility = View.GONE
    }

    private fun sendFriendRequest(username: String) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/user/friendRequest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("friendUserName", username)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Friend request sent to $username", Toast.LENGTH_SHORT).show()
                        addFriendEditText?.text?.clear()
                    } else {
                        // Read error message from response
                        val errorStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
                        val errorResponse = errorStream.bufferedReader().use { it.readText() }
                        val errorJson = JSONObject(errorResponse)
                        val errorMessage = errorJson.optString("message", "Failed to send friend request")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending friend request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createGroup(groupName: String) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/group")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("name", groupName)
                    put("members", arrayOf(userId))
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonData.toString())
                writer.flush()

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(context, "Group created: $groupName", Toast.LENGTH_SHORT).show()
                        createGroupEditText?.text?.clear()
                        // Refresh groups list
                        fetchGroups()
                    } else {
                        Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating group", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
