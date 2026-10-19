package com.example.farmapp.data

import android.content.Context
import androidx.room.Room

// =========================================
// HU01 - AUTENTICACION
// =========================================

import com.example.farmapp.auth.AuthRepository
import com.example.farmapp.auth.FirebaseAuthRepository

// =========================================
// CATALOGO EDUCATIVO EXISTENTE
// =========================================

import com.example.farmapp.catalog.CatalogRepository
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.InMemoryCatalogRepository

// =========================================
// HU03 - CATALOGO PERSISTENTE CON ROOM
// =========================================

import com.example.farmapp.data.catalog.CatalogDatabase
import com.example.farmapp.data.catalog.CatalogProductRepository
import com.example.farmapp.data.catalog.OfflineCatalogProductRepository

// =========================================
// HU04 - CARRITO OFFLINE CON ROOM
// =========================================

import com.example.farmapp.data.cart.CartDatabase
import com.example.farmapp.data.cart.CartRepository
import com.example.farmapp.data.cart.OfflineCartRepository

// =========================================
// ROOM EDUCATIVO EXISTENTE
// =========================================

import com.example.farmapp.data.room.RoomDemoDatabase
import com.example.farmapp.data.room.RoomDemoRepository
import com.example.farmapp.data.room.OfflineRoomDemoRepository

// =========================================
// SQLITE EDUCATIVO EXISTENTE
// =========================================

import com.example.farmapp.data.sql.SqlLabDatabase
import com.example.farmapp.data.sql.SqlLabRepository
import com.example.farmapp.data.sql.SqliteSqlLabRepository

// =========================================
// DATASTORE - PREFERENCIAS
// =========================================

import com.example.farmapp.data.preferences.UserPreferencesRepository
import com.example.farmapp.data.preferences.createUserPreferencesRepository

// =========================================
// WORKMANAGER
// =========================================

import com.example.farmapp.data.background.InventoryReportRepository
import com.example.farmapp.data.background.WorkManagerInventoryReportRepository

// =========================================
// RETROFIT Y SERVICIOS REMOTOS
// =========================================

import com.example.farmapp.network.RemoteProductsRepository
import com.example.farmapp.network.RemoteProductsService
import com.example.farmapp.network.RetrofitRemoteProductsRepository

// =========================================
// HU02 - PERFIL DE USUARIO
// =========================================

import com.example.farmapp.profile.ProfileRepository
import com.example.farmapp.profile.FirestoreProfileRepository

// =========================================
// FIREBASE
// =========================================

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// =========================================
// RETROFIT Y SERIALIZACION
// =========================================

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

import com.example.farmapp.data.orders.OrderRepository
import com.example.farmapp.data.orders.FirestoreOrderRepository

/**
 * Contenedor principal de dependencias de FarmApp.
 *
 * Centraliza los repositorios utilizados por
 * los ViewModels y las distintas pantallas.
 *
 * HU01: Autenticacion.
 * HU02: Perfil.
 * HU03: Catalogo persistente.
 * HU04: Carrito de compras offline.
 *
 * Permite sustituir los repositorios reales
 * por implementaciones falsas durante
 * las pruebas unitarias.
 */
interface AppContainer {

    // HU04 - Paso 13A: pedidos en Firestore.
    val orderRepository: OrderRepository

    // =====================================
    // SERVICIOS REMOTOS
    // =====================================

    val remoteProductsRepository: RemoteProductsRepository

    // =====================================
    // CATALOGO EDUCATIVO
    // =====================================

    val catalogRepository: CatalogRepository<DemoProduct>

    // =====================================
    // HU01 - AUTENTICACION
    // =====================================

    val authRepository: AuthRepository

    // =====================================
    // LABORATORIO SQLITE
    // =====================================

    val sqlLabRepository: SqlLabRepository

    // =====================================
    // LABORATORIO ROOM
    // =====================================

    val roomDemoRepository: RoomDemoRepository

    // =====================================
    // DATASTORE
    // =====================================

    val userPreferencesRepository: UserPreferencesRepository

    // =====================================
    // WORKMANAGER
    // =====================================

    val inventoryReportRepository: InventoryReportRepository

    // =====================================
    // HU02 - PERFIL
    // =====================================

    val profileRepository: ProfileRepository

    // =====================================
    // HU03 - CATALOGO CON ROOM
    // =====================================

