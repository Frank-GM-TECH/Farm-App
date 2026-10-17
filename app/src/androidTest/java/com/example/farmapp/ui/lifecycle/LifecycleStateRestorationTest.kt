package com.example.farmapp.ui.lifecycle

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simula guardado/restauración de estado de Compose (como al recrear una Activity).
 * Sin Firebase: usa callbacks de prueba. Para probar rotación física y la secuencia
 * de los siete callbacks de MainActivity, consultar GUIA_EJECUTAR_COTEJO_13.md.
 */
@RunWith(AndroidJUnit4::class)
class LifecycleStateRestorationTest {
    @get:Rule val rule = createComposeRule()

    private fun screen(tester: StateRestorationTester, onLogin: (String, String) -> Unit = { _, _ -> }) {
        tester.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = onLogin,
                    onRegister = { _, _ -> },
                    onResetPassword = {},
                    onDismissMessage = {}
                )
            }
        }
    }

    @Test fun correoYModoRegistroPersistenAlRestaurarEstado() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tester = StateRestorationTester(rule)
        screen(tester)

        rule.onNodeWithTag("auth_email_input").performTextInput("cliente@farmapp.pe")
        rule.onNodeWithTag("auth_toggle_mode_button").performClick()
        tester.emulateSavedInstanceStateRestore()

        rule.onNodeWithTag("auth_email_input").assertTextContains("cliente@farmapp.pe")
        rule.onNodeWithText(context.getString(R.string.auth_title_register)).assertExists()
    }

    @Test fun contrasenaNoSeGuardaEnEstadoRestaurable() {
        val tester = StateRestorationTester(rule)
        var submittedPassword: String? = null
        screen(tester, onLogin = { _, password -> submittedPassword = password })

        rule.onNodeWithTag("auth_password_input").performTextInput("Secreto123")
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("auth_submit_button").performClick()

        rule.runOnIdle { assertEquals("", submittedPassword) }
    }
}
