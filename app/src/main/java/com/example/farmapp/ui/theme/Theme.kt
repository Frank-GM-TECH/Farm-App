package com.example.farmapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R

/**
 * FARMAPP MODERN
 * Sistema de temas Material 3.
 *
 * Conserva los tres modos:
 * SISTEMA, CLARO y OSCURO.
 */

enum class FarmThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

// =====================================
// TEMA CLARO - FARMAPP MODERN
// =====================================

private val LightColorPalette = lightColorScheme(

    // Colores principales
    primary = FarmGreen40,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainerLight,
    onPrimaryContainer = FarmOnGreenContainerLight,

    inversePrimary = FarmGreen80,

    // Colores secundarios
    secondary = FarmSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = FarmSecondaryContainerLight,
    onSecondaryContainer = FarmSecondaryLight,

    // Acentos
    tertiary = FarmTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = FarmTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF16495D),

    // Fondo principal
    background = FarmBackgroundLight,
    onBackground = FarmOnSurfaceLight,

    // Superficies
    surface = FarmSurfaceLight,
    onSurface = FarmOnSurfaceLight,
    surfaceTint = FarmGreen40,

    // Capas de elevación Material 3
    surfaceDim = Color(0xFFE4EBEF),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7FAFC),
    surfaceContainer = Color(0xFFEEF3F6),
    surfaceContainerHigh = Color(0xFFE7EFF2),
    surfaceContainerHighest = Color(0xFFDFE9EE),

    // Superficies alternativas
    surfaceVariant = FarmSurfaceVariantLight,
    onSurfaceVariant = FarmOnSurfaceVariantLight,

    // Colores inversos
    inverseSurface = FarmOnSurfaceLight,
    inverseOnSurface = Color.White,

    // Bordes
    outline = FarmOutlineLight,
    outlineVariant = Color(0xFFCEDADF),

    // Errores
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    scrim = Color.Black
)

// =====================================
// TEMA OSCURO - FARMAPP MODERN
// =====================================

private val DarkColorPalette = darkColorScheme(

    // Colores principales
    primary = FarmGreen80,
    onPrimary = Color(0xFF003C35),
    primaryContainer = FarmGreenContainerDark,
    onPrimaryContainer = FarmOnGreenContainerDark,

    inversePrimary = FarmGreen40,

    // Colores secundarios
    secondary = FarmSecondaryDark,
    onSecondary = Color(0xFF17324D),
    secondaryContainer = FarmSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFE0EDF7),

    // Acentos
    tertiary = FarmTertiaryDark,
    onTertiary = Color(0xFF063D54),
    tertiaryContainer = FarmTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFD5F1FF),

    // Fondo principal
    background = FarmBackgroundDark,
    onBackground = FarmOnSurfaceDark,

    // Superficies
    surface = FarmSurfaceDark,
    onSurface = FarmOnSurfaceDark,
    surfaceTint = FarmGreen80,

    // Capas de elevación Material 3
    surfaceDim = Color(0xFF0C1820),
    surfaceBright = Color(0xFF2D424E),
    surfaceContainerLowest = Color(0xFF09141C),
    surfaceContainerLow = Color(0xFF12212B),
    surfaceContainer = Color(0xFF192C36),
    surfaceContainerHigh = Color(0xFF233944),
    surfaceContainerHighest = Color(0xFF2D4550),

    // Superficies alternativas
    surfaceVariant = FarmSurfaceVariantDark,
    onSurfaceVariant = FarmOnSurfaceVariantDark,

    // Colores inversos
    inverseSurface = FarmOnSurfaceDark,
    inverseOnSurface = Color(0xFF162A35),

    // Bordes
    outline = FarmOutlineDark,
    outlineVariant = Color(0xFF455B65),

    // Errores
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    scrim = Color.Black
)

// =====================================
// APLICACIÓN DEL TEMA
// =====================================

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
        colorScheme = if (darkTheme) {
            DarkColorPalette
        } else {
            LightColorPalette
        },
        typography = FarmTypography,
        shapes = FarmShapes,
        content = content
    )
}

// =====================================
// PREVISUALIZACIONES
// =====================================

@Preview(
    name = "FarmApp Modern - Claro",
    showBackground = true
)
@Composable
private fun LightPalettePreview() {
    FarmAppTheme(mode = FarmThemeMode.LIGHT) {
        Surface {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    stringResource(R.string.theme_light),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(
    name = "FarmApp Modern - Oscuro",
    showBackground = true
)
@Composable
private fun DarkPalettePreview() {
    FarmAppTheme(mode = FarmThemeMode.DARK) {
        Surface {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    stringResource(R.string.theme_dark),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}