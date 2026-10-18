package com.example.farmapp.ui.catalog

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.farmapp.catalog.AvailabilityPhase
import com.example.farmapp.catalog.AvailabilityUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** C19: prueba de Compose de interacción; no requiere acceso a Firebase. */
class CatalogAvailabilityPanelTest {
    @get:Rule val rule = createComposeRule()

    @Test fun permiteIniciarConsultaMedianteCallback() {
        var clicked = 0
        rule.setContent {
            CatalogAvailabilityPanel(AvailabilityUiState(), onRefresh = { clicked++ }, onCancel = {})
        }
        rule.onNodeWithTag("async_refresh").performClick()
        rule.runOnIdle { assertEquals(1, clicked) }
    }

    @Test fun cargaDeshabilitaConsultaYMuestraCancelar() {
        rule.setContent {
            CatalogAvailabilityPanel(
                AvailabilityUiState(AvailabilityPhase.LOADING),
                onRefresh = {}, onCancel = {}
            )
        }
        rule.onNodeWithTag("async_refresh").assertIsNotEnabled()
        rule.onNodeWithTag("async_cancel").assertExists().assertIsEnabled()
        rule.onNodeWithTag("async_loading_indicator").assertExists()
    }
}
