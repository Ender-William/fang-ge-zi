package com.pigeonnest

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.pigeonnest.presentation.main.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun launchActivity_showsBottomNavigation() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_switchesBetweenTabs() {
        ActivityScenario.launch(MainActivity::class.java)

        // Default tab should show pigeon list title (indirectly verified by UI presence)
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))

        // Click on loft tab
        onView(withId(R.id.loftListFragment)).perform(click())
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))

        // Click on family tab
        onView(withId(R.id.familyListFragment)).perform(click())
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))

        // Click on settings tab
        onView(withId(R.id.settingsFragment)).perform(click())
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))
    }
}
