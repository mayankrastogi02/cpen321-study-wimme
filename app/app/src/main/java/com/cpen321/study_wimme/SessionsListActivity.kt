package com.cpen321.study_wimme

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SessionsListActivity : AppCompatActivity() {
    private lateinit var viewToggleGroup: MaterialButtonToggleGroup
    private lateinit var visibilityToggleGroup: MaterialButtonToggleGroup
    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var addSessionFab: FloatingActionButton
    private lateinit var sessionsAdapter: SessionsAdapter

    private var currentViewMode: ViewMode = ViewMode.LIST
    private var currentVisibility: SessionVisibility = SessionVisibility.PRIVATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sessions_list)

        // Setup edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupToggleGroups()
        setupFab()
    }

    private fun initializeViews() {
        viewToggleGroup = findViewById(R.id.viewToggleGroup)
        visibilityToggleGroup = findViewById(R.id.visibilityToggleGroup)
        sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView)
        addSessionFab = findViewById(R.id.addSessionFab)
    }

    private fun setupRecyclerView() {
        sessionsAdapter = SessionsAdapter()
        sessionsRecyclerView.apply {
            adapter = sessionsAdapter
            layoutManager = LinearLayoutManager(this@SessionsListActivity)
        }
    }

    private fun setupToggleGroups() {
        // View mode toggle (List/Map)
        viewToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentViewMode = when (checkedId) {
                    R.id.listViewButton -> ViewMode.LIST
                    R.id.mapViewButton -> ViewMode.MAP
                    else -> ViewMode.LIST
                }
                updateViewMode()
            }
        }

        // Visibility toggle (Private/Public)
        visibilityToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentVisibility = when (checkedId) {
                    R.id.privateButton -> SessionVisibility.PRIVATE
                    R.id.publicButton -> SessionVisibility.PUBLIC
                    else -> SessionVisibility.PRIVATE
                }
                updateSessionsList()
            }
        }

        // Set initial states
        viewToggleGroup.check(R.id.listViewButton)
        visibilityToggleGroup.check(R.id.privateButton)
    }

    private fun setupFab() {
        addSessionFab.setOnClickListener {
            // TODO: Implement new session creation
            showAddSessionDialog()
        }
    }

    private fun updateViewMode() {
        when (currentViewMode) {
            ViewMode.LIST -> {
                sessionsRecyclerView.visibility = View.VISIBLE
                // TODO: Hide map view when implemented
            }
            ViewMode.MAP -> {
                sessionsRecyclerView.visibility = View.GONE
                // TODO: Show map view when implemented
            }
        }
    }

    private fun updateSessionsList() {
        // TODO: Update sessions based on visibility setting
        val sessions = when (currentVisibility) {
            SessionVisibility.PRIVATE -> getPrivateSessions()
            SessionVisibility.PUBLIC -> getPublicSessions()
        }
        sessionsAdapter.submitList(sessions)
    }

    private fun showAddSessionDialog() {
        // TODO: Implement add session dialog
    }

    private fun getPrivateSessions(): List<Session> {
        // TODO: Implement fetching private sessions
        return emptyList()
    }

    private fun getPublicSessions(): List<Session> {
        // TODO: Implement fetching public sessions
        return emptyList()
    }

    enum class ViewMode {
        LIST, MAP
    }

    enum class SessionVisibility {
        PRIVATE, PUBLIC
    }
}