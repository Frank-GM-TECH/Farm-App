package com.example.farmapp.data

import android.content.Context
import com.example.farmapp.data.background.InventoryReportRepository
import com.example.farmapp.data.background.WorkManagerInventoryReportRepository
import com.example.farmapp.data.sql.SqlLabRepository
import com.example.farmapp.data.sql.SqlLabDatabase
import com.example.farmapp.data.sql.SqliteSqlLabRepository
import androidx.room.Room
import com.example.farmapp.data.room.RoomDemoDatabase
import com.example.farmapp.data.room.RoomDemoRepository
import com.example.farmapp.data.room.OfflineRoomDemoRepository

import com.example.farmapp.data.preferences.UserPreferencesRepository
import com.example.farmapp.data.preferences.createUserPreferencesRepository
import com.example.farmapp.auth.AuthRepository
import com.example.farmapp.auth.FirebaseAuthRepository
import com.example.farmapp.catalog.CatalogRepository
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.InMemoryCatalogRepository
import com.example.farmapp.network.RemoteProductsRepository
import com.example.farmapp.network.RemoteProductsService
import com.example.farmapp.network.RetrofitRemoteProductsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.example.farmapp.profile.ProfileRepository
import com.example.farmapp.profile.FirestoreProfileRepository
import com.google.firebase.firestore.FirebaseFirestore

/** C22: contrato de dependencias; permite sustituir las implementaciones durante tests. */
interface AppContainer {
    val remoteProductsRepository: RemoteProductsRepository
    val catalogRepository: CatalogRepository<DemoProduct>
    val authRepository: AuthRepository
    val sqlLabRepository: SqlLabRepository
    val roomDemoRepository: RoomDemoRepository
    val userPreferencesRepository: UserPreferencesRepository
    val inventoryReportRepository: InventoryReportRepository
    val profileRepository: ProfileRepository
}

/**
 * Implementación para producción, siguiendo el codelab Mars Photos.
 * Ningún ViewModel crea Retrofit, FirebaseAuth ni repositorios concretos.
 * `by lazy` evita levantar clientes de red o Firebase hasta el primer acceso.
 * Las consultas REST son solo sobre productos de DEMOSTRACIÓN de DummyJSON.
 */
class DefaultAppContainer(private val appContext: Context) : AppContainer {
    override val profileRepository: ProfileRepository by lazy {
        FirestoreProfileRepository(
            FirebaseFirestore.getInstance()
        )
    }
    private val baseUrl = "https://dummyjson.com/"

    private val json by lazy {
        Json { ignoreUnknownKeys = true }
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val remoteProductsService: RemoteProductsService by lazy {
        retrofit.create(RemoteProductsService::class.java)
    }

    override val remoteProductsRepository: RemoteProductsRepository by lazy {
        RetrofitRemoteProductsRepository(remoteProductsService)
    }

    override val catalogRepository: CatalogRepository<DemoProduct> by lazy {
        InMemoryCatalogRepository(DemoCatalogData.products)
    }

    override val sqlLabRepository: SqlLabRepository by lazy {
        SqliteSqlLabRepository(SqlLabDatabase(appContext))
    }

    private val roomDemoDatabase: RoomDemoDatabase by lazy {
        Room.databaseBuilder(
            appContext.applicationContext, RoomDemoDatabase::class.java, "farmapp_room_demo.db"
        ).build()
    }

    override val roomDemoRepository: RoomDemoRepository by lazy {
        OfflineRoomDemoRepository(roomDemoDatabase.roomDemoDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        createUserPreferencesRepository(appContext)
    }

    override val inventoryReportRepository: InventoryReportRepository by lazy {
        WorkManagerInventoryReportRepository(appContext)
    }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }
}
