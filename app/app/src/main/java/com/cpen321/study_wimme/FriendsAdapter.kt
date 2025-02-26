package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(
    private val itemList: List<Any>,
    private val onItemClicked: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val FRIEND_VIEW_TYPE = 0
    private val GROUP_VIEW_TYPE = 1

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is Friend -> FRIEND_VIEW_TYPE
            is Group -> GROUP_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FRIEND_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.friends_item, parent, false)
                FriendViewHolder(view)
            }
            GROUP_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_item, parent, false)
                GroupViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            FRIEND_VIEW_TYPE -> {
                val friend = itemList[position] as Friend
                val friendViewHolder = holder as FriendViewHolder
                friendViewHolder.usernameTextView.text = friend.username
                friendViewHolder.firstNameTextView.text = friend.firstName
                friendViewHolder.lastNameTextView.text = friend.lastName

                friendViewHolder.itemView.setOnClickListener {
                    onItemClicked(friend)
                }
            }
            GROUP_VIEW_TYPE -> {
                val group = itemList[position] as Group
                val groupViewHolder = holder as GroupViewHolder
                groupViewHolder.groupNameTextView.text = group.name

                groupViewHolder.editGroupImageView.setOnClickListener {
                    onItemClicked(group)
                }

                groupViewHolder.itemView.setOnClickListener {
                    onItemClicked(group)
                }
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameView)
        val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameView)
        val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameView)
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView = itemView.findViewById(R.id.groupNameTextView)
        val editGroupImageView: ImageView =
            itemView.findViewById(R.id.editGroupImageView) // Add this line
    }
}