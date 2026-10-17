package com.example.farmapp.catalog

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cotejo 15: estrategia éxito/error/casos límite para StateFlow y ViewModel.
 * Cada método crea SU PROPIO ViewModel y repositorio in-memory: sin Firebase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelStrategyTest {
    private fun createViewModel(
        products: List<DemoProduct> = DemoCatalogData.products,
        handle: SavedStateHandle = SavedStateHandle()
    ): CatalogViewModel = CatalogViewModel(handle, InMemoryCatalogRepository(products))

    // Ruta de éxito
    @Test fun exito_busquedaPorSkuDevuelveProductoEsperado() {
        val vm = createViewModel()
        vm.onQueryChanged("fa-004")
        assertEquals("FA-004", vm.uiState.value.snapshot.products.single().sku)
        assertEquals(1, vm.uiState.value.snapshot.products.size)
    }

    // Ruta de error de negocio: resultado vacío, sin excepciones ni cantidades negativas.
    @Test fun error_consultaSinCoincidenciasProduceEstadoVacio() {
        val vm = createViewModel()
        vm.onQueryChanged("producto inexistente")
        val result = vm.uiState.value.snapshot
        assertTrue(result.products.isEmpty())
        assertEquals(0, result.totalUnits)
        assertEquals(0L, result.totalInventoryValueCents)
    }

    // Caso límite: repositorio sin registros.
    @Test fun limite_repositorioVacioNoRompeElViewModel() {
        val vm = createViewModel(products = emptyList())
        val result = vm.uiState.value.snapshot
        assertTrue(result.products.isEmpty())
        assertTrue(result.categories.isEmpty())
        assertTrue(result.grouped.isEmpty())
        assertEquals(0, result.totalUnits)
    }

    // Caso límite: espacios en blanco equivalen a mostrar todos los productos.
    @Test fun limite_busquedaSoloConEspaciosDevuelveTodosLosProductos() {
        val vm = createViewModel()
        vm.onQueryChanged("   ")
        assertEquals(6, vm.uiState.value.snapshot.products.size)
        assertEquals(34, vm.uiState.value.snapshot.totalUnits)
    }

    @Test fun limite_categoriaDesconocidaGuardadaSeIgnora() {
        val vm = createViewModel(handle = SavedStateHandle(mapOf(
            CatalogViewModel.CATEGORY_KEY to "CATEGORIA_INCORRECTA"
        )))
        assertEquals(null, vm.uiState.value.selectedCategory)
        assertEquals(6, vm.uiState.value.snapshot.products.size)
    }

    @Test fun limite_eventoRepetidoNoRecreaElEstado() {
        val vm = createViewModel()
        val before = vm.uiState.value
        vm.onQueryChanged("")
        vm.onCategorySelected(null)
        assertSame(before, vm.uiState.value)
    }

    // Aislamiento: cada caso debe tener sus propias dependencias y SavedStateHandle.
    @Test fun aislamiento_unViewModelNoModificaElEstadoDeOtro() {
        val first = createViewModel()
        val second = createViewModel()
        first.onQueryChanged("gasas")
        first.onCategorySelected(DemoCategory.PRIMEROS_AUXILIOS)
        assertEquals("", second.uiState.value.query)
        assertEquals(null, second.uiState.value.selectedCategory)
        assertEquals(6, second.uiState.value.snapshot.products.size)
        assertEquals(1, first.uiState.value.snapshot.products.size)
    }

    // StateFlow: colector listo ANTES de disparar los eventos.
    // StateFlow es confluente; probamos estados observables, sin asumir todos los intermedios.
    @Test fun stateFlow_publicaEstadoInicialBusquedaYCategoria() = runTest {
        val vm = createViewModel()
        val observed = mutableListOf<CatalogUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.toList(observed)
        }
        assertEquals(6, observed.last().snapshot.products.size)
        vm.onQueryChanged("gel")
        assertEquals("gel", observed.last().query)
        assertEquals("FA-001", observed.last().snapshot.products.single().sku)
        vm.onCategorySelected(DemoCategory.HIGIENE)
        assertEquals(DemoCategory.HIGIENE, observed.last().selectedCategory)
        assertTrue(observed.size >= 2)
    }

    @Test fun estadoGuardadoConservaBusquedaYCategoriaEnNuevaInstancia() {
        val saved = SavedStateHandle()
        createViewModel(handle = saved).apply {
            onQueryChanged("gasas")
            onCategorySelected(DemoCategory.PRIMEROS_AUXILIOS)
        }
        val restore = SavedStateHandle(mapOf(
            CatalogViewModel.QUERY_KEY to saved.get<String>(CatalogViewModel.QUERY_KEY),
            CatalogViewModel.CATEGORY_KEY to saved.get<String>(CatalogViewModel.CATEGORY_KEY)
        ))
        val restoredVm = createViewModel(handle = restore)
        assertEquals("gasas", restoredVm.uiState.value.query)
        assertEquals(DemoCategory.PRIMEROS_AUXILIOS, restoredVm.uiState.value.selectedCategory)
        assertFalse(restoredVm.uiState.value.snapshot.products.isEmpty())
    }
}
