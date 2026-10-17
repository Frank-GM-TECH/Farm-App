package com.example.farmapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.auth.password.PasswordAdvisor
import com.example.farmapp.auth.password.PasswordRequirement
import com.example.farmapp.auth.password.PasswordStrength
import com.example.farmapp.auth.password.RequirementCheck
import com.example.farmapp.ui.theme.FarmAppTheme

/** Asesor visual del registro. `remember` conserva el asesor entre recomposiciones. */
@Composable
fun PasswordGuidance(password: String, modifier: Modifier = Modifier) {
    val advisor = remember { PasswordAdvisor() }
    val result = remember(password) { advisor.evaluate(password) }

    // when como expresión: varias opciones pueden compartir la misma rama (coma).
    val strengthLabel = when (result.strength) {
        PasswordStrength.EMPTY, PasswordStrength.SHORT -> R.string.password_strength_short
        PasswordStrength.BASIC -> R.string.password_strength_basic
        PasswordStrength.IMPROVED -> R.string.password_strength_improved
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.password_strength_label, stringResource(strengthLabel)),
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            // repeat() es una función de orden superior. Lambda final e `it` implícito.
            repeat(3) { segment ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (segment < result.strength.filledSegments) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
        result.checks.forEach { check ->
            val textRes = when (check.requirement) {
                PasswordRequirement.MINIMUM_LENGTH -> R.string.password_requirement_length
                PasswordRequirement.CONTAINS_LETTER -> R.string.password_requirement_letter
                PasswordRequirement.CONTAINS_NUMBER -> R.string.password_requirement_number
            }
            // when + `is`: distingue polimórficamente las dos subclases selladas.
            val (statusRes, tint) = when (check) {
                is RequirementCheck.Met -> R.string.password_check_met to MaterialTheme.colorScheme.primary
                is RequirementCheck.Missing -> R.string.password_check_missing to MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = stringResource(statusRes, stringResource(textRes)),
                color = tint,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = stringResource(R.string.password_checks_completed, result.completedChecks, result.checks.size),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = stringResource(R.string.password_strength_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Cotejo 03 - Kotlin", showBackground = true)
@Composable
private fun PasswordGuidancePreview() {
    FarmAppTheme {
        Card(modifier = Modifier.padding(16.dp)) {
            PasswordGuidance(password = "Farmacia2026", modifier = Modifier.padding(16.dp))
        }
    }
}
