package com.example.farmapp.data.preferences

import kotlinx.coroutines.flow.Flow

/** Preferencias no sensibles de la app: no guardar contraseñas ni tokens aquí. */
enum class AppThemePreference { SYSTEM, LIGHT, DARK }
enum class GalleryLayoutPreference { GRID, LIST }

data class UserPreferences(
    val theme: AppThemePreference = AppThemePreference.SYSTEM,
    val galleryLayout: GalleryLayoutPreference = GalleryLayoutPreference.GRID
)

/** La capa UI consume una interfaz; no conoce DataStore ni Context. */
interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setTheme(theme: AppThemePreference)
    suspend fun setGalleryLayout(layout: GalleryLayoutPreference)
}

/** Parseo tolerante a valores anteriores, desconocidos o corruptos. */
fun <T : Enum<T>> decodePreference(
    raw: String?, values: Array<T>, fallback: T
): T = values.firstOrNull { it.name == raw } ?: fallback
