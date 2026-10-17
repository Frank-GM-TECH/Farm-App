package com.example.farmapp.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveNavigationTypeTest {
    @Test fun compactUsaBarraInferior() = assertEquals(
        FarmAppNavigationType.BOTTOM_BAR,
        navigationTypeFor(WindowWidthSizeClass.Compact)
    )
    @Test fun mediumUsaRail() = assertEquals(
        FarmAppNavigationType.NAVIGATION_RAIL,
        navigationTypeFor(WindowWidthSizeClass.Medium)
    )
    @Test fun expandedUsaDrawerPermanente() = assertEquals(
        FarmAppNavigationType.PERMANENT_DRAWER,
        navigationTypeFor(WindowWidthSizeClass.Expanded)
    )
}
