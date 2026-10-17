package com.example.farmapp.data.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Un archivo aislado por método: no se toca la preferencia real del usuario. */
@RunWith(AndroidJUnit4::class)
class DataStoreUserPreferencesRepositoryTest {
    private lateinit var scope: CoroutineScope
    private lateinit var dbFile: File
    private lateinit var store: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
    private lateinit var repository: DataStoreUserPreferencesRepository

    @Before fun prepare() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dbFile = context.preferencesDataStoreFile("test_${UUID.randomUUID()}")
        store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { dbFile })
        repository = DataStoreUserPreferencesRepository(store)
    }
    @After fun cleanup() { scope.cancel() }

    @Test fun defaultsSeLeenDeArchivoVacio() = runBlocking {
        assertEquals(UserPreferences(), repository.preferences.first())
    }
    @Test fun cambioDeTemaSePersiste() = runBlocking {
        repository.setTheme(AppThemePreference.DARK)
        assertEquals(AppThemePreference.DARK, repository.preferences.first().theme)
        assertEquals(AppThemePreference.DARK, DataStoreUserPreferencesRepository(store).preferences.first().theme)
    }
    @Test fun cambiosDeVistaYTemaSonIndependientes() = runBlocking {
        repository.setGalleryLayout(GalleryLayoutPreference.LIST)
        repository.setTheme(AppThemePreference.LIGHT)
        assertEquals(UserPreferences(AppThemePreference.LIGHT, GalleryLayoutPreference.LIST), repository.preferences.first())
    }
    @Test fun valoresDesconocidosCaenEnDefaults() = runBlocking {
        store.edit { it[stringPreferencesKey("theme_mode")] = "OBSOLETE" }
        store.edit { it[stringPreferencesKey("gallery_layout")] = "UNKNOWN" }
        assertEquals(UserPreferences(), repository.preferences.first())
    }
}
