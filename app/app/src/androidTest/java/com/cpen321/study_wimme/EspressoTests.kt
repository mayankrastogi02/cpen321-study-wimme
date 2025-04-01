package com.cpen321.study_wimme

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.Calendar
import java.util.Properties
import android.view.View
import android.widget.EditText
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.intent.Intents.intended
import org.hamcrest.Description
import org.hamcrest.Matcher
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog

//For displayed error messages (non-toast)
fun hasEditTextErrorText(expectedError: String): Matcher<View> {
    return object : BoundedMatcher<View, EditText>(EditText::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has error text: $expectedError")
        }
        override fun matchesSafely(editText: EditText): Boolean {
            return editText.error?.toString() == expectedError
        }
    }
}

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreateSessionActivityTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun globalSetup() {
            // Stored in test.properties file
            val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
            val properties = Properties()
            instrumentationContext.assets.open("test.properties").use { properties.load(it) }

            val googleId = properties.getProperty("googleId")
            val userId = properties.getProperty("userId")

            val appContext = ApplicationProvider.getApplicationContext<Context>()
            val sharedPrefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("googleId", googleId)
                .putString("userId", userId)
                .apply()
        }
    }

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test01_CreateSessionWithValidInputs() {

        val createScenario = ActivityScenario.launch(CreateSessionActivity::class.java)

        onView(withId(R.id.sessionNameInput))
            .perform(typeText(""), closeSoftKeyboard())

        createScenario.onActivity { activity ->
            val calStart = Calendar.getInstance().apply {
                set(2026, Calendar.FEBRUARY, 20, 10, 0)
            }
            val calEnd = Calendar.getInstance().apply {
                set(2026, Calendar.FEBRUARY, 21, 11, 0)
            }

            activity.setTestDates(calStart.timeInMillis, calEnd.timeInMillis)

            activity.findViewById<TextInputEditText>(R.id.startTimeInput)
                .setText("Feb 20, 2026 10:00 AM")
            activity.findViewById<TextInputEditText>(R.id.endTimeInput)
                .setText("Feb 21, 2026 11:00 AM")
        }

        val resultData = Intent().apply {
            putExtra("LATITUDE", 37.4219983)
            putExtra("LONGITUDE", -122.084)
        }

        val activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        Intents.intending(hasComponent(MapLocationPickerActivity::class.java.name))
            .respondWith(activityResult)

        onView(withId(R.id.sessionLocationInput)).perform(click())

        onView(withId(R.id.sessionLocationInput))
            .check(matches(withText(containsString("37.4219"))))

        onView(withId(R.id.sessionDescriptionInput))
            .perform(typeText("This is a test session"), closeSoftKeyboard())
        onView(withId(R.id.subjectInput))
            .perform(typeText("Math"), closeSoftKeyboard())
        onView(withId(R.id.facultyDropdown))
            .perform(click()) // Open the dropdown
        onView(withText("Law"))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        onView(withId(R.id.yearInput))
            .perform(typeText("5"), closeSoftKeyboard())

        onView(withId(R.id.publicButton)).perform(click())
        onView(withId(R.id.hostButton)).perform(click())

        //failure scenario
        onView(withId(R.id.sessionNameInput))
            .perform(click())

        onView(withId(R.id.sessionNameInput))
            .check(matches(hasEditTextErrorText("Session name is required")))

        //Fill with valid name
        onView(withId(R.id.sessionNameInput))
            .perform(typeText("Test Session Espresso"), closeSoftKeyboard())
        onView(withId(R.id.hostButton)).perform(click())

        ActivityScenario.launch(SessionsListActivity::class.java)
        //verify it shows up
        onView(withId(R.id.publicButton)).perform(click())
        onView(withId(R.id.hostedButton)).perform(click())

        onView(withText("Test Session Espresso"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test02_JoinAndLeaveSession() {
        val activity = ActivityScenario.launch(SessionsListActivity::class.java)
        var currentActivity: Activity? = null
        activity.onActivity { currentActivity = it }

        onView(withId(R.id.publicButton)).perform(click())
        onView(withId(R.id.joinedButton)).perform(click())

        onView(withText("STAT251"))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.findButton)).perform(click())

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("STAT251")),
                    click()
                )
            )

        onView(withId(R.id.joinButton)).perform(click())

        onView(withId(R.id.backButton)).perform(click())
        onView(withId(R.id.joinedButton)).perform(click())

        onView(withText("STAT251"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("STAT251")),
                    click()
                )
            )
        onView(withId(R.id.joinButton)).perform(click())

        // we are back on the session list after failure scenario
        onView(withId(R.id.publicButton)).check(matches(isDisplayed()))
        onView(withId(R.id.joinedButton)).check(matches(isDisplayed()))

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("STAT251")),
                    click()
                )
            )

        onView(withId(R.id.leaveButton)).perform(click())
        onView(withId(R.id.backButton)).perform(click())

        onView(withText("STAT251"))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun test03_DeleteHostedSession() {
        // Will not work if run independently from other tests unless test01_CreateSessionWithValidInputs() is run first.
        val scenario = ActivityScenario.launch(SessionsListActivity::class.java)

        onView(withId(R.id.publicButton)).perform(click())
        onView(withId(R.id.hostedButton)).perform(click())

        onView(withText("Test Session Espresso"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Session Espresso")),
                    click()
                )
            )
        onView(withId(R.id.deleteButton)).perform(click())

        //failure scenario
        onView(withText("Cancel"))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(R.id.deleteButton)).perform(click())

        onView(withText("Delete"))
            .inRoot(isDialog())
            .perform(click())

        Thread.sleep(1000)

        onView(withText("Test Session Espresso"))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun test04_NewUserCompletingProfilePrepopulate() {
        // Set up SharedPreferences with dummy Google sign-in info
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("displayName", "Alice Wonderland")
            .putString("email", "alice@wonderland.com")
            .apply()

        // Launch UserSettingsActivity with COMPLETE_PROFILE set to true
        val intent = Intent(appContext, UserSettingsActivity::class.java).apply {
            putExtra("COMPLETE_PROFILE", true)
        }
        ActivityScenario.launch<UserSettingsActivity>(intent)

        onView(withId(R.id.usernameEditText))
            .perform(clearText(), closeSoftKeyboard())

        onView(withId(R.id.programDropdown))
            .perform(click()) // Open the dropdown
        onView(withText("Engineering"))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        onView(withId(R.id.yearEditText))
            .perform(typeText("2"), closeSoftKeyboard())
        onView(withId(R.id.interestsEditText))
            .perform(typeText("Math, Science"), closeSoftKeyboard())

        // Attempt to save with an empty username
        onView(withId(R.id.saveButton)).perform(click())

        onView(withId(R.id.usernameEditText))
            .check(matches(hasEditTextErrorText("Username is required")))

        // Now fix the username field
        onView(withId(R.id.usernameEditText))
            .perform(typeText("alice"), closeSoftKeyboard())

        // Verify that the first and last names are prepopulated from displayName
        onView(withId(R.id.firstNameEditText))
            .check(matches(withText("Alice")))
        onView(withId(R.id.lastNameEditText))
            .check(matches(withText("Wonderland")))

        // Verify that the username is set using the email's local-part ("alice")
        onView(withId(R.id.usernameEditText))
            .check(matches(withText("alice")))

        // Verify that the back and delete account buttons are hidden
        onView(withId(R.id.backButton))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.deleteAccountButton))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.saveButton)).perform(click())

        intended(hasComponent(SessionsListActivity::class.java.getName()))
    }

    @Test
    fun test05_ReadProfileFields() {
        val studyListScenario = ActivityScenario.launch(SessionsListActivity::class.java)

        onView(withId(R.id.profileIcon))
            .perform(click())

        onView(withId(R.id.usernameEditText))
            .check(matches(withText("alice")))
        onView(withId(R.id.firstNameEditText))
            .check(matches(withText("Alice")))
        onView(withId(R.id.lastNameEditText))
            .check(matches(withText("Wonderland")))
        onView(withId(R.id.programDropdown))
            .check(matches(withText("Engineering")))
        onView(withId(R.id.yearEditText))
            .check(matches(withText("2")))
        onView(withId(R.id.interestsEditText))
            .check(matches(withText("Math, Science")))
    }

    @Test
    fun test06_EditProfileFieldsAndSave() {
        val studyListScenario = ActivityScenario.launch(SessionsListActivity::class.java)

        onView(withId(R.id.profileIcon))
            .perform(click())

        // Verify that each field is pre-populated (not empty) before editing.
        onView(withId(R.id.usernameEditText))
            .check(matches(not(withText(""))))
        onView(withId(R.id.firstNameEditText))
            .check(matches(not(withText(""))))
        onView(withId(R.id.lastNameEditText))
            .check(matches(not(withText(""))))
        onView(withId(R.id.programDropdown))
            .check(matches(not(withText(""))))
        onView(withId(R.id.yearEditText))
            .check(matches(not(withText(""))))
        onView(withId(R.id.interestsEditText))
            .check(matches(not(withText(""))))

        onView(withId(R.id.usernameEditText))
            .perform(clearText(), closeSoftKeyboard())

        // Attempt to save with an empty username
        onView(withId(R.id.saveButton)).perform(click())

        onView(withId(R.id.usernameEditText))
            .check(matches(hasEditTextErrorText("Username is required")))

        onView(withId(R.id.usernameEditText))
            .perform(clearText(), typeText("John Doe"), closeSoftKeyboard())
        onView(withId(R.id.firstNameEditText))
            .perform(clearText(), typeText("John"), closeSoftKeyboard())
        onView(withId(R.id.lastNameEditText))
            .perform(clearText(), typeText("Doe"), closeSoftKeyboard())
        onView(withId(R.id.programDropdown))
            .perform(click()) // Open the dropdown
        onView(withText("Law"))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        onView(withId(R.id.yearEditText))
            .perform(clearText(), typeText("3"), closeSoftKeyboard())
        onView(withId(R.id.interestsEditText))
            .perform(clearText(), typeText("Biology, CPSC"), closeSoftKeyboard())

        // the activity will simply call finish() so that it goes back to SessionsListActivity.
        onView(withId(R.id.saveButton)).perform(click())

        intended(hasComponent(SessionsListActivity::class.java.getName()))

        Thread.sleep(1000)

        onView(withId(R.id.profileIcon))
            .perform(click())

        onView(withId(R.id.usernameEditText))
            .check(matches(withText("John Doe")))
        onView(withId(R.id.firstNameEditText))
            .check(matches(withText("John")))
        onView(withId(R.id.lastNameEditText))
            .check(matches(withText("Doe")))
        onView(withId(R.id.programDropdown))
            .check(matches(withText("Law")))
        onView(withId(R.id.yearEditText))
            .check(matches(withText("3")))
        onView(withId(R.id.interestsEditText))
            .check(matches(withText("Biology, CPSC")))
    }
}
