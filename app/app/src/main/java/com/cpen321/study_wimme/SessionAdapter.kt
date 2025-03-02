package com.cpen321.study_wimme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {
    private val allSessions = mutableListOf<Session>()
    private val displayedSessions = mutableListOf<Session>()
    private var currentVisibility = SessionVisibility.PRIVATE
    private var onSessionClickListener: ((Session) -> Unit)? = null

    inner class SessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val sessionNameTextView: TextView = view.findViewById(R.id.sessionNameTextView)
        private val sessionTimeTextView: TextView = view.findViewById(R.id.sessionTimeTextView)
        private val sessionLocationTextView: TextView = view.findViewById(R.id.sessionLocationTextView)
        private val sessionDescriptionTextView: TextView = view.findViewById(R.id.sessionDescriptionTextView)

        fun bind(session: Session) {
            sessionNameTextView.text = session.name
            sessionTimeTextView.text = session.time
            sessionLocationTextView.text = session.location
            sessionDescriptionTextView.text = session.description
            
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

    fun updateSessions(newSessions: List<Session>) {
        allSessions.clear()
        allSessions.addAll(newSessions)
        
        displayedSessions.clear()
        displayedSessions.addAll(newSessions.filter { it.visibility == currentVisibility })
        notifyDataSetChanged()
    }

    // Method to set click listener
    fun setOnSessionClickListener(listener: (Session) -> Unit) {
        this.onSessionClickListener = listener
    }
}