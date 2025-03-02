package com.cpen321.study_wimme

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

class EditFriendAdapter(
    private val friendsMap: MutableMap<Friend, Boolean>,
    private val onSelectionChanged: (Friend, Boolean) -> Unit
) : RecyclerView.Adapter<EditFriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.usernameView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.friend_checkbox)

        fun bind(friend: Friend, isSelected: Boolean) {
            nameTextView.text = friend.username
            checkBox.isChecked = isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(friend, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checkable_friend_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsMap.keys.toList()[position]
        holder.bind(friend, friendsMap[friend] ?: false)
    }

    override fun getItemCount(): Int = friendsMap.size
}