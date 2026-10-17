package com.example.farmapp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** C21: el JSON externo no es el modelo del inventario real de FarmApp. */
@Serializable
data class RemoteProductsResponse(
    @SerialName("products") val products: List<RemoteProductDto> = emptyList()
)

@Serializable
data class RemoteProductDto(
    val id: Int,
    @SerialName("title") val productName: String,
    val price: Double,
    val category: String,
    @SerialName("thumbnail") val thumbnail: String? = null
)

/** Modelo de dominio simple: mantiene separados datos externos y datos reales. */
data class RemoteProduct(
    val id: Int,
    val name: String,
    val priceUsd: Double,
    val imageUrl: String? = null
)

fun RemoteProductDto.toDomain() = RemoteProduct(
    id = id, name = productName, priceUsd = price,
    imageUrl = thumbnail.asSecureImageUrl()
)
