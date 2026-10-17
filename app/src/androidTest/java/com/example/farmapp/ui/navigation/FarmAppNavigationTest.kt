package com.example.farmapp.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Cotejo 16: pruebas instrumentadas; requieren emulador/dispositivo Android. */
class FarmAppNavigationTest {
    @get:Rule val rule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private var sharedSku: String? = null

    private fun setApp(initiallyAuthenticated: Boolean) {
        rule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            var auth by remember {
                mutableStateOf(
                    AuthUiState(
                        isAuthenticated = initiallyAuthenticated,
                        userEmail = if (initiallyAuthenticated) "demo@example.com" else null
                    )
                )
            }
            FarmAppTheme {
                FarmAppNavHost(
                    uiState = auth,
                    onSignIn = { _, _ -> auth = AuthUiState(true, "demo@example.com") },
                    onRegister = { _, _ -> auth = AuthUiState(true, "demo@example.com") },
                    onResetPassword = {},
                    onDismissMessage = {},
                    onSignOut = { auth = AuthUiState() },
                    navController = navController,
                    onShareProduct = { sharedSku = it.sku }
                )
            }
        }
        rule.waitForIdle()
    }

    @Test fun comienzaEnAutenticacionCuandoNoHaySesion() {
        setApp(false)
        rule.onNodeWithTag("auth_submit_button").assertIsDisplayed()
        assertEquals(FarmAppDestinations.AUTH, navController.currentDestination?.route)
    }

    @Test fun navegaInicioCatalogoFichaYRegresa() {
        setApp(true)
        rule.onNodeWithTag("navigate_to_catalog").performClick()
        rule.waitForIdle()
        assertEquals(FarmAppDestinations.CATALOG, navController.currentDestination?.route)
        rule.onNodeWithTag("catalog_list").performScrollToNode(
            androidx.compose.ui.test.hasTestTag("navigate_to_product_FA-001")
        )
        rule.onNodeWithTag("navigate_to_product_FA-001").performClick()
        rule.waitForIdle()
        assertEquals(FarmAppDestinations.PRODUCT, navController.currentDestination?.route)
        rule.onNodeWithTag("share_product").performClick()
        rule.runOnIdle { assertEquals("FA-001", sharedSku) }
        rule.onNodeWithTag("back_from_product").performClick()
        rule.waitForIdle()
        assertEquals(FarmAppDestinations.CATALOG, navController.currentDestination?.route)
        rule.onNodeWithTag("back_from_catalog").performClick()
        rule.waitForIdle()
        assertEquals(FarmAppDestinations.HOME, navController.currentDestination?.route)
    }

    @Test fun cerrarSesionLimpiaNavegacionProtegida() {
        setApp(true)
        rule.onNodeWithTag("navigate_to_catalog").performClick()
        rule.onNodeWithTag("back_from_catalog").performClick()
        rule.onNodeWithTag("home_sign_out").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("auth_submit_button").assertIsDisplayed()
        assertEquals(FarmAppDestinations.AUTH, navController.currentDestination?.route)
        rule.runOnIdle { assertTrue(navController.popBackStack().not()) }
    }
}
