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
import org.hamcrest.Description
import org.hamcrest.Matcher
import androidx.test.espresso.matcher.BoundedMatcher

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
        onView(withId(R.id.facultyInput))
            .perform(typeText("CPEN"), closeSoftKeyboard())
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

        onView(withText("Test Session 1"))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.findButton)).perform(click())

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Session 1")),
                    click()
                )
            )

        onView(withId(R.id.joinButton)).perform(click())

        onView(withId(R.id.backButton)).perform(click())
        onView(withId(R.id.joinedButton)).perform(click())

        onView(withText("Test Session 1"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.sessionsRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Session 1")),
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
                    hasDescendant(withText("Test Session 1")),
                    click()
                )
            )

        onView(withId(R.id.leaveButton)).perform(click())
        onView(withId(R.id.backButton)).perform(click())

        onView(withText("Test Session 1"))
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


        onView(withText("Test Session Espresso"))
            .check(matches(not(isDisplayed())))
    }
}