    val catalogProductRepository: CatalogProductRepository

    // =====================================
    // HU04 - CARRITO CON ROOM
    // =====================================

    val cartRepository: CartRepository
}

/**
 * Implementacion real del contenedor.
 *
 * Utiliza inicializacion lazy para crear
 * bases de datos y repositorios solamente
 * cuando alguna funcionalidad los necesite.
 *
 * Esto evita inicializaciones innecesarias
 * durante el inicio de la aplicacion.
 */
class DefaultAppContainer(
    private val appContext: Context
) : AppContainer {

    // HU04 - Paso 13A: repositorio de pedidos.
    override val orderRepository: OrderRepository by lazy {
        FirestoreOrderRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
    }

    // =========================================
    // HU02 - PERFIL DE USUARIO CON FIRESTORE
    // =========================================

    override val profileRepository: ProfileRepository by lazy {

        FirestoreProfileRepository(
            FirebaseFirestore.getInstance()
        )
    }

    // =========================================
    // HU03 - BASE DE DATOS DEL CATALOGO
    // =========================================

    /**
     * Base de datos Room para los productos.
     *
     * Guarda:
     * - SKU
     * - Codigo de barras
     * - Nombre
     * - Categoria
     * - Precio
     * - Stock
     * - Imagen
     *
     * Esta base no se modifica al incorporar
     * el carrito de HU04.
     */
    private val catalogDatabase: CatalogDatabase by lazy {

        Room.databaseBuilder(
            appContext.applicationContext,
            CatalogDatabase::class.java,
            "farmapp_catalog.db"
        )
            .addMigrations(CatalogDatabase.MIGRATION_1_2)
            .build()
    }

    /**
     * Repositorio del catalogo HU03.
     *
     * Se utiliza tanto en Inicio y Categorias
     * como para validar los productos
     * agregados al carrito.
     */
    override val catalogProductRepository:
            CatalogProductRepository by lazy {

        OfflineCatalogProductRepository(
            catalogDatabase.catalogProductDao()
        )
    }

    // =========================================
    // HU04 - BASE DE DATOS DEL CARRITO
    // =========================================

    /**
     * Nueva base de datos Room exclusiva
     * para el carrito de compras.
     *
     * Archivo:
     * farmapp_cart.db
     *
     * Mantiene separados los datos de HU03.
     * Cada producto del carrito se identifica
     * mediante userId y SKU.
     */
    private val cartDatabase: CartDatabase by lazy {

        Room.databaseBuilder(
            appContext.applicationContext,
            CartDatabase::class.java,
            "farmapp_cart.db"
        ).build()
    }

    /**
     * Repositorio del carrito HU04.
     *
     * Recibe:
     *
     * 1. CartItemDao:
     *    Para guardar y consultar el carrito.
     *
     * 2. CatalogProductRepository:
     *    Para consultar precios y stock.
     *
     * 3. FirebaseAuth:
     *    Para identificar al usuario actual.
     *
     * Esto permite reutilizar los productos
     * existentes sin duplicar el catalogo.
     */
    override val cartRepository: CartRepository by lazy {

        OfflineCartRepository(
            dao = cartDatabase.cartItemDao(),
            catalogRepository = catalogProductRepository,
            firebaseAuth = FirebaseAuth.getInstance()
        )
    }

    // =========================================
    // RETROFIT - PRODUCTOS REMOTOS
    // =========================================

    /**
     * Servicio remoto educativo existente.
     *
     * Se conserva para no afectar los
     * modulos desarrollados anteriormente.
     */
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
    // CATALOGO EDUCATIVO EXISTENTE
    // =========================================

    /**
     * Mantiene el catalogo educativo original.
     *
     * No se reemplaza por el catalogo comercial,
     * ya que ambos tienen objetivos diferentes.
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

    /**
     * Base de datos utilizada en las
     * demostraciones educativas de Room.
     *
     * Independiente de las bases:
     * - farmapp_catalog.db
     * - farmapp_cart.db
     */
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

    /**
     * Mantiene el inicio y cierre de sesion
     * existente de HU01.
     *
     * El carrito utiliza el mismo FirebaseAuth
     * para consultar el UID autenticado.
     */
    override val authRepository:
            AuthRepository by lazy {

        FirebaseAuthRepository(
            FirebaseAuth.getInstance()
        )
    }
}