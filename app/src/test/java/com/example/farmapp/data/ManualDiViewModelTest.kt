package com.example.farmapp.data

import com.example.farmapp.auth.AuthRepository
import com.example.farmapp.auth.MainDispatcherRule

import com.example.farmapp.catalog.CatalogRepository
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.InMemoryCatalogRepository

import com.example.farmapp.data.background.InventoryReportRepository
import com.example.farmapp.data.background.ReportWorkUiState

// HU03 - Repositorio del catalogo persistente.
import com.example.farmapp.data.catalog.CatalogProductRepository

// HU04 - Repositorio del carrito offline.
import com.example.farmapp.data.cart.CartRepository

import com.example.farmapp.data.preferences.UserPreferencesRepository

import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.data.room.RoomDemoRepository

import com.example.farmapp.data.sql.SqlLabQuery
import com.example.farmapp.data.sql.SqlLabRepository
import com.example.farmapp.data.sql.SqlLabResult

import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsRepository
import com.example.farmapp.network.RemoteProductsViewModel
// HU04 - Paso 13A: repositorio de pedidos.
import com.example.farmapp.data.orders.OrderRepository

// HU02 - Repositorio de perfiles.
import com.example.farmapp.profile.ProfileRepository

import java.io.IOException

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas de inyeccion manual de dependencias.
 *
 * Utiliza implementaciones falsas para evitar
 * conexiones reales con Firebase, Retrofit y Room.
 *
 * Compatible con:
 * HU01 - Autenticacion.
 * HU02 - Perfiles.
 * HU03 - Catalogo.
 * HU04 - Carrito offline.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManualDiViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    /**
     * Contenedor falso para pruebas unitarias.
     *
     * Solo simulamos el repositorio remoto
     * que se utiliza directamente en estas pruebas.
     *
     * Las demas propiedades se mantienen
     * para cumplir el contrato AppContainer.
     */
    private class FakeAppContainer(
        override val remoteProductsRepository: RemoteProductsRepository
    ) : AppContainer {

        // =========================================
        // HU02 - REPOSITORIO DE PERFILES
        // =========================================

        override val profileRepository: ProfileRepository
            get() = error(
                "El perfil no se utiliza en esta prueba"
            )

        // =========================================
        // HU03 - REPOSITORIO DE CATALOGO ROOM
        // =========================================

        override val catalogProductRepository: CatalogProductRepository
            get() = error(
                "El catalogo Room no se utiliza en esta prueba"
            )

        // =========================================
        // HU04 - REPOSITORIO DEL CARRITO ROOM
        // =========================================

        override val cartRepository: CartRepository
            get() = error(
                "El carrito HU04 no se utiliza en esta prueba"
            )
        // =========================================
        // HU04 - PASO 13A: PEDIDOS EN FIRESTORE
        // =========================================

        override val orderRepository: OrderRepository
            get() = error(
                "Los pedidos Firestore no se utilizan en esta prueba"
            )

        // =========================================
        // WORKMANAGER
        // =========================================

        override val inventoryReportRepository:
                InventoryReportRepository =
            object : InventoryReportRepository {

                override val status: Flow<ReportWorkUiState> =
                    flowOf(ReportWorkUiState())

                override fun enqueueReport() = Unit

                override fun cancelReport() = Unit
            }

        // =========================================
        // DATASTORE
        // =========================================

        override val userPreferencesRepository:
                UserPreferencesRepository
            get() = error(
                "No se consulta la preferencia en estas pruebas de red"
            )

        // =========================================
        // ROOM EDUCATIVO EXISTENTE
        // =========================================

        override val roomDemoRepository: RoomDemoRepository =
            object : RoomDemoRepository {

                override val items =
                    flowOf(emptyList<RoomDemoItem>())

                override fun observeItem(id: Long) =
                    flowOf(null as RoomDemoItem?)

                override suspend fun updateRecord(
                    id: Long,
                    name: String,
                    priceCents: Long,
                    stock: Int
                ) = false

                override suspend fun sellOne(
                    id: Long
                ) = false

                override suspend fun deleteRecord(
                    id: Long
                ) = false

                override suspend fun add(
                    item: RoomDemoItem
                ) = Unit

                override suspend fun increaseStock(
                    id: Long
                ) = Unit

                override suspend fun remove(
                    item: RoomDemoItem
                ) = Unit
            }

        // =========================================
        // SQLITE EDUCATIVO
        // =========================================

        override val sqlLabRepository: SqlLabRepository =
            object : SqlLabRepository {

                override suspend fun runQuery(
                    query: SqlLabQuery
                ): SqlLabResult {

                    return SqlLabResult.Products(emptyList())
                }

                override suspend fun insertExample() = true

                override suspend fun updateExample() = 1

                override suspend fun deleteExample() = 1

                override suspend fun resetExamples() = Unit
            }

        // =========================================
        // CATALOGO EDUCATIVO
        // =========================================

        override val catalogRepository:
                CatalogRepository<DemoProduct> =
            InMemoryCatalogRepository(emptyList())

        // =========================================
        // HU01 - AUTENTICACION
        // =========================================

        override val authRepository: AuthRepository =
            object : AuthRepository {

                override fun currentUserEmail(): String? = null

                override fun isAuthenticated() = false

                override suspend fun signIn(
                    email: String,
                    password: String
                ) = Unit

                override suspend fun register(
                    email: String,
                    password: String
                ) = Unit

                override suspend fun signInWithGoogleIdToken(
                    idToken: String
                ) {
                    error("Google no simulado")
                }

                override suspend fun resetPassword(
                    email: String
                ) = Unit

                override fun signOut() = Unit
            }
    }

    // =========================================
    // PRUEBA 1 - RUTA EXITOSA
    // =========================================

    @Test
    fun rutaExitosa_viewModelRecibeRepositorioDesdeContenedorFalso() =
        runTest(main.dispatcher) {

            val container: AppContainer = FakeAppContainer(
                object : RemoteProductsRepository {

                    override suspend fun loadSkinCare() =
                        listOf(
                            RemoteProduct(
                                10,
                                "Producto demo",
                                9.99
                            )
                        )

                    override suspend fun previewRawJson() = "{}"
                }
            )

            val vm = RemoteProductsViewModel(
                container.remoteProductsRepository
            )

            vm.refresh()

            advanceUntilIdle()

            assertEquals(
                RemoteProductsPhase.SUCCESS,
                vm.uiState.value.phase
            )

            assertEquals(
                10,
                vm.uiState.value.products.single().id
            )
        }

    // =========================================
    // PRUEBA 2 - ERROR DE RED
    // =========================================

    @Test
    fun rutaError_aplicacionNoNecesitaRedParaProbarEstado() =
        runTest(main.dispatcher) {

            val container: AppContainer = FakeAppContainer(
                object : RemoteProductsRepository {

                    override suspend fun loadSkinCare():
                            List<RemoteProduct> {

                        throw IOException("sin conexion")
                    }

                    override suspend fun previewRawJson() = "{}"
                }
            )

            val vm = RemoteProductsViewModel(
                container.remoteProductsRepository
            )

            vm.refresh()

            advanceUntilIdle()

            assertEquals(
                RemoteProductsPhase.ERROR,
                vm.uiState.value.phase
            )
        }

    // =========================================
    // PRUEBA 3 - CONTENEDORES INDEPENDIENTES
    // =========================================

    @Test
    fun casoLimite_contenedoresDePruebaIndependientes() =
        runTest(main.dispatcher) {

            fun fake(ids: List<Int>) = FakeAppContainer(
                object : RemoteProductsRepository {

                    override suspend fun loadSkinCare() =
                        ids.map {
                            RemoteProduct(
                                it,
                                "Demo $it",
                                1.0
                            )
                        }

                    override suspend fun previewRawJson() = "{}"
                }
            )

            val first = RemoteProductsViewModel(
                fake(listOf(1)).remoteProductsRepository
            )

            val second = RemoteProductsViewModel(
                fake(emptyList()).remoteProductsRepository
            )

            first.refresh()
            second.refresh()

            advanceUntilIdle()

            assertEquals(
                RemoteProductsPhase.SUCCESS,
                first.uiState.value.phase
            )

            assertEquals(
                RemoteProductsPhase.EMPTY,
                second.uiState.value.phase
            )

            assertTrue(
                second.uiState.value.products.isEmpty()
            )
        }
}