package com.example.farmapp.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Cotejo 05: componente SIN estado propio (stateless).
 * Recibe valor y visibilidad desde quien lo llama; emite eventos mediante lambdas.
 * De ese modo se reutiliza en registro e ingreso sin duplicar estados.
 */
@Composable
fun AuthPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val visibilityAction = stringResource(
        if (isPasswordVisible) R.string.a11y_hide_password else R.string.a11y_show_password
    )
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier.testTag("auth_password_input"),
        label = { Text(stringResource(R.string.auth_password_label)) },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(
                onClick = onPasswordVisibilityToggle,
                enabled = enabled,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("auth_password_visibility")
                    .semantics {
                        contentDescription = visibilityAction
                    }
            ) {
                Text(
                    stringResource(
                        if (isPasswordVisible) R.string.auth_hide_password
                        else R.string.auth_show_password
                    )
                )
            }
        },
        enabled = enabled
    )
}

/**
 * Preview con padre CON estado para comprobar la recomposición.
 * El estado no vive dentro de AuthPasswordField sino en esta función que lo usa.
 * Cambiar texto o pulsar Mostrar/Ocultar cambia la composición visible.
 */
@Preview(name = "Cotejo 05 - Estado elevado", showBackground = true)
@Composable
private fun PasswordFieldHoistingPreview() {
    var previewPassword by remember { mutableStateOf("Farmacia2026") }
    var previewVisible by remember { mutableStateOf(false) }

    FarmAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthPasswordField(
                password = previewPassword,
                onPasswordChange = { previewPassword = it },
                isPasswordVisible = previewVisible,
                onPasswordVisibilityToggle = { previewVisible = !previewVisible },
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
