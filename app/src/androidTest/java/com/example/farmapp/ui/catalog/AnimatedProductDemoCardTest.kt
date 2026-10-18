package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Rule
import org.junit.Test

/** Pruebas de interfaz: ejecutar en androidTest con emulador/dispositivo. */
class AnimatedProductDemoCardTest {
    @get:Rule val rule = createComposeRule()

    @Test fun alPulsarVerDetallesApareceYSePuedeOcultar() {
        rule.setContent {
            FarmAppTheme { AnimatedProductDemoCard(DemoCatalogData.products.first()) }
        }
        rule.onNodeWithTag("catalog_details_FA-001").assertDoesNotExist()
        rule.onNodeWithTag("catalog_toggle_FA-001").performClick()
        rule.waitForIdle() // esperar a que finalice la animación del cambio de tamaño
        rule.onNodeWithTag("catalog_details_FA-001").assertIsDisplayed()
        rule.onNodeWithTag("catalog_toggle_FA-001").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("catalog_details_FA-001").assertDoesNotExist()
    }

    @Test fun expandirUnaTarjetaNoExpandeLaSiguiente() {
        rule.setContent {
            FarmAppTheme {
                Column {
                    AnimatedProductDemoCard(DemoCatalogData.products[0])
                    AnimatedProductDemoCard(DemoCatalogData.products[1])
                }
            }
        }
        rule.onNodeWithTag("catalog_toggle_FA-001").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("catalog_details_FA-001").assertIsDisplayed()
        rule.onNodeWithTag("catalog_details_FA-002").assertDoesNotExist()
    }
}
