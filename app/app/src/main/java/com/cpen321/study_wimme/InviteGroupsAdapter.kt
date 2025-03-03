package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InviteGroupsAdapter(
    private val groups: List<Group>,
    private val allFriends: List<Friend>,
    private val onGroupSelectionChanged: (List<Friend>, Boolean) -> Unit
) : RecyclerView.Adapter<InviteGroupsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupNameTextView: TextView = view.findViewById(R.id.groupNameTextView)
        val memberCountTextView: TextView = view.findViewById(R.id.memberCountTextView)
        val checkBox: CheckBox = view.findViewById(R.id.groupCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_select_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        
        holder.groupNameTextView.text = group.name
        holder.memberCountTextView.text = "${group.members.size} members"
        
        // Convert group member IDs to Friend objects
        val friendsInGroup = getFriendsInGroup(group)
        
        // Handle checkbox state
        holder.checkBox.setOnCheckedChangeListener(null) // Clear previous listener
        holder.checkBox.isChecked = false
        
        // Set click listener for both the item view and checkbox
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            onGroupSelectionChanged(friendsInGroup, holder.checkBox.isChecked)
        }
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onGroupSelectionChanged(friendsInGroup, isChecked)
        }
    }
    
    private fun getFriendsInGroup(group: Group): List<Friend> {
        // Map group member IDs to actual Friend objects
        return allFriends.filter { friend ->
            group.members.any { member -> member.id == friend.id }
        }
    }

    override fun getItemCount() = groups.size
}