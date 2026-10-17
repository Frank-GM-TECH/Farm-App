package com.example.farmapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Una sola instancia de DataStore por archivo/proceso. */
private val Context.userPreferencesDataStore by preferencesDataStore(name = "farmapp_user_preferences")

fun createUserPreferencesRepository(context: Context): UserPreferencesRepository =
    DataStoreUserPreferencesRepository(context.applicationContext.userPreferencesDataStore)

/** Constructor inyectable: las pruebas usan un DataStore de archivo aislado. */
class DataStoreUserPreferencesRepository(
    private val store: DataStore<Preferences>
) : UserPreferencesRepository {
    private companion object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val GALLERY = stringPreferencesKey("gallery_layout")
    }

    override val preferences: Flow<UserPreferences> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { values ->
            UserPreferences(
                theme = decodePreference(
                    values[THEME], AppThemePreference.entries.toTypedArray(),
                    AppThemePreference.SYSTEM
                ),
                galleryLayout = decodePreference(
                    values[GALLERY], GalleryLayoutPreference.entries.toTypedArray(),
                    GalleryLayoutPreference.GRID
                )
            )
        }

    override suspend fun setTheme(theme: AppThemePreference) {
        store.edit { it[THEME] = theme.name }
    }

    override suspend fun setGalleryLayout(layout: GalleryLayoutPreference) {
        store.edit { it[GALLERY] = layout.name }
    }
}
