package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class InviteFriendsActivity : AppCompatActivity() {

    private val TAG = "InviteFriendsActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var continueButton: Button
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private val selectedFriends = mutableSetOf<Friend>() // Changed to Set to avoid duplicates
    private var friendsList = ArrayList<Friend>()
    private var groupsList = ArrayList<Group>()
    private var currentMode = DisplayMode.FRIENDS

    // Define display modes
    enum class DisplayMode {
        FRIENDS, GROUPS
    }

    // Session data passed from CreateSessionActivity
    private lateinit var sessionName: String
    private lateinit var sessionDescription: String
    private var sessionLatitude: Double = 0.0
    private var sessionLongitude: Double = 0.0
    private var sessionStartDate: Long = 0L
    private var sessionEndDate: Long = 0L
    private var isPublic: Boolean = false
    private lateinit var sessionSubject: String
    private lateinit var sessionFaculty: String
    private var sessionYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_friends)

        // Get session data from intent
        sessionName = intent.getStringExtra("SESSION_NAME") ?: ""
        sessionDescription = intent.getStringExtra("SESSION_DESCRIPTION") ?: ""
        sessionLatitude = intent.getDoubleExtra("SESSION_LATITUDE", 0.0)
        sessionLongitude = intent.getDoubleExtra("SESSION_LONGITUDE", 0.0)
        sessionStartDate = intent.getLongExtra("SESSION_START_DATE", 0L)
        sessionEndDate = intent.getLongExtra("SESSION_END_DATE", 0L)
        isPublic = intent.getBooleanExtra("SESSION_IS_PUBLIC", false)
        sessionSubject = intent.getStringExtra("SESSION_SUBJECT") ?: ""
        sessionFaculty = intent.getStringExtra("SESSION_FACULTY") ?: ""
        sessionYear = intent.getIntExtra("SESSION_YEAR", 1)

        recyclerView = findViewById(R.id.friendsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        continueButton = findViewById(R.id.continueButton)
        toggleGroup = findViewById(R.id.inviteToggleGroup)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Set up continue button
        continueButton.setOnClickListener {
            continueWithSelectedFriends()
        }

        // Setup toggle group
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsButton -> {
                        currentMode = DisplayMode.FRIENDS
                        showFriends()
                    }
                    R.id.groupsButton -> {
                        currentMode = DisplayMode.GROUPS
                        showGroups()
                    }
                }
            }
        }

        // Default to friends view
        toggleGroup.check(R.id.friendsButton)

        // Fetch data
        fetchFriends()
        fetchGroups()
    }

    private fun showFriends() {
        if (friendsList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.text = "You don't have any friends yet.\nAdd friends to invite them to your session."
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            setupFriendsAdapter(friendsList)
        }
    }

    private fun showGroups() {
        if (groupsList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.text = "You don't have any groups yet.\nCreate groups in the Friends tab to quickly invite multiple friends."
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
            setupGroupsAdapter(groupsList)
        }
    }

    private fun fetchFriends() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.GONE

        val userId = LoginActivity.getCurrentUserId(this)

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/user/friends?userId=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val fetchedFriends = parseFriendsResponse(response)

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        friendsList = fetchedFriends

                        if (currentMode == DisplayMode.FRIENDS) {
                            showFriends()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@InviteFriendsActivity,
                            "Failed to fetch friends",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error fetching friends", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@InviteFriendsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun parseFriendsResponse(response: String): ArrayList<Friend> {
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
        return fetchedFriends
    }

    private fun fetchGroups() {
        val userId = LoginActivity.getCurrentUserId(this)

        if (userId == null) {
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
                        val groupMembers = ArrayList<GroupMember>()

                        for (j in 0 until membersArray.length()) {
                            val memberObj = membersArray.getJSONObject(j)
                            val member = GroupMember(
                                memberObj.getString("_id"),
                                memberObj.getString("userName")
                            )
                            groupMembers.add(member)
                        }

                        val group = Group(
                            groupObj.getString("_id"),
                            groupObj.getString("name"),
                            groupMembers
                        )
                        fetchedGroups.add(group)
                    }

                    withContext(Dispatchers.Main) {
                        groupsList = fetchedGroups

                        if (currentMode == DisplayMode.GROUPS) {
                            showGroups()
                        }
                    }
                }
                connection.disconnect()
            } catch (e: JSONException) {
                Log.e(TAG, "Error fetching groups", e)
            }
        }
    }

    private fun setupFriendsAdapter(friends: List<Friend>) {
        val adapter = InviteFriendsAdapter(friends) { friend, isSelected ->
            if (isSelected) {
                selectedFriends.add(friend)
            } else {
                selectedFriends.remove(friend)
            }
            updateContinueButtonText()
        }
        recyclerView.adapter = adapter
    }

    private fun setupGroupsAdapter(groups: List<Group>) {
        val adapter = InviteGroupsAdapter(groups, friendsList) { groupMembers, isSelected ->
            if (isSelected) {
                // Add all friends in the group to selected friends
                selectedFriends.addAll(groupMembers)
            } else {
                // Remove all friends in the group from selected friends
                selectedFriends.removeAll(groupMembers)
            }
            updateContinueButtonText()
        }
        recyclerView.adapter = adapter
    }

    private fun updateContinueButtonText() {
        continueButton.text = if (selectedFriends.isEmpty()) {
            "Continue without invites"
        } else {
            "Invite ${selectedFriends.size} friend${if (selectedFriends.size > 1) "s" else ""}"
        }
    }

    private fun continueWithSelectedFriends() {
        // Return selected friend IDs to CreateSessionActivity
        val intent = Intent()
        val friendIds = selectedFriends.map { it.id }.toTypedArray()

        // Pass back session data and selected friends
        intent.putExtra("SELECTED_FRIEND_IDS", friendIds)
        intent.putExtra("SESSION_NAME", sessionName)
        intent.putExtra("SESSION_DESCRIPTION", sessionDescription)
        intent.putExtra("SESSION_LATITUDE", sessionLatitude)
        intent.putExtra("SESSION_LONGITUDE", sessionLongitude)
        intent.putExtra("SESSION_START_DATE", sessionStartDate)
        intent.putExtra("SESSION_END_DATE", sessionEndDate)
        intent.putExtra("SESSION_IS_PUBLIC", isPublic)
        intent.putExtra("SESSION_SUBJECT", sessionSubject)
        intent.putExtra("SESSION_FACULTY", sessionFaculty)
        intent.putExtra("SESSION_YEAR", sessionYear)

        setResult(RESULT_OK, intent)
        finish()
    }
}