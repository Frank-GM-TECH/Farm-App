package com.example.farmapp.ui.catalog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.network.RemoteProductsError
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Requiere Android Studio y emulador; sin red y sin Firebase. */
class RemoteProductsPanelTest {
    @get:Rule val rule = createComposeRule()

    @Test fun idle_botonRealizaCallback() {
        var requests = 0
        rule.setContent { FarmAppTheme {
            RemoteProductsPanel(RemoteProductsUiState(), onRefresh = { requests++ })
        } }
        rule.onNodeWithTag("net_demo_refresh").performClick()
        assertEquals(1, requests)
    }

    @Test fun idle_botonVerJsonRealizaCallback() {
        var clicks = 0
        rule.setContent { FarmAppTheme {
            RemoteProductsPanel(RemoteProductsUiState(), onRefresh = {}, onRawJson = { clicks++ })
        } }
        rule.onNodeWithTag("net_demo_raw").performClick()
        assertEquals(1, clicks)
    }

    @Test fun loading_desactivaBoton() {
        rule.setContent { FarmAppTheme {
            RemoteProductsPanel(RemoteProductsUiState(RemoteProductsPhase.LOADING), {})
        } }
        rule.onNodeWithTag("net_demo_refresh").assertIsNotEnabled()
        rule.onNodeWithTag("net_demo_loading").assertIsDisplayed()
    }

    @Test fun error_redExponeMensajeAccesible() {
        rule.setContent { FarmAppTheme {
            RemoteProductsPanel(
                RemoteProductsUiState(RemoteProductsPhase.ERROR, error = RemoteProductsError.NETWORK),
                {}
            )
        } }
        rule.onNodeWithTag("net_demo_error").assertIsDisplayed()
    }
}
