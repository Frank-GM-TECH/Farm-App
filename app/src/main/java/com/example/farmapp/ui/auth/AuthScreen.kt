package com.example.farmapp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.ui.components.FarmAppBrandHeader
import com.example.farmapp.ui.components.PasswordGuidance
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Interfaz pura: recibe estado del ViewModel y conserva estado efímero en Compose.
 * Cotejo 05: hoisting de password/visibilidad a AuthScreen; el hijo no guarda estado.
 */
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onResetPassword: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    isGooglePickerOpen: Boolean = false
) {
    // Cotejo 13: conservamos correo y modo al recrear Activity (por ejemplo, al girar).
    var email by rememberSaveable { mutableStateOf("") }
    var registerMode by rememberSaveable { mutableStateOf(false) }
    // Privacidad: NO guardar contraseñas en el Bundle de estado restaurable.
    // Se conserva solo entre recomposiciones y se limpia al recrear la Activity.
    var password by remember { mutableStateOf("") }
    // Cotejo 04: estado temporal con remember y mutableStateOf.
    // Por seguridad, la contraseña vuelve a ocultarse si la Activity se recrea.
    var passwordVisible by remember { mutableStateOf(false) }

    val busy = uiState.isLoading || isGooglePickerOpen

    LaunchedEffect(registerMode) {
        onDismissMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FarmAppBrandHeader(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(
                if (registerMode) R.string.auth_title_register else R.string.auth_title_sign_in
            ),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
            label = { Text(stringResource(R.string.auth_email_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !busy
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Cotejo 05: elevación de estado.
        // AuthScreen posee el estado y el componente hijo solo lo recibe y notifica eventos.
        AuthPasswordField(
            password = password,
            onPasswordChange = { password = it },
            isPasswordVisible = passwordVisible,
            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
        // HU01 + Cotejo 03: ejemplo visible de Kotlin en el formulario real.
        if (registerMode) {
            Spacer(modifier = Modifier.height(10.dp))
            PasswordGuidance(password = password, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(16.dp))
        uiState.errorMessageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("auth_error_message")
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.infoMessageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("auth_info_message")
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                if (registerMode) onRegister(email, password) else onSignIn(email, password)
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth_submit_button")
        ) {
            if (uiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(stringResource(R.string.a11y_auth_processing))
                }
            } else {
                Text(stringResource(
                    if (registerMode) R.string.auth_register_button else R.string.auth_sign_in_button
                ))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!registerMode) {
            TextButton(
                onClick = { onResetPassword(email) },
                modifier = Modifier.testTag("auth_reset_password_button"),
                enabled = !busy
            ) {
                Text(stringResource(R.string.auth_forgot_password))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.auth_or_continue_with),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("auth_google_sign_in_button")
        ) {
            Text(stringResource(R.string.auth_google_button))
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = { registerMode = !registerMode },
            modifier = Modifier.testTag("auth_toggle_mode_button"),
            enabled = !busy
        ) {
            Text(stringResource(
                if (registerMode) R.string.auth_switch_sign_in else R.string.auth_switch_register
            ))
        }
    }
}


/**
 * Preview interactiva (clic en "Interactive mode" de Android Studio).
 * Se pueden probar los campos, el cambio Registro/Ingreso y Mostrar/Ocultar
 * sin conectar Firebase: los callbacks son simulados.
 */
@Preview(name = "Cotejo 04 - Interactividad y estado", showBackground = true)
@Composable
private fun AuthScreenInteractivePreview() {
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
