package com.cpen321.study_wimme

import android.content.Intent
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
            val intent = Intent(this, CreateSessionActivity::class.java)
            startActivityForResult(intent, CREATE_SESSION_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_SESSION_REQUEST && resultCode == RESULT_OK) {
            val session = Session(
                name = data?.getStringExtra("SESSION_NAME") ?: "",
                time = data?.getStringExtra("SESSION_TIME") ?: "",
                location = data?.getStringExtra("SESSION_LOCATION") ?: "",
                description = data?.getStringExtra("SESSION_DESCRIPTION") ?: "",
                visibility = data?.getSerializableExtra("SESSION_VISIBILITY") as? SessionVisibility
                    ?: SessionVisibility.PRIVATE
            )
            sessionsAdapter.addSession(session)
        }
    }

    private fun updateViewMode() {
        // Update the view mode of the sessions list (List or Map)
        when (currentViewMode) {
            ViewMode.LIST -> {
                // Set RecyclerView to List mode
                sessionsRecyclerView.visibility = View.VISIBLE
                // Hide Map view if implemented
            }
            ViewMode.MAP -> {
                // Set RecyclerView to Map mode
                sessionsRecyclerView.visibility = View.GONE
                // Show Map view if implemented
            }
        }
    }

    private fun updateSessionsList() {
        // Update the sessions list based on the selected visibility (Private or Public)
        sessionsAdapter.filterSessions(currentVisibility)
    }



    enum class ViewMode {
        LIST, MAP
    }


    companion object {
        private const val CREATE_SESSION_REQUEST = 1
    }
}