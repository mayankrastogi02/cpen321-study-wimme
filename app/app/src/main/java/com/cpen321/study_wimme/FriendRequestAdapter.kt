package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(
    private val requests: List<FriendRequest>,
    private val onActionClick: (FriendRequest, Boolean) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.userNameTextView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]

        holder.userNameTextView.text = request.userName
        holder.nameTextView.text = "${request.firstName} ${request.lastName}"

        holder.acceptButton.setOnClickListener {
            onActionClick(request, true)
        }

        holder.rejectButton.setOnClickListener {
            onActionClick(request, false)
        }
    }

    override fun getItemCount() = requests.size
}