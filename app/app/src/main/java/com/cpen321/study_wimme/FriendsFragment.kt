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
import com.cpen321.study_wimme.helpers.FriendsHelper
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
        val userId = LoginActivity.getCurrentUserId(requireActivity())
        Log.d(TAG, "Fetching friends with userId: $userId")

        if (userId == null) {
            val googleId = LoginActivity.getCurrentUserGoogleId(requireActivity())
            Log.d(TAG, "userId is null, googleId: $googleId")

            if (googleId != null) {
                FriendsHelper.fetchUserIdFromGoogleId(requireActivity(), googleId) { mongoUserId ->
                    if (mongoUserId != null) {
                        fetchFriendsFromServer(mongoUserId)
                    } else {
                        Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            return
        }

        fetchFriendsFromServer(userId)
    }

    private fun fetchFriendsFromServer(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/user/friends?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d(TAG, "Friends API response code: $responseCode")
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val friendsArray = jsonResponse.getJSONArray("friends")
                    val fetchedFriends = FriendsHelper.parseFriends(friendsArray)

                    withContext(Dispatchers.Main) {
                        updateFriendsList(fetchedFriends)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch friends", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error fetching friends", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFriendsList(fetchedFriends: ArrayList<Friend>) {
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

    private fun fetchGroups() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        fetchGroupsFromServer(userId)
    }

    private fun fetchGroupsFromServer(userId: String) {
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
                    val fetchedGroups = FriendsHelper.parseGroups(groupsArray)

                    withContext(Dispatchers.Main) {
                        updateGroupsList(fetchedGroups)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch groups", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error fetching groups", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateGroupsList(fetchedGroups: ArrayList<Group>) {
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
                val url = URL("${BuildConfig.SERVER_URL}/user/friendRequest")
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
            } catch (e: JSONException) {
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
            } catch (e: JSONException) {
                Log.e(TAG, "Error creating group", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}