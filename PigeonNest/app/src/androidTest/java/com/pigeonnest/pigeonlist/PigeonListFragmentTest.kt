package com.pigeonnest.pigeonlist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.pigeonnest.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PigeonListFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun fragmentLaunch_showsSearchView() {
        launchFragmentInContainer<com.pigeonnest.presentation.pigeonlist.PigeonListFragment>(
            themeResId = R.style.Theme_PigeonNest
        )
        onView(withId(R.id.search_view)).check(matches(isDisplayed()))
    }

    @Test
    fun fragmentLaunch_showsAddButton() {
        launchFragmentInContainer<com.pigeonnest.presentation.pigeonlist.PigeonListFragment>(
            themeResId = R.style.Theme_PigeonNest
        )
        onView(withId(R.id.fab_add_pigeon)).check(matches(isDisplayed()))
    }
}
