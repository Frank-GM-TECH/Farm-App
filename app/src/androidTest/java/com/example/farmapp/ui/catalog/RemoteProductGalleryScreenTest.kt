package com.example.farmapp.ui.catalog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.network.RemoteProductsError
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Composición sobre estado falso; no necesita API remota ni autenticación. */
class RemoteProductGalleryScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test fun success_muestraCuadriculaYTareaConImagenFallback() {
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(
                RemoteProductsUiState(RemoteProductsPhase.SUCCESS,
                    products = listOf(RemoteProduct(23, "Crema demo", 4.50, null))),
                onRetry = {}, onBack = {}
            )
        } }
        rule.onNodeWithTag("gallery_grid").assertIsDisplayed()
        rule.onNodeWithTag("gallery_photo_23").assertIsDisplayed()
        rule.onNodeWithTag("gallery_image_23").assertIsDisplayed()
    }

    @Test fun loading_muestraIndicador() {
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(
                RemoteProductsUiState(RemoteProductsPhase.LOADING), {}, {}
            )
        } }
        rule.onNodeWithTag("gallery_loading").assertIsDisplayed()
    }

    @Test fun error_clickEnReintentarEjecutaCallback() {
        var retries = 0
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(
                RemoteProductsUiState(RemoteProductsPhase.ERROR,
                    error = RemoteProductsError.NETWORK),
                onRetry = { retries++ }, onBack = {}
            )
        } }
        rule.onNodeWithTag("gallery_error").assertIsDisplayed()
        rule.onNodeWithTag("gallery_retry").performClick()
        assertEquals(1, retries)
    }

    @Test fun navigation_atrasEjecutaCallback() {
        var backs = 0
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(RemoteProductsUiState(), onRetry = {}, onBack = { backs++ })
        } }
        rule.onNodeWithTag("gallery_back").performClick()
        assertEquals(1, backs)
    }
}
