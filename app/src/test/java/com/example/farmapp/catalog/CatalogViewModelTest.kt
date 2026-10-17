package com.example.farmapp.catalog

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas locales del ViewModel del catálogo: NO requieren Firebase. */
class CatalogViewModelTest {
    private fun newVm(handle: SavedStateHandle = SavedStateHandle()) =
        CatalogViewModel(handle, InMemoryCatalogRepository(DemoCatalogData.products))

    @Test fun estadoInicialContieneSeisProductos() {
        val state = newVm().uiState.value
        assertEquals("", state.query)
        assertEquals(null, state.selectedCategory)
        assertEquals(6, state.snapshot.products.size)
        assertEquals(34, state.snapshot.totalUnits)
    }

    @Test fun cambiarBusquedaActualizaUiState() {
        val vm = newVm()
        vm.onQueryChanged("  TERMÓMETRO ")
        assertEquals("  TERMÓMETRO ", vm.uiState.value.query)
        assertEquals("FA-006", vm.uiState.value.snapshot.products.single().sku)
    }

    @Test fun seleccionarCategoriaRecalculaResultados() {
        val vm = newVm()
        vm.onCategorySelected(DemoCategory.HIGIENE)
        assertEquals(DemoCategory.HIGIENE, vm.uiState.value.selectedCategory)
        assertEquals(3, vm.uiState.value.snapshot.products.size)
        assertEquals(19, vm.uiState.value.snapshot.totalUnits)
    }

    @Test fun combinarBusquedaYCategoriaYRestablecerFiltro() {
        val vm = newVm()
        vm.onQueryChanged("gasas")
        vm.onCategorySelected(DemoCategory.HIGIENE)
        assertTrue(vm.uiState.value.snapshot.products.isEmpty())
        vm.onCategorySelected(null)
        assertEquals(1, vm.uiState.value.snapshot.products.size)
    }

    @Test fun mismoViewModelConservaEstadoEntreLecturasDeLaUi() {
        val vm = newVm()
        vm.onQueryChanged("venda")
        val stateBeforeNewComposition = vm.uiState.value
        // Al volver a recoger el StateFlow tras recrear una composición,
        // se obtiene el último valor del mismo ViewModel.
        assertEquals(stateBeforeNewComposition, vm.uiState.value)
        assertEquals("FA-005", vm.uiState.value.snapshot.products.single().sku)
    }

    @Test fun savedStateHandleRecuperaFiltrosCuandoSeRestauraEstado() {
        val handle = SavedStateHandle()
        val first = newVm(handle)
        first.onQueryChanged("gasas")
        first.onCategorySelected(DemoCategory.PRIMEROS_AUXILIOS)

        // Simulación del Bundle restaurado; en Android esto lo realiza el sistema.
        val restoredHandle = SavedStateHandle(mapOf(
            CatalogViewModel.QUERY_KEY to handle.get<String>(CatalogViewModel.QUERY_KEY),
            CatalogViewModel.CATEGORY_KEY to handle.get<String>(CatalogViewModel.CATEGORY_KEY)
        ))
        val restored = newVm(restoredHandle)
        assertEquals("gasas", restored.uiState.value.query)
        assertEquals(DemoCategory.PRIMEROS_AUXILIOS, restored.uiState.value.selectedCategory)
        assertEquals("FA-004", restored.uiState.value.snapshot.products.single().sku)
    }

    @Test fun categoriaDesconocidaRestauradaSeTrataComoTodas() {
        val vm = newVm(SavedStateHandle(mapOf(CatalogViewModel.CATEGORY_KEY to "DESCONOCIDA")))
        assertEquals(null, vm.uiState.value.selectedCategory)
        assertFalse(vm.uiState.value.snapshot.products.isEmpty())
    }
}
