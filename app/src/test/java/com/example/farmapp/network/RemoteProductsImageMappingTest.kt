package com.example.farmapp.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Son pruebas JVM: no requieren red, Firebase ni cargar imágenes reales. */
class RemoteProductsImageMappingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun success_imageThumbnailJsonSeConvierte() {
        val payload = """{"products":[{"id":17,"title":"Loción","price":8.9,"category":"skin-care","thumbnail":"https://cdn.example.test/17.png"}]}"""
        val item = json.decodeFromString<RemoteProductsResponse>(payload).products.single().toDomain()
        assertEquals("https://cdn.example.test/17.png", item.imageUrl)
        assertEquals("Loción", item.name)
    }

    @Test fun limite_sinThumbnailMantieneLaCompatibilidad() {
        val payload = """{"products":[{"id":1,"title":"Crema","price":2.0,"category":"skin-care"}]}"""
        assertNull(json.decodeFromString<RemoteProductsResponse>(payload).products.single().toDomain().imageUrl)
    }

    @Test fun limite_thumbnailVacioUsaFallback() {
        assertNull("   ".asSecureImageUrl())
        assertNull(null.asSecureImageUrl())
    }

    @Test fun error_noUsaHttpSinCifrar() {
        assertNull("http://example.test/image.png".asSecureImageUrl())
    }
}
