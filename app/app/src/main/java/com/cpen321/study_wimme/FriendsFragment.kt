package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup

class FriendsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendList: ArrayList<Friend> // Use your Friend data class
    private lateinit var groupList: ArrayList<Group> // Use your Group data class
    private lateinit var adapter: FriendAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup

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

        friendList = ArrayList()
        // Add some dummy data for now
        friendList.add(Friend("alice02", "Alice", "Truman", "Freshman", "Computer Science", "Android Development"))
        friendList.add(Friend("bob01", "Bob", "Truman", "Sophomore", "Computer Science", "Web Development"))
        friendList.add(Friend("xcxx01", "Charlie", "Truman", "Junior", "Computer Science", "Machine Learning"))

        groupList = ArrayList()
        groupList.add(Group("Study Group 1", "Studying for CPEN 321"))
        groupList.add(Group("Study Group 2", "Studying for CPEN 421"))

        adapter = FriendAdapter(friendList) { item ->
            // Handle item click
            when (item) {
                is Friend -> {
                    val intent = Intent(context, FriendsInfoActivity::class.java)
                    intent.putExtra("friend", item) // Pass the Friend object to the activity
                    startActivity(intent)
                }
                is Group -> {
                    // Handle group click
                    Toast.makeText(context, "Clicked on group: ${item.name}", Toast.LENGTH_SHORT).show()
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
                        // Switch to friends view
                        showAddFriendLayout()
                        hideCreateGroupLayout()
                        adapter = FriendAdapter(friendList) { item ->
                            // Handle item click
                            when (item) {
                                is Friend -> {
                                    val intent = Intent(context, FriendsInfoActivity::class.java)
                                    intent.putExtra("friend", item) // Pass the Friend object to the activity
                                    startActivity(intent)
                                }
                            }
                        }
                        recyclerView.adapter = adapter
                        // Toast.makeText(context, "Switching to Friends", Toast.LENGTH_SHORT).show()
                        // Update the RecyclerView with friends data
                    }
                    R.id.groupsButton -> {
                        // Switch to groups view
                        hideAddFriendLayout()
                        showCreateGroupLayout()
                        adapter = FriendAdapter(groupList) { item ->
                            // Handle item click
                            when (item) {
                                is Group -> {
                                    // Handle group click
                                    val intent = Intent(context, EditGroupActivity::class.java)
                                    intent.putExtra("group", item)
                                    startActivity(intent)
                                    Toast.makeText(context, "Clicked on group: ${item.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        recyclerView.adapter = adapter
                        // Toast.makeText(context, "Switching to Groups", Toast.LENGTH_SHORT).show()
                        // Update the RecyclerView with groups data
                    }
                }
            }
        }

        visibilityToggleGroup.check(R.id.friendsButton)

        return view
    }

    private fun showAddFriendLayout() {
        if (addFriendView == null) {
            addFriendView = addFriendStub?.inflate()
            addFriendEditText = addFriendView?.findViewById(R.id.addFriendEditText)
            addFriendButton = addFriendView?.findViewById(R.id.addFriendButton)

            addFriendButton?.setOnClickListener {
                val username = addFriendEditText?.text.toString()
                if (username.isNotEmpty()) {
                    // Implement your logic to add a friend (e.g., API call)
                    Toast.makeText(context, "Adding friend: $username", Toast.LENGTH_SHORT).show()
                    addFriendEditText?.text?.clear()
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
                    // Implement your logic to create a group (e.g., API call)
                    Toast.makeText(context, "Creating group: $groupName", Toast.LENGTH_SHORT).show()
                    createGroupEditText?.text?.clear()
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
}