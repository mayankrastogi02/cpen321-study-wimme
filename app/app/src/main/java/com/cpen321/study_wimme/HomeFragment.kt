package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var sessionsAdapter: SessionsAdapter
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var profileIcon: ImageView
    private lateinit var addSessionFab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView)
        visibilityToggleGroup = view.findViewById(R.id.visibilityToggleGroup)
        visibilityToggleGroup.check(R.id.privateButton)
        profileIcon = view.findViewById(R.id.profileIcon)
        addSessionFab = view.findViewById(R.id.addSessionFab)

        // Set up RecyclerView
        sessionsRecyclerView.layoutManager = LinearLayoutManager(context)
        sessionsAdapter = SessionsAdapter()
        sessionsRecyclerView.adapter = sessionsAdapter

        // Set up visibility toggle group
        visibilityToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val visibility = when (checkedId) {
                    R.id.privateButton -> SessionVisibility.PRIVATE
                    R.id.publicButton -> SessionVisibility.PUBLIC
                    else -> SessionVisibility.PRIVATE
                }
                sessionsAdapter.filterSessions(visibility)
            }
        }

        // Set initial visibility
        visibilityToggleGroup.check(R.id.privateButton)

        // Set up profile icon click listener
        profileIcon.setOnClickListener {
            val intent = Intent(context, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        // Set up add session FAB click listener
        addSessionFab.setOnClickListener {
            val intent = Intent(context, CreateSessionActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    fun getSessionsAdapter(): SessionsAdapter {
        return sessionsAdapter
    }
}