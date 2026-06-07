package com.pigeonnest.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.pigeonnest.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun fragmentLaunch_showsSettingsOptions() {
        launchFragmentInContainer<com.pigeonnest.presentation.settings.SettingsFragment>(
            themeResId = R.style.Theme_PigeonNest
        )

        onView(withId(R.id.button_font_size)).check(matches(isDisplayed()))
        onView(withId(R.id.switch_high_contrast)).check(matches(isDisplayed()))
        onView(withId(R.id.button_export)).check(matches(isDisplayed()))
        onView(withId(R.id.button_import)).check(matches(isDisplayed()))
        onView(withId(R.id.button_about)).check(matches(isDisplayed()))
    }

    @Test
    fun clickFontSize_showsDialog() {
        launchFragmentInContainer<com.pigeonnest.presentation.settings.SettingsFragment>(
            themeResId = R.style.Theme_PigeonNest
        )

        onView(withId(R.id.button_font_size)).perform(click())
        onView(withText("字体大小")).check(matches(isDisplayed()))
    }

    @Test
    fun clickAbout_showsAboutDialog() {
        launchFragmentInContainer<com.pigeonnest.presentation.settings.SettingsFragment>(
            themeResId = R.style.Theme_PigeonNest
        )

        onView(withId(R.id.button_about)).perform(click())
        onView(withText("关于放鸽子")).check(matches(isDisplayed()))
    }
}
