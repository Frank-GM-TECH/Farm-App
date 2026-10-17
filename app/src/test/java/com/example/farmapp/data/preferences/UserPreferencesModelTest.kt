package com.example.farmapp.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

/** Valores de respaldo ante claves nuevas, desconocidas o vacías. */
class UserPreferencesModelTest {
    @Test fun defaultsSonSistemaYCuadricula() {
        assertEquals(AppThemePreference.SYSTEM, UserPreferences().theme)
        assertEquals(GalleryLayoutPreference.GRID, UserPreferences().galleryLayout)
    }
    @Test fun decodeAceptaValoresGuardados() {
        assertEquals(AppThemePreference.DARK, decodePreference("DARK", AppThemePreference.entries.toTypedArray(), AppThemePreference.SYSTEM))
        assertEquals(GalleryLayoutPreference.LIST, decodePreference("LIST", GalleryLayoutPreference.entries.toTypedArray(), GalleryLayoutPreference.GRID))
    }
    @Test fun decodeDeValorDesconocidoAplicaPredeterminado() {
        assertEquals(AppThemePreference.SYSTEM, decodePreference("OLD_THEME", AppThemePreference.entries.toTypedArray(), AppThemePreference.SYSTEM))
    }
    @Test fun decodeNullAplicaPredeterminado() {
        assertEquals(GalleryLayoutPreference.GRID, decodePreference(null, GalleryLayoutPreference.entries.toTypedArray(), GalleryLayoutPreference.GRID))
    }
}
