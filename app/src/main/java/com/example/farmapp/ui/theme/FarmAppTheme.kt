package com.example.farmapp.ui.theme

import androidx.compose.runtime.Composable

/** Nombre público de tema usado por los ocho cotejos anteriores: no cambia las llamadas. */
@Composable
fun FarmAppTheme(
    mode: FarmThemeMode = FarmThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    FarmMaterialTheme(mode = mode, content = content)
}
