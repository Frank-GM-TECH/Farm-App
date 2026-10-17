package com.example.farmapp.ui.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.data.preferences.GalleryLayoutPreference
import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GalleryDataStorePreferenceUiTest {
    @get:Rule val rule = createComposeRule()
    private val sample = RemoteProductsUiState(
        RemoteProductsPhase.SUCCESS,
        products = listOf(RemoteProduct(101, "Gel demo", 4.0))
    )
    @Test fun cambiaDeCuadriculaAListaSinNuevaLlamadaDeRed() {
        var selected by mutableStateOf(GalleryLayoutPreference.GRID)
        var changes = 0
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(sample, onRetry = {}, onBack = {},
                galleryLayout = selected,
                onGalleryLayoutSelected = { selected = it; changes++ })
        } }
        rule.onNodeWithTag("gallery_grid").assertIsDisplayed()
        rule.onNodeWithTag("gallery_layout_list").performClick()
        rule.onNodeWithTag("gallery_list").assertIsDisplayed()
        rule.onNodeWithTag("gallery_row_101").assertIsDisplayed()
        assertEquals(1, changes)
    }
    @Test fun preferenciaCargadaComoListaSeMuestraDeInicio() {
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(sample, onRetry = {}, onBack = {},
                galleryLayout = GalleryLayoutPreference.LIST)
        } }
        rule.onNodeWithTag("gallery_list").assertIsDisplayed()
    }
    @Test fun botonDeshabilitadoMientrasSeLeenPreferencias() {
        var changes = 0
        rule.setContent { FarmAppTheme {
            RemoteProductGalleryScreen(sample, onRetry = {}, onBack = {},
                galleryLayoutReady = false,
                onGalleryLayoutSelected = { changes++ })
        } }
        rule.onNodeWithTag("gallery_layout_list").assertIsNotEnabled()
        assertEquals(0, changes)
    }
}
