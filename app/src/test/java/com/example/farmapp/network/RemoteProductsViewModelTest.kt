package com.example.farmapp.network

import com.example.farmapp.auth.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** ViewModel con repositorio falso; no alcanza Internet ni Firebase. */
@OptIn(ExperimentalCoroutinesApi::class)
class RemoteProductsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun exito_emiteCargaYResultado() = runTest(main.dispatcher) {
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun previewRawJson() = "{\"products\":[]}"
            override suspend fun loadSkinCare(): List<RemoteProduct> {
                delay(100)
                return listOf(RemoteProduct(1, "Crema", 4.0))
            }
        })
        vm.refresh()
        assertEquals(RemoteProductsPhase.LOADING, vm.uiState.value.phase)
        advanceUntilIdle()
        assertEquals(RemoteProductsPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals(1, vm.uiState.value.products.size)
    }

    @Test fun limite_respuestaVacia() = runTest(main.dispatcher) {
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun previewRawJson() = "{\"products\":[]}"
            override suspend fun loadSkinCare() = emptyList<RemoteProduct>()
        })
        vm.refresh(); advanceUntilIdle()
        assertEquals(RemoteProductsPhase.EMPTY, vm.uiState.value.phase)
    }

    @Test fun error_sinInternetSePuedeReintentar() = runTest(main.dispatcher) {
        var disconnected = true
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun previewRawJson() = "{\"products\":[]}"
            override suspend fun loadSkinCare(): List<RemoteProduct> {
                if (disconnected) throw IOException("sin red")
                return listOf(RemoteProduct(2, "Loción", 12.0))
            }
        })
        vm.refresh(); advanceUntilIdle()
        assertEquals(RemoteProductsPhase.ERROR, vm.uiState.value.phase)
        assertEquals(RemoteProductsError.NETWORK, vm.uiState.value.error)
        disconnected = false
        vm.refresh(); advanceUntilIdle()
        assertEquals(RemoteProductsPhase.SUCCESS, vm.uiState.value.phase)
    }

    @Test fun exito_previsualizacionJsonCrudoConScalars() = runTest(main.dispatcher) {
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun loadSkinCare() = emptyList<RemoteProduct>()
            override suspend fun previewRawJson() = "{\"products\":[]}"
        })
        vm.previewJson(); advanceUntilIdle()
        assertEquals(RemoteProductsPhase.RAW_SUCCESS, vm.uiState.value.phase)
        assertEquals("{\"products\":[]}", vm.uiState.value.rawJsonPreview)
    }

    @Test fun limite_dobleClickNoDuplicaSolicitud() = runTest(main.dispatcher) {
        var count = 0
        val pending = CompletableDeferred<List<RemoteProduct>>()
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun previewRawJson() = "{\"products\":[]}"
            override suspend fun loadSkinCare(): List<RemoteProduct> {
                count++
                return pending.await()
            }
        })
        vm.refresh(); runCurrent()
        vm.refresh(); runCurrent()
        assertEquals(1, count)
        pending.complete(emptyList())
        advanceUntilIdle()
        assertEquals(RemoteProductsPhase.EMPTY, vm.uiState.value.phase)
    }

    @Test fun galeria_exitoExponeUrlDeImagenEnUiState() = runTest(main.dispatcher) {
        val vm = RemoteProductsViewModel(object : RemoteProductsRepository {
            override suspend fun previewRawJson() = "{\"products\":[]}"
            override suspend fun loadSkinCare() = listOf(
                RemoteProduct(31, "Crema de muestra", 8.0, "https://cdn.example.test/31.png")
            )
        })
        vm.refresh()
        advanceUntilIdle()
        assertEquals(RemoteProductsPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals("https://cdn.example.test/31.png", vm.uiState.value.products.single().imageUrl)
    }

}
