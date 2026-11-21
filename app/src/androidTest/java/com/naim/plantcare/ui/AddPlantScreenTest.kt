package com.naim.plantcare.ui

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.naim.plantcare.R
import com.naim.plantcare.ui.add.AddPlantActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPlantScreenTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AddPlantActivity::class.java)

    @Test
    fun saveWithEmptyForm_showsNameAndSpeciesErrors() {
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        onView(withId(R.id.tilName)).check(matches(hasError(R.string.error_plant_name)))
        onView(withId(R.id.tilSpecies)).check(matches(hasError(R.string.error_species_required)))
    }

    @Test
    fun invalidWateringDays_showsErrorAndKeepsNameValid() {
        onView(withId(R.id.etName)).perform(replaceText("Монстерата в хола"), closeSoftKeyboard())
        onView(withId(R.id.etCustomDays)).perform(scrollTo(), replaceText("95"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        onView(withId(R.id.tilCustomDays)).check(matches(hasError(R.string.error_watering_days)))
        onView(withId(R.id.tilName)).check(matches(hasError(null)))
    }

    @Test
    fun fixingName_removesErrorOnNextSave() {
        onView(withId(R.id.etName)).perform(replaceText("М"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())
        onView(withId(R.id.tilName)).check(matches(hasError(R.string.error_plant_name)))

        onView(withId(R.id.etName)).perform(scrollTo(), replaceText("Фикус"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        onView(withId(R.id.tilName)).check(matches(hasError(null)))
        onView(withId(R.id.tilSpecies)).check(matches(hasError(R.string.error_species_required)))
    }

    private fun hasError(@StringRes expected: Int?): Matcher<View> =
        object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("TextInputLayout with error ").appendValue(expected)
            }

            override fun matchesSafely(layout: TextInputLayout): Boolean {
                val actual = layout.error?.toString()
                return if (expected == null) actual == null else actual == layout.context.getString(expected)
            }
        }
}
