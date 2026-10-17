package com.example.farmapp.ui.auth

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Cotejo 04: pruebas de UI para ejecutar en un emulador/dispositivo Android.
 * Requieren androidx.compose.ui:ui-test-junit4 y ui-test-manifest.
 * No se ejecutaron al crear este paquete (no hay SDK/Gradle local).
 */
@RunWith(AndroidJUnit4::class)
class AuthScreenStateTest {
    @get:Rule val composeRule = createComposeRule()

    private fun showScreen() {
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> },
                    onRegister = { _, _ -> },
                    onResetPassword = { _ -> },
                    onDismissMessage = {}
                )
            }
        }
    }

    @Test
    fun passwordVisibilityClickRecomposesTheScreen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        showScreen()
        composeRule.onNodeWithText(context.getString(R.string.auth_show_password))
            .assertExists()
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.auth_hide_password))
            .assertExists()
    }

    @Test
    fun tappingRegisterRecomposesFormTitle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        showScreen()
        composeRule.onNodeWithText(context.getString(R.string.auth_switch_register))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.auth_title_register))
            .assertExists()
    }
}
