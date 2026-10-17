package com.example.farmapp.ui.legacy

import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.farmapp.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegacyNavigationGraphTest {
    @Test fun navigationGraphStartsAtTracker() {
        ActivityScenario.launch(LegacyInventoryActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager
                    .findFragmentById(R.id.legacy_nav_host)
                    as androidx.navigation.fragment.NavHostFragment
                assertEquals(R.id.legacy_tracker, host.navController.currentDestination?.id)
            }
        }
    }
}
