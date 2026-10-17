package com.example.farmapp.data

import com.example.farmapp.data.background.InventoryReportRepository
import com.example.farmapp.data.background.ReportWorkUiState
import kotlinx.coroutines.flow.Flow
import com.example.farmapp.data.preferences.UserPreferencesRepository
import com.example.farmapp.auth.AuthRepository
import com.example.farmapp.auth.MainDispatcherRule
import com.example.farmapp.catalog.CatalogRepository
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.InMemoryCatalogRepository
import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.data.sql.SqlLabRepository
import com.example.farmapp.data.room.RoomDemoRepository
import com.example.farmapp.data.room.RoomDemoItem
import kotlinx.coroutines.flow.flowOf
import com.example.farmapp.data.sql.SqlLabQuery
import com.example.farmapp.data.sql.SqlLabResult
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsRepository
import com.example.farmapp.network.RemoteProductsViewModel
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** C22: sustitución explícita de dependencias, sin Firebase ni Retrofit reales. */
@OptIn(ExperimentalCoroutinesApi::class)
class ManualDiViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private class FakeAppContainer(
        override val remoteProductsRepository: RemoteProductsRepository
    ) : AppContainer {
        override val inventoryReportRepository: InventoryReportRepository = object : InventoryReportRepository {
            override val status: Flow<ReportWorkUiState> = flowOf(ReportWorkUiState())
            override fun enqueueReport() = Unit
            override fun cancelReport() = Unit
        }
        override val userPreferencesRepository: UserPreferencesRepository
            get() = error("No se consulta la preferencia en estas pruebas de red")
        override val roomDemoRepository: RoomDemoRepository = object : RoomDemoRepository {
            override val items = flowOf(emptyList<RoomDemoItem>())
            override fun observeItem(id: Long) = flowOf(null as RoomDemoItem?)
            override suspend fun updateRecord(id: Long, name: String, priceCents: Long, stock: Int) = false
            override suspend fun sellOne(id: Long) = false
            override suspend fun deleteRecord(id: Long) = false
            override suspend fun add(item: RoomDemoItem) = Unit
            override suspend fun increaseStock(id: Long) = Unit
            override suspend fun remove(item: RoomDemoItem) = Unit
        }
        override val sqlLabRepository: SqlLabRepository = object : SqlLabRepository {
            override suspend fun runQuery(query: SqlLabQuery): SqlLabResult = SqlLabResult.Products(emptyList())
            override suspend fun insertExample() = true
            override suspend fun updateExample() = 1
            override suspend fun deleteExample() = 1
            override suspend fun resetExamples() = Unit
        }
        override val catalogRepository: CatalogRepository<DemoProduct> = InMemoryCatalogRepository(emptyList())
        override val authRepository: AuthRepository = object : AuthRepository {
            override fun currentUserEmail(): String? = null
            override fun isAuthenticated() = false
            override suspend fun signIn(email: String, password: String) = Unit
            override suspend fun register(email: String, password: String) = Unit
            override suspend fun signInWithGoogleIdToken(idToken: String) { error("Google no simulado") }
            override suspend fun resetPassword(email: String) = Unit
            override fun signOut() = Unit
        }
    }

    @Test fun rutaExitosa_viewModelRecibeRepositorioDesdeContenedorFalso() = runTest(main.dispatcher) {
        val container: AppContainer = FakeAppContainer(object : RemoteProductsRepository {
            override suspend fun loadSkinCare() = listOf(RemoteProduct(10, "Producto demo", 9.99))
            override suspend fun previewRawJson() = "{}"
        })
        val vm = RemoteProductsViewModel(container.remoteProductsRepository)
        vm.refresh()
        advanceUntilIdle()
        assertEquals(RemoteProductsPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals(10, vm.uiState.value.products.single().id)
    }

    @Test fun rutaError_aplicacionNoNecesitaRedParaProbarEstado() = runTest(main.dispatcher) {
        val container: AppContainer = FakeAppContainer(object : RemoteProductsRepository {
            override suspend fun loadSkinCare(): List<RemoteProduct> = throw IOException("sin conexion")
            override suspend fun previewRawJson() = "{}"
        })
        val vm = RemoteProductsViewModel(container.remoteProductsRepository)
        vm.refresh()
        advanceUntilIdle()
        assertEquals(RemoteProductsPhase.ERROR, vm.uiState.value.phase)
    }

    @Test fun casoLimite_contenedoresDePruebaIndependientes() = runTest(main.dispatcher) {
        fun fake(ids: List<Int>) = FakeAppContainer(object : RemoteProductsRepository {
            override suspend fun loadSkinCare() = ids.map { RemoteProduct(it, "Demo $it", 1.0) }
            override suspend fun previewRawJson() = "{}"
        })
        val first = RemoteProductsViewModel(fake(listOf(1)).remoteProductsRepository)
        val second = RemoteProductsViewModel(fake(emptyList()).remoteProductsRepository)
        first.refresh(); second.refresh(); advanceUntilIdle()
        assertEquals(RemoteProductsPhase.SUCCESS, first.uiState.value.phase)
        assertEquals(RemoteProductsPhase.EMPTY, second.uiState.value.phase)
        assertTrue(second.uiState.value.products.isEmpty())
    }
}
