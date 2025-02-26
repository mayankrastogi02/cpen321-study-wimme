package com.cpen321.study_wimme

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditGroupActivity : AppCompatActivity() {

    private lateinit var group: Group
    private lateinit var groupMembersRecyclerView: RecyclerView
    private lateinit var searchFriendEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var adapter: FriendAdapter // Assuming you can reuse FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        group = intent.getSerializableExtra("group") as Group

        val groupNameTextView: TextView = findViewById(R.id.groupNameTextView)
        groupNameTextView.text = group.name

        groupMembersRecyclerView = findViewById(R.id.groupMembersRecyclerView)
        searchFriendEditText = findViewById(R.id.searchFriendEditText)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        // Initialize RecyclerView and Adapter with group members
        groupMembersRecyclerView.layoutManager = LinearLayoutManager(this)
        //adapter = FriendAdapter(getGroupMembers(group)) { friend ->
        // Handle remove friend logic here
        //}
        //groupMembersRecyclerView.adapter = adapter

        saveChangesButton.setOnClickListener {
            // Implement save changes logic here
            Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Function to get group members (replace with your actual implementation)
    //private fun getGroupMembers(group: Group): List<Friend> {
    //  // Replace with your actual implementation to fetch group members
    //  return listOf()
    //}
}