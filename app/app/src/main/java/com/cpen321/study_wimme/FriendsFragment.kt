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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/user/friends?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
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

    private fun fetchGroups() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/group?userId=$userId")
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
                        val group = Group(
                            groupObj.getString("_id"),
                            groupObj.getString("name"),
                            groupObj.optString("description", "")
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
                val url = URL("${BuildConfig.SERVER_URL}/api/group")
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
