package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import com.example.farmapp.catalog.CatalogViewModel
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Prueba instrumentada: UI -> callback -> ViewModel -> StateFlow -> UI. */
class CatalogViewModelUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun escribirBusquedaActualizaElViewModelYLaUi() {
        val vm = CatalogViewModel(SavedStateHandle(), DemoCatalogData.repository)
        rule.setContent {
            FarmAppTheme {
                CatalogLearningPreview(
                    modifier = Modifier.fillMaxSize(),
                    catalogViewModel = vm
                )
            }
        }
        rule.onNodeWithTag("catalog_search").performTextInput("gasas")
        rule.waitForIdle()
        assertEquals("gasas", vm.uiState.value.query)
        assertEquals("FA-004", vm.uiState.value.snapshot.products.single().sku)
        rule.onNodeWithTag("catalog_search").assertTextContains("gasas")
        rule.onNodeWithTag("catalog_results_summary").assertExists()
    }
}
