package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Rule
import org.junit.Test

/** Pruebas UI instrumentadas: requieren dispositivo o emulador, no Firebase. */
class CatalogLearningPreviewTest {
    @get:Rule val rule = createComposeRule()

    @Test fun buscarPorNombreFiltraLaVista() {
        rule.setContent { FarmAppTheme { CatalogLearningPreview(modifier = Modifier.fillMaxSize()) } }
        rule.onNodeWithTag("catalog_search").performTextInput("Termómetro")
        rule.onNodeWithTag("catalog_list")
            .performScrollToNode(hasTestTag("catalog_product_FA-006"))
        rule.onNodeWithTag("catalog_product_FA-006").assertIsDisplayed()
        rule.onNodeWithTag("catalog_product_FA-004").assertDoesNotExist()
    }

    @Test fun filtroPorCategoriaSePuedeRestablecer() {
        rule.setContent { FarmAppTheme { CatalogLearningPreview(modifier = Modifier.fillMaxSize()) } }
        rule.onNodeWithTag("catalog_filter_HIGIENE").performClick()
        rule.onNodeWithTag("catalog_list")
            .performScrollToNode(hasTestTag("catalog_product_FA-001"))
        rule.onNodeWithTag("catalog_product_FA-001").assertExists()
        rule.onNodeWithTag("catalog_product_FA-006").assertDoesNotExist()
        // Si el filtro quedó fuera de pantalla, regresar al comienzo del LazyColumn.
        rule.onNodeWithTag("catalog_list")
            .performScrollToNode(hasTestTag("catalog_filter_all"))
        rule.onNodeWithTag("catalog_filter_all").performClick()
        rule.onNodeWithTag("catalog_list")
            .performScrollToNode(hasTestTag("catalog_product_FA-006"))
        rule.onNodeWithTag("catalog_product_FA-006").assertIsDisplayed()
    }

    @Test fun lazyColumnPermiteDesplazarseHastaUnaTarjetaPersonalizada() {
        rule.setContent { FarmAppTheme { CatalogLearningPreview(modifier = Modifier.fillMaxSize()) } }
        rule.onNodeWithTag("catalog_list").assertExists()
        rule.onNodeWithTag("catalog_list")
            .performScrollToNode(hasTestTag("catalog_product_FA-006"))
        rule.onNodeWithTag("catalog_product_FA-006").assertIsDisplayed()
    }
}
