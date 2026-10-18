package com.example.farmapp.ui.auth

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Instrumentación de Compose: usar emulador/dispositivo, sin Firebase. */
@RunWith(AndroidJUnit4::class)
class AuthScreenInteractionTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun completarFormularioYEnviarLoginInvocaCallbackConValoresCorrectos() {
        var received: Pair<String, String>? = null
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { email, password -> received = email to password },
                    onRegister = { _, _ -> },
                    onResetPassword = {},
                    onDismissMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("auth_email_input").performTextInput("cliente@farmapp.pe")
        composeRule.onNodeWithTag("auth_password_input").performTextInput("123456")
        composeRule.onNodeWithTag("auth_submit_button").performClick()
        composeRule.runOnIdle { assertEquals("cliente@farmapp.pe" to "123456", received) }
    }

    @Test fun cambiarARegistroYEnviarLlamaCallbackDeRegistro() {
        var registration: Pair<String, String>? = null
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> },
                    onRegister = { email, password -> registration = email to password },
                    onResetPassword = {},
                    onDismissMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("auth_toggle_mode_button").performClick()
        composeRule.onNodeWithTag("auth_email_input").performTextInput("nuevo@farmapp.pe")
        composeRule.onNodeWithTag("auth_password_input").performTextInput("Farmacia2026")
        composeRule.onNodeWithTag("auth_submit_button").performClick()
        composeRule.runOnIdle { assertEquals("nuevo@farmapp.pe" to "Farmacia2026", registration) }
    }

    @Test fun estadoCargandoDeshabilitaEnvioYCampos() {
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(isLoading = true),
                    onSignIn = { _, _ -> },
                    onRegister = { _, _ -> },
                    onResetPassword = {},
                    onDismissMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("auth_submit_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth_email_input").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth_password_input").assertIsNotEnabled()
    }

    @Test fun errorDeValidacionSeMuestraEnPantalla() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(errorMessageRes = R.string.auth_error_invalid_email),
                    onSignIn = { _, _ -> },
                    onRegister = { _, _ -> },
                    onResetPassword = {},
                    onDismissMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("auth_error_message").assertExists()
        composeRule.onNodeWithText(context.getString(R.string.auth_error_invalid_email)).assertExists()
    }

    @Test fun recuperarContrasenaEntregaCorreoEscritoAlCallback() {
        var resetEmail: String? = null
        composeRule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> },
                    onRegister = { _, _ -> },
                    onResetPassword = { resetEmail = it },
                    onDismissMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("auth_email_input").performTextInput("cliente@farmapp.pe")
        composeRule.onNodeWithTag("auth_reset_password_button").performClick()
        composeRule.runOnIdle { assertEquals("cliente@farmapp.pe", resetEmail) }
    }
}
