package com.cpen321.study_wimme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SessionsListActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var addSessionFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions_list)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        addSessionFab = findViewById(R.id.addSessionFab)

        // Set initial fragment
        loadFragment(HomeFragment())

        // Select Home tab initially
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.nav_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }
                else -> false
            }
        }

        addSessionFab.setOnClickListener {
            startActivityForResult(
                Intent(this, CreateSessionActivity::class.java),
                CREATE_SESSION_REQUEST
            )
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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
            val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
            homeFragment?.getSessionsAdapter()?.addSession(session)
        }
    }

    companion object {
        private const val CREATE_SESSION_REQUEST = 1
    }
}