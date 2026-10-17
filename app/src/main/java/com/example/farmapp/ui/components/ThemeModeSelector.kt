package com.example.farmapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.ui.theme.FarmAppTheme
import com.example.farmapp.ui.theme.FarmThemeMode

/** Componente sin estado propio: el padre decide el tema y recibe los eventos. */
@Composable
fun ThemeModeSelector(
    selectedMode: FarmThemeMode,
    onModeSelected: (FarmThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_label),
                modifier = Modifier.padding(top = 13.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FarmThemeMode.entries.forEach { option ->
                FilterChip(
                    selected = selectedMode == option,
                    enabled = enabled,
                    onClick = { onModeSelected(option) },
                    label = {
                        Text(stringResource(when (option) {
                            FarmThemeMode.SYSTEM -> R.string.theme_system
                            FarmThemeMode.LIGHT -> R.string.theme_light
                            FarmThemeMode.DARK -> R.string.theme_dark
                        }))
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("theme_option_${option.name.lowercase()}")
                )
            }
        }
    }
}

@Preview(name = "Cotejo 10 - Cambiar tema", showBackground = true)
@Composable
private fun ThemeModeSelectorPreview() {
    var selectedName by rememberSaveable { mutableStateOf(FarmThemeMode.LIGHT.name) }
    val selected = FarmThemeMode.entries.firstOrNull { it.name == selectedName } ?: FarmThemeMode.LIGHT
    FarmAppTheme(mode = selected) {
        Column {
            ThemeModeSelector(selectedMode = selected, onModeSelected = { selectedName = it.name })
            Surface {
                Text(
                    text = stringResource(R.string.theme_preview_text),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
