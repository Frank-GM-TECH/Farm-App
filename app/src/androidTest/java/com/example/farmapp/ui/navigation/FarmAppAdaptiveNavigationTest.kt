package com.example.farmapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** La misma pantalla, distintas ventanas. Ejecutar en emulador/dispositivo. */
class FarmAppAdaptiveNavigationTest {
    @get:Rule val rule = createComposeRule()

    private fun show(width: WindowWidthSizeClass) {
        rule.setContent {
            FarmAppTheme {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = width,
                    selectedDestination = FarmAppDestinations.HOME,
                    onNavigateHome = {},
                    onNavigateCatalog = {},
                    modifier = Modifier.fillMaxSize()
                ) { Text("Contenido de prueba") }
            }
        }
    }

    @Test fun celularBarraInferior() {
        show(WindowWidthSizeClass.Compact)
        rule.onNodeWithTag("adaptive_bottom_bar").assertExists()
    }

    @Test fun pantallaMedianaRail() {
        show(WindowWidthSizeClass.Medium)
        rule.onNodeWithTag("adaptive_navigation_rail").assertExists()
    }

    @Test fun tabletGrandeDrawer() {
        show(WindowWidthSizeClass.Expanded)
        rule.onNodeWithTag("adaptive_navigation_drawer").assertExists()
    }

    @Test fun alPulsarCatalogoEjecutaCallback() {
        rule.setContent {
            var selected by remember { mutableIntStateOf(0) }
            FarmAppTheme {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = WindowWidthSizeClass.Compact,
                    selectedDestination = if (selected == 0) FarmAppDestinations.HOME
                                          else FarmAppDestinations.CATALOG,
                    onNavigateHome = { selected = 0 },
                    onNavigateCatalog = { selected = 1 }
                ) { Text("Contenido de prueba") }
            }
        }
        rule.onNodeWithTag("adaptive_go_catalog").performClick()
        rule.onNodeWithTag("adaptive_go_catalog").assertIsSelected()
    }
}
