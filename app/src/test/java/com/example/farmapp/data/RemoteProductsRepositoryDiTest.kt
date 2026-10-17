package com.example.farmapp.data

import com.example.farmapp.network.RemoteProductDto
import com.example.farmapp.network.RemoteProductsResponse
import com.example.farmapp.network.RemoteProductsService
import com.example.farmapp.network.RetrofitRemoteProductsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Los tests de repositorio usan un servicio FALSO sin conexión HTTP. */
class RemoteProductsRepositoryDiTest {
    private class FakeService : RemoteProductsService {
        var listCalls = 0
        var rawCalls = 0

        override suspend fun getSkinCareProducts(limit: Int): RemoteProductsResponse {
            listCalls++
            return RemoteProductsResponse(listOf(
                RemoteProductDto(id = 5, productName = "Crema educativa", price = 8.75,
                    category = "skin-care")
            ))
        }

        override suspend fun getRawSkinCareJson(limit: Int): String {
            rawCalls++
            return "{\"products\":[]}"
        }
    }

    @Test fun exito_convierteDtoSinAccederAInternet() = runTest {
        val service = FakeService()
        val repository = RetrofitRemoteProductsRepository(service)
        val products = repository.loadSkinCare()
        assertEquals(1, service.listCalls)
        assertEquals(5, products.single().id)
        assertEquals("Crema educativa", products.single().name)
        assertEquals(8.75, products.single().priceUsd, 0.001)
    }

    @Test fun exito_recuperaJsonDeServicioFalso() = runTest {
        val service = FakeService()
        val repository = RetrofitRemoteProductsRepository(service)
        assertEquals("{\"products\":[]}", repository.previewRawJson())
        assertEquals(1, service.rawCalls)
    }
}
