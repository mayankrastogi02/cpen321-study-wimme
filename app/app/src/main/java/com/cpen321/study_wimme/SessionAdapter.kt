package com.cpen321.study_wimme

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {
    private val allSessions = mutableListOf<Session>()
    private val displayedSessions = mutableListOf<Session>()
    private var recommendedSessionIds = setOf<String>() // Store IDs of recommended sessions

    private var currentVisibility = SessionVisibility.PRIVATE
    private var onSessionClickListener: ((Session) -> Unit)? = null

    inner class SessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val sessionNameTextView: TextView = view.findViewById(R.id.sessionNameTextView)
        private val sessionTimeTextView: TextView = view.findViewById(R.id.sessionTimeTextView)
        private val sessionLocationTextView: TextView = view.findViewById(R.id.sessionLocationTextView)
        private val sessionDescriptionTextView: TextView = view.findViewById(R.id.sessionDescriptionTextView)
        private val recommendedStarIcon: ImageView = view.findViewById(R.id.recommendedStarIcon)


        fun bind(session: Session) {
            sessionNameTextView.text = session.name
            sessionTimeTextView.text = session.time
            sessionLocationTextView.text = session.location
            sessionDescriptionTextView.text = session.description
            
            // Show star icon if the session is recommended
            Log.d("SessionAdapter", "Session ID: ${session.id}, Recommended: ${recommendedSessionIds.contains(session.id)}")
            if (recommendedSessionIds.contains(session.id)) {
                Log.d("SessionAdapter", "Session ${session.name} is recommended, TOGGLING ICON")
            }
             recommendedStarIcon.visibility = if (recommendedSessionIds.contains(session.id)) {
                 View.VISIBLE
             } else {
                 View.GONE
             }

            // Set click listener for the entire item
            itemView.setOnClickListener {
                onSessionClickListener?.invoke(session)
            }
        }
    }

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

    fun updateSessions(newSessions: List<Session>, recommendedIds: Set<String>) {
        Log.d("SessionAdapter", "Updating sessions: ${newSessions.size} sessions")

        allSessions.clear()
        allSessions.addAll(newSessions)

        displayedSessions.clear()
        displayedSessions.addAll(newSessions)

        recommendedSessionIds = recommendedIds
        Log.d("SessionAdapter", "Recommended session IDs: $recommendedSessionIds")
        Log.d("SessionAdapter", "Displayed sessions: ${displayedSessions.size}")
        notifyDataSetChanged()
    }

    // Method to set click listener
    fun setOnSessionClickListener(listener: (Session) -> Unit) {
        this.onSessionClickListener = listener
    }
}