package com.pigeonnest

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.pigeonnest.presentation.main.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PigeonCrudFlowTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun addPigeonFlow_navigatesToEditScreen() {
        ActivityScenario.launch(MainActivity::class.java)

        // Click FAB to add pigeon
        onView(withId(R.id.fab_add_pigeon)).perform(click())

        // Should navigate to edit screen with step 1 visible
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_ring_number)).check(matches(isDisplayed()))
        onView(withId(R.id.radio_group_gender)).check(matches(isDisplayed()))
    }

    @Test
    fun addPigeonFlow_fillStep1AndNavigate() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.fab_add_pigeon)).perform(click())

        // Fill basic info
        onView(withId(R.id.edit_name)).perform(typeText("测试鸽子"), closeSoftKeyboard())
        onView(withId(R.id.edit_ring_number)).perform(typeText("2024-TEST-001"), closeSoftKeyboard())

        // Select male gender
        onView(withId(R.id.radio_male)).perform(click())

        // Click next to go to step 2
        onView(withId(R.id.button_next)).perform(click())

        // Step 2 should show loft selection
        onView(withId(R.id.button_select_loft)).check(matches(isDisplayed()))
    }
}
