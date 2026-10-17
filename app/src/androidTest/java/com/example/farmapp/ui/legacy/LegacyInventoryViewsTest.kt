package com.example.farmapp.ui.legacy

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.farmapp.R
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas del flujo Fragment + FAB + DialogFragment, deben ejecutarse en emulador. */
@RunWith(AndroidJUnit4::class)
class LegacyInventoryViewsTest {
    @Test fun screenShowsRecyclerViewAndFab() {
        ActivityScenario.launch(LegacyInventoryActivity::class.java).use {
            onView(withId(R.id.legacy_recycler)).check(matches(isDisplayed()))
            onView(withId(R.id.legacy_fab)).check(matches(isDisplayed()))
        }
    }

    @Test fun fabOpensNewItemDialog() {
        ActivityScenario.launch(LegacyInventoryActivity::class.java).use {
            onView(withId(R.id.legacy_fab)).perform(click())
            onView(withId(R.id.legacy_item_name)).check(matches(isDisplayed()))
            onView(withId(R.id.legacy_item_price)).check(matches(isDisplayed()))
            onView(withId(R.id.legacy_item_stock)).check(matches(isDisplayed()))
        }
    }
}
