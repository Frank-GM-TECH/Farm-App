package com.example.farmapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.example.farmapp.catalog.CatalogAnalyzer
import com.example.farmapp.catalog.CatalogUiState
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Tests UI requieren ejecución en emulador/tablet; no se certifican desde la JVM. */
class CatalogAdaptiveContentTest {
    @get:Rule val rule = createComposeRule()

    private fun render(width: Int, onOpen: () -> Unit = {}) {
        val state = CatalogUiState("", null, CatalogAnalyzer(DemoCatalogData.repository).search())
        rule.setContent {
            FarmAppTheme {
                // requiredWidth permite ensayar layouts mayores que el ancho del emulador.
                Box(Modifier.requiredWidth(width.dp).height(780.dp)) {
                    CatalogAdaptiveContent(
                        uiState = state,
                        onQueryChanged = {},
                        onCategorySelected = {},
                        onBack = {},
                        onSelectProduct = { onOpen() },
                        onShareProduct = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    @Test fun telefonoListaDeUnPanel() {
        render(390)
        rule.onNodeWithTag("catalog_single_pane").assertExists()
        rule.onNodeWithTag("catalog_two_pane").assertDoesNotExist()
    }

    @Test fun anchoMedianoNoAprietaDosPaneles() {
        render(620)
        rule.onNodeWithTag("catalog_single_pane").assertExists()
    }

    @Test fun tabletAnchaTieneDosPaneles() {
        render(1000)
        rule.onNodeWithTag("catalog_two_pane").assertExists()
        rule.onNodeWithTag("catalog_list_pane").assertExists()
        rule.onNodeWithTag("catalog_detail_pane").assertExists()
    }

    @Test fun seleccionarProductoEnTabletActualizaPanelSinNavegar() {
        var navigations = 0
        render(1000) { navigations++ }
        rule.onNodeWithTag("catalog_list").performScrollToNode(
            hasTestTag("navigate_to_product_FA-004")
        )
        rule.onNodeWithTag("navigate_to_product_FA-004").performClick()
        rule.onNodeWithTag("product_detail_title").assertTextEquals("Gasas estériles")
        assertEquals(0, navigations)
    }

    @Test fun seleccionarProductoEnTelefonoUsaCallbackNavegacion() {
        var navigations = 0
        render(390) { navigations++ }
        rule.onNodeWithTag("catalog_list").performScrollToNode(
            hasTestTag("navigate_to_product_FA-004")
        )
        rule.onNodeWithTag("navigate_to_product_FA-004").performClick()
        assertEquals(1, navigations)
    }
}
