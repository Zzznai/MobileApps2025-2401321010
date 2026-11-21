package com.naim.plantcare.ui

import android.os.SystemClock
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.naim.plantcare.R
import com.naim.plantcare.data.PlantCareDatabase
import com.naim.plantcare.ui.list.MainActivity
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantFlowTest {
    companion object {
        private const val SPECIES_LABEL = "Монстера (Monstera deliciosa)"
        private const val WAIT_MS = 5000L
        private const val POLL_MS = 100L
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val plantName = "Тестово растение ${SystemClock.uptimeMillis() % 100_000}"

    @After
    fun removeTestPlant() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PlantCareDatabase.getInstance(context).openHelper.writableDatabase
            .execSQL("DELETE FROM plants WHERE name = ?", arrayOf(plantName))
    }

    @Test
    fun addPlant_showsInListAndOpensDetails() {
        onView(withId(R.id.fabAdd)).perform(click())

        waitUntil { onView(withId(R.id.etName)).perform(replaceText(plantName), closeSoftKeyboard()) }
        waitUntil { onView(withId(R.id.actvSpecies)).check(matches(hasSpeciesLoaded())) }
        onView(withId(R.id.actvSpecies)).perform(chooseSpecies(SPECIES_LABEL))
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        waitUntil {
            onView(withId(R.id.rvPlants)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(plantName))),
            )
        }
        onView(withText(plantName)).check(matches(isDisplayed())).perform(click())

        waitUntil { onView(withId(R.id.tvName)).check(matches(withText(plantName))) }
        onView(withId(R.id.tvSubtitle)).check(matches(withText(containsString("Monstera deliciosa"))))
    }

    private fun waitUntil(check: () -> Unit) {
        val deadline = SystemClock.uptimeMillis() + WAIT_MS
        while (true) {
            try {
                check()
                return
            } catch (error: Throwable) {
                if (SystemClock.uptimeMillis() > deadline) throw error
                SystemClock.sleep(POLL_MS)
            }
        }
    }

    private fun hasSpeciesLoaded(): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("AutoCompleteTextView with loaded species")
        }

        override fun matchesSafely(view: View): Boolean = view is AutoCompleteTextView && (view.adapter?.count ?: 0) > 0
    }

    private fun chooseSpecies(label: String): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(AutoCompleteTextView::class.java)

        override fun getDescription(): String = "choose species $label"

        override fun perform(uiController: UiController, view: View) {
            (view as AutoCompleteTextView).setText(label, false)
            uiController.loopMainThreadUntilIdle()
        }
    }
}
