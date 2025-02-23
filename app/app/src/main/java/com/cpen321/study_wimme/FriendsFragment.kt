package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup

class FriendsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter // Use a common adapter for both Friends and Groups
    private lateinit var friendList: ArrayList<Friend>
    private lateinit var groupList: ArrayList<Group>
    private lateinit var addFriendEditText: EditText
    private lateinit var addFriendButton: Button
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.friendsRecyclerView)
        addFriendEditText = view.findViewById(R.id.addFriendEditText)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        visibilityToggleGroup = view.findViewById(R.id.friendsGroupsToggle)

        // Initialize data
        friendList = ArrayList()
        friendList.add(Friend("alice02", "Alice", "Truman", "Freshman", "Computer Science", "Android Development"))
        friendList.add(Friend("bob01", "Bob", "Truman", "Sophomore", "Computer Science", "Web Development"))
        friendList.add(Friend("xcxx01", "Charlie", "Truman", "Junior", "Computer Science", "Machine Learning"))

        groupList = ArrayList()
        groupList.add(Group("Group 1", "Description 1"))
        groupList.add(Group("Group 2", "Description 2"))

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FriendAdapter(friendList) { item ->
            // Handle item click (either Friend or Group)
            if (item is Friend) {
                val intent = Intent(context, FriendsInfoActivity::class.java)
                intent.putExtra("friend", item)
                startActivity(intent)
            } else if (item is Group) {
                // Handle group click (if needed)
                Toast.makeText(context, "Group Clicked: ${ (item as Group).name }", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        // Set up add friend button
        addFriendButton.setOnClickListener {
            val username = addFriendEditText.text.toString()
            if (username.isNotEmpty()) {
                // Implement your logic to add a friend (e.g., API call)
                Toast.makeText(context, "Adding friend: $username", Toast.LENGTH_SHORT).show()
                addFriendEditText.text.clear()
            } else {
                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up visibility toggle group
        visibilityToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.friendsButton -> {
                        updateRecyclerView(friendList)
                    }
                    R.id.groupsButton -> {
                        updateRecyclerView(groupList)
                    }
                }
            }
        }

        // Set initial visibility
        visibilityToggleGroup.check(R.id.friendsButton)

        return view
    }

    private fun updateRecyclerView(newList: List<Any>) {
        adapter = FriendAdapter(newList) { item ->
            if (item is Friend) {
                val intent = Intent(context, FriendsInfoActivity::class.java)
                intent.putExtra("friend", item)
                startActivity(intent)
            } else if (item is Group) {
                // Handle group click (if needed)
                Toast.makeText(context, "Group Clicked: ${ (item as Group).name }", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}