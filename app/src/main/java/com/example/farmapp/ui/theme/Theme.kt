package com.example.farmapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
    import androidx.compose.ui.res.stringResource
    import com.example.farmapp.R

/** Cotejo 10: tema del sistema o elección explícita del usuario. */
enum class FarmThemeMode { SYSTEM, LIGHT, DARK }

private val LightColorPalette = lightColorScheme(
    primary = FarmGreen40,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainerLight,
    onPrimaryContainer = FarmOnGreenContainerLight,
    secondary = FarmSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = FarmSecondaryContainerLight,
    onSecondaryContainer = FarmOnSurfaceLight,
    tertiary = FarmTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = FarmTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF002F33),
    background = FarmBackgroundLight,
    onBackground = FarmOnSurfaceLight,
    surface = FarmSurfaceLight,
    onSurface = FarmOnSurfaceLight,
    surfaceVariant = FarmSurfaceVariantLight,
    onSurfaceVariant = FarmOnSurfaceVariantLight,
    outline = FarmOutlineLight,
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkColorPalette = darkColorScheme(
    primary = FarmGreen80,
    onPrimary = Color(0xFF00391E),
    primaryContainer = FarmGreenContainerDark,
    onPrimaryContainer = FarmOnGreenContainerDark,
    secondary = FarmSecondaryDark,
    onSecondary = Color(0xFF173A27),
    secondaryContainer = FarmSecondaryContainerDark,
    onSecondaryContainer = FarmOnSurfaceDark,
    tertiary = FarmTertiaryDark,
    onTertiary = Color(0xFF00383E),
    tertiaryContainer = FarmTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFB4EEF2),
    background = FarmBackgroundDark,
    onBackground = FarmOnSurfaceDark,
    surface = FarmSurfaceDark,
    onSurface = FarmOnSurfaceDark,
    surfaceVariant = FarmSurfaceVariantDark,
    onSurfaceVariant = FarmOnSurfaceVariantDark,
    outline = FarmOutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

/**
 * Reutilizable en todos los sprints; por defecto sigue el tema del sistema.
 * Cotejo 27: la selección SISTEMA/CLARO/OSCURO ahora se obtiene de
 * Preferences DataStore a través del UserPreferencesViewModel.
 */
@Composable
internal fun FarmMaterialTheme(
    mode: FarmThemeMode = FarmThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        FarmThemeMode.SYSTEM -> isSystemInDarkTheme()
        FarmThemeMode.LIGHT -> false
        FarmThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = FarmTypography,
        shapes = FarmShapes,
        content = content
    )
}

/** Previews del mismo componente en dos variantes, sin conexión a Firebase. */
@Preview(name = "Cotejo 10 - Material claro", showBackground = true)
@Composable
private fun LightPalettePreview() {
    FarmAppTheme(mode = FarmThemeMode.LIGHT) {
        Surface { Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.theme_light), style = MaterialTheme.typography.bodyMedium)
        } }
    }
}

@Preview(name = "Cotejo 10 - Material oscuro", showBackground = true)
@Composable
private fun DarkPalettePreview() {
    FarmAppTheme(mode = FarmThemeMode.DARK) {
        Surface { Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.theme_dark), style = MaterialTheme.typography.bodyMedium)
        } }
    }
}
