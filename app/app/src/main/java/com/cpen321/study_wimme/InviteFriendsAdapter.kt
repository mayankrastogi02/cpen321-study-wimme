package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InviteFriendsAdapter(
    private val friends: List<Friend>,
    private val onFriendSelectionChanged: (Friend, Boolean) -> Unit
) : RecyclerView.Adapter<InviteFriendsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.friendNameTextView)
        val usernameTextView: TextView = view.findViewById(R.id.friendUsernameTextView)
        val checkBox: CheckBox = view.findViewById(R.id.friendCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_select_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]

        holder.nameTextView.text = "${friend.firstName} ${friend.lastName}"
        holder.usernameTextView.text = friend.username

        // Handle checkbox state
        holder.checkBox.setOnCheckedChangeListener(null) // Clear previous listener
        holder.checkBox.isChecked = false

        // Set click listener for both the item view and checkbox
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            onFriendSelectionChanged(friend, holder.checkBox.isChecked)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onFriendSelectionChanged(friend, isChecked)
        }
    }

    override fun getItemCount() = friends.size
}