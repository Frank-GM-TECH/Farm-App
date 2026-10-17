package com.example.farmapp.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

/** Cotejo 17: regla explícita del codelab Reply (Compact/Medium/Expanded). */
enum class FarmAppNavigationType { BOTTOM_BAR, NAVIGATION_RAIL, PERMANENT_DRAWER }

fun navigationTypeFor(widthSizeClass: WindowWidthSizeClass): FarmAppNavigationType =
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> FarmAppNavigationType.BOTTOM_BAR
        WindowWidthSizeClass.Medium -> FarmAppNavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> FarmAppNavigationType.PERMANENT_DRAWER
        else -> FarmAppNavigationType.BOTTOM_BAR
    }

