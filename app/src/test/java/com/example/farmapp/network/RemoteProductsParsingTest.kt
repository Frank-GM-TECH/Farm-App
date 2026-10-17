package com.example.farmapp.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Casos éxito, borde y error de JSON y del repositorio. Sin Internet. */
class RemoteProductsParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun exito_interpretaArrayYSerialName() {
        val payload = """{"products":[{"id":7,"title":"Crema facial","price":11.5,"category":"skin-care","rating":4.3}]}"""
        val decoded = json.decodeFromString<RemoteProductsResponse>(payload)
        assertEquals(1, decoded.products.size)
        assertEquals("Crema facial", decoded.products[0].productName)
        assertEquals(RemoteProduct(7, "Crema facial", 11.5), decoded.products[0].toDomain())
    }

    @Test fun limite_arrayVacioEsValido() {
        val result = json.decodeFromString<RemoteProductsResponse>("""{"products":[]}""")
        assertTrue(result.products.isEmpty())
    }

    @Test fun error_jsonInvalidoProduceExcepcion() {
        try {
            json.decodeFromString<RemoteProductsResponse>("""{"products": [""")
            throw AssertionError("Debió lanzar SerializationException")
        } catch (_: SerializationException) { }
    }

    @Test fun rawJson_retornaTextoSinParsear() = runTest {
        val fake = object : RemoteProductsService {
            override suspend fun getSkinCareProducts(limit: Int) = RemoteProductsResponse()
            override suspend fun getRawSkinCareJson(limit: Int) = "{\"products\":[]}"
        }
        assertEquals("{\"products\":[]}", RetrofitRemoteProductsRepository(fake).previewRawJson())
    }

    @Test fun exito_repositorioTransformaDtoEnDominio() = runTest {
        val fake = object : RemoteProductsService {
            override suspend fun getRawSkinCareJson(limit: Int): String = "{\"products\":[]}"
            override suspend fun getSkinCareProducts(limit: Int) = RemoteProductsResponse(
                listOf(RemoteProductDto(9, "Loción", 5.75, "skin-care"))
            )
        }
        assertEquals(
            listOf(RemoteProduct(9, "Loción", 5.75)),
            RetrofitRemoteProductsRepository(fake).loadSkinCare()
        )
    }
}
