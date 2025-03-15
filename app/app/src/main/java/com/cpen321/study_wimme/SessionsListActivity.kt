package com.cpen321.study_wimme

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SessionsListActivity : AppCompatActivity() {
    private val TAG = "SessionsListActivity"

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First check if the user has completed their profile
        checkProfileCreated()

        LoginActivity.getCurrentToken(this) {
            token ->
                val userId = LoginActivity.getCurrentUserId(this)

                val jsonData = JSONObject().apply {
                    put("userId", userId)
                    put("token", token)
                }

                val url = URL("${BuildConfig.SERVER_URL}/notification/deviceToken")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d(TAG, "JSON data: $jsonData")

                        val outputStream = OutputStreamWriter(connection.outputStream)
                        outputStream.write(jsonData.toString())
                        outputStream.flush()
                        outputStream.close()

                        // Handle the response
                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            Log.d(TAG, "Response: $response")
                        } else {
                            Log.e(TAG, "Server returned error code: $responseCode")
                            val errorResponse = connection.errorStream.bufferedReader().use { it.readText() }
                            Log.e(TAG, "Error Response: $errorResponse")
                        }
                    } catch (e: JSONException) {
                        // Log and handle exceptions
                        Log.e(TAG, "Error sending data: ${e.message}", e)
                    } finally {
                        connection.disconnect()
                    }
                }
        }

        setContentView(R.layout.activity_sessions_list)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set initial fragment
        loadFragment(HomeFragment())
        requestNotificationPermission()

        // Select Home tab initially
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_map -> {
                    if (hasLocationPermission()) {
                        loadFragment(MapFragment())
                    } else {
                        requestLocationPermission()
                    }
                    true
                }
                R.id.nav_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun checkProfileCreated() {
        val googleId = LoginActivity.getCurrentUserGoogleId(this)
        
        // If no Google ID found, user is not logged in
        if (googleId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Check with the server if the profile is created
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/auth/verify?googleId=$googleId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    
                    // Check the profileCreated flag
                    val profileCreated = jsonResponse.getBoolean("profileCreated")
                    
                    if (!profileCreated) {
                        withContext(Dispatchers.Main) {
                            // If profile not created, redirect to UserSettingsActivity
                            val intent = Intent(this@SessionsListActivity, UserSettingsActivity::class.java)
                            intent.putExtra("COMPLETE_PROFILE", true)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    withContext(Dispatchers.Main) {
                        // User doesn't exist in backend, go to login
                        startActivity(Intent(this@SessionsListActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                // In case of error, continue with the activity
                // The user will still be able to use the app if there's a temporary connection issue
            }
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

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = "android.permission.POST_NOTIFICATIONS"
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFragment(MapFragment())
            }
            else {
                Toast.makeText(
                    this,
                    "Precise location privileges are required for this feature.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val CREATE_SESSION_REQUEST = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}