package com.example.farmapp.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmapp.R
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Prueba instrumentada propuesta para Android Studio, NO ejecutada en este entorno.
 * Comprueba que el hijo notifica cambios al padre y que estos se reflejan en la UI.
 */
@RunWith(AndroidJUnit4::class)
class AuthPasswordFieldHoistingTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun writingIntoStatelessFieldUpdatesParentAndRecomposes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            var password by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            FarmAppTheme {
                Column {
                    AuthPasswordField(
                        password = password,
                        onPasswordChange = { password = it },
                        isPasswordVisible = visible,
                        onPasswordVisibilityToggle = { visible = !visible },
                        enabled = true
                    )
                    Text("Longitud actual: ${password.length}")
                }
            }
        }
        composeRule.onNodeWithTag("auth_password_input").performTextInput("abc123")
        composeRule.onNodeWithText("Longitud actual: 6").assertExists()
        composeRule.onNodeWithText(context.getString(R.string.auth_show_password)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.auth_hide_password)).assertExists()
    }
}
