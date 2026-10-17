package com.example.farmapp.ui.auth

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AuthGoogleButtonTest {
    @get:Rule val rule = createComposeRule()

    @Test fun googleButtonInvocaCallback() {
        var calls = 0
        rule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> }, onRegister = { _, _ -> },
                    onResetPassword = {}, onDismissMessage = {},
                    onGoogleSignIn = { calls++ }
                )
            }
        }
        rule.onNodeWithTag("auth_google_sign_in_button").assertExists().performClick()
        rule.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun googleButtonBloqueadoMientrasSeAutentica() {
        rule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(isLoading = true),
                    onSignIn = { _, _ -> }, onRegister = { _, _ -> },
                    onResetPassword = {}, onDismissMessage = {}
                )
            }
        }
        rule.onNodeWithTag("auth_google_sign_in_button").assertIsNotEnabled()
    }

    @Test fun googleButtonBloqueadoMientrasSelectorEstaAbierto() {
        rule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> }, onRegister = { _, _ -> },
                    onResetPassword = {}, onDismissMessage = {},
                    isGooglePickerOpen = true
                )
            }
        }
        rule.onNodeWithTag("auth_google_sign_in_button").assertIsNotEnabled()
    }
}
