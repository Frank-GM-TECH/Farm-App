package com.example.farmapp.data

import android.content.Context
import androidx.room.Room

// HU01 - Autenticación.
import com.example.farmapp.auth.AuthRepository
import com.example.farmapp.auth.FirebaseAuthRepository

// Catálogo educativo existente.
import com.example.farmapp.catalog.CatalogRepository
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.InMemoryCatalogRepository

// HU03 - Catálogo persistente con Room.
import com.example.farmapp.data.catalog.CatalogDatabase
import com.example.farmapp.data.catalog.CatalogProductRepository
import com.example.farmapp.data.catalog.OfflineCatalogProductRepository

// Room educativo existente.
import com.example.farmapp.data.room.RoomDemoDatabase
import com.example.farmapp.data.room.RoomDemoRepository
import com.example.farmapp.data.room.OfflineRoomDemoRepository

// SQLite educativo existente.
import com.example.farmapp.data.sql.SqlLabDatabase
import com.example.farmapp.data.sql.SqlLabRepository
import com.example.farmapp.data.sql.SqliteSqlLabRepository

// Preferencias.
import com.example.farmapp.data.preferences.UserPreferencesRepository
import com.example.farmapp.data.preferences.createUserPreferencesRepository

// WorkManager.
import com.example.farmapp.data.background.InventoryReportRepository
import com.example.farmapp.data.background.WorkManagerInventoryReportRepository

// Retrofit y servicios remotos.
import com.example.farmapp.network.RemoteProductsRepository
import com.example.farmapp.network.RemoteProductsService
import com.example.farmapp.network.RetrofitRemoteProductsRepository

// HU02 - Perfil de usuario.
import com.example.farmapp.profile.ProfileRepository
import com.example.farmapp.profile.FirestoreProfileRepository

// Firebase.
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Retrofit y serialización.
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Contenedor principal de dependencias de FarmApp.
 *
 * Permite sustituir repositorios reales por implementaciones
 * falsas durante las pruebas unitarias.
 */
interface AppContainer {

    // Repositorios de funcionalidades existentes.
    val remoteProductsRepository: RemoteProductsRepository

    val catalogRepository: CatalogRepository<DemoProduct>

    val authRepository: AuthRepository

    val sqlLabRepository: SqlLabRepository

    val roomDemoRepository: RoomDemoRepository

    val userPreferencesRepository: UserPreferencesRepository

    val inventoryReportRepository: InventoryReportRepository

    // HU02 - Perfil de usuario.
    val profileRepository: ProfileRepository

    // HU03 - Catálogo persistente.
    val catalogProductRepository: CatalogProductRepository
}

/**
 * Implementación real de las dependencias de FarmApp.
 *
 * Se utiliza inicialización lazy para evitar crear
 * recursos hasta que realmente sean necesarios.
 */
class DefaultAppContainer(
    private val appContext: Context
) : AppContainer {

    // =========================================
    // HU02 - PERFIL DE USUARIO CON FIRESTORE
    // =========================================

    override val profileRepository: ProfileRepository by lazy {
        FirestoreProfileRepository(
            FirebaseFirestore.getInstance()
        )
    }

    // =========================================
    // HU03 - BASE DE DATOS LOCAL DEL CATÁLOGO
    // =========================================

    /**
     * Base de datos Room exclusiva para productos.
     *
     * Se mantiene independiente de RoomDemoDatabase
     * para no afectar las prácticas anteriores.
     */
    private val catalogDatabase: CatalogDatabase by lazy {

        Room.databaseBuilder(
            appContext.applicationContext,
            CatalogDatabase::class.java,
            "farmapp_catalog.db"
        ).build()
    }

    /**
     * Repositorio que permite consultar y guardar
     * productos de manera local.
     */
    override val catalogProductRepository:
            CatalogProductRepository by lazy {

        OfflineCatalogProductRepository(
            catalogDatabase.catalogProductDao()
        )
    }

    // =========================================
    // RETROFIT - PRODUCTOS REMOTOS
    // =========================================

    private val baseUrl = "https://dummyjson.com/"

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    private val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                ScalarsConverterFactory.create()
            )
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

    private val remoteProductsService:
            RemoteProductsService by lazy {

        retrofit.create(
            RemoteProductsService::class.java
        )
    }

    override val remoteProductsRepository:
            RemoteProductsRepository by lazy {

        RetrofitRemoteProductsRepository(
            remoteProductsService
        )
    }

    // =========================================
    // CATÁLOGO EDUCATIVO EXISTENTE
    // =========================================

    /**
     * Este catálogo de demostración se conserva
     * para no afectar las funcionalidades anteriores.
     */
    override val catalogRepository:
            CatalogRepository<DemoProduct> by lazy {

        InMemoryCatalogRepository(
            DemoCatalogData.products
        )
    }

    // =========================================
    // SQLITE - LABORATORIO EXISTENTE
    // =========================================

    override val sqlLabRepository:
            SqlLabRepository by lazy {

        SqliteSqlLabRepository(
            SqlLabDatabase(appContext)
        )
    }

    // =========================================
    // ROOM - LABORATORIO EDUCATIVO
    // =========================================

    private val roomDemoDatabase:
            RoomDemoDatabase by lazy {

        Room.databaseBuilder(
            appContext.applicationContext,
            RoomDemoDatabase::class.java,
            "farmapp_room_demo.db"
        ).build()
    }

    override val roomDemoRepository:
            RoomDemoRepository by lazy {

        OfflineRoomDemoRepository(
            roomDemoDatabase.roomDemoDao()
        )
    }

    // =========================================
    // DATASTORE - PREFERENCIAS
    // =========================================

    override val userPreferencesRepository:
            UserPreferencesRepository by lazy {

        createUserPreferencesRepository(
            appContext
        )
    }

    // =========================================
    // WORKMANAGER - TAREAS EN SEGUNDO PLANO
    // =========================================

    override val inventoryReportRepository:
            InventoryReportRepository by lazy {

        WorkManagerInventoryReportRepository(
            appContext
        )
    }

    // =========================================
    // HU01 - FIREBASE AUTHENTICATION
    // =========================================

    override val authRepository:
            AuthRepository by lazy {

        FirebaseAuthRepository(
            FirebaseAuth.getInstance()
        )
    }
}