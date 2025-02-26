package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionsAdapter : RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {
    private val allSessions = mutableListOf<Session>()
    private val displayedSessions = mutableListOf<Session>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(displayedSessions[position])
    }

    override fun getItemCount(): Int = displayedSessions.size

    fun addSession(session: Session) {
        allSessions.add(session)
        if (session.visibility == currentVisibility) {
            displayedSessions.add(session)
            notifyItemInserted(displayedSessions.size - 1)
        }
    }

    fun filterSessions(visibility: SessionVisibility) {
        currentVisibility = visibility
        displayedSessions.clear()
        displayedSessions.addAll(allSessions.filter { it.visibility == visibility })
        notifyDataSetChanged()
    }

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.sessionName)
        private val timeTextView: TextView = itemView.findViewById(R.id.sessionTime)
        private val locationTextView: TextView = itemView.findViewById(R.id.sessionLocation)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.sessionDescription)

        fun bind(session: Session) {
            nameTextView.text = session.name
            timeTextView.text = session.time
            locationTextView.text = session.location
            descriptionTextView.text = session.description
        }
    }

    companion object {
        private var currentVisibility: SessionVisibility = SessionVisibility.PRIVATE
    }
}