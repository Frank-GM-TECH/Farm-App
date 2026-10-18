package com.example.farmapp.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.ui.components.ThemeModeSelector
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class ThemeModeSelectorTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun puedeCambiarDeClaroAOscuroYModificaLaPaleta() {
        var mode by mutableStateOf(FarmThemeMode.LIGHT)
        var backgroundObserved = Color.Unspecified
        composeRule.setContent {
            FarmAppTheme(mode = mode) {
                val currentBackground = MaterialTheme.colorScheme.background
                SideEffect { backgroundObserved = currentBackground }
                Column {
                    ThemeModeSelector(selectedMode = mode, onModeSelected = { mode = it })
                    Surface(modifier = Modifier.testTag("theme_surface")) { }
                }
            }
        }
        composeRule.onNodeWithTag("theme_option_light").assertIsSelected()
        var colorBefore = Color.Unspecified
        composeRule.runOnIdle { colorBefore = backgroundObserved }
        composeRule.onNodeWithTag("theme_option_dark").performClick()
        composeRule.onNodeWithTag("theme_option_dark").assertIsSelected()
        composeRule.runOnIdle { assertNotEquals(colorBefore, backgroundObserved) }
    }
}
