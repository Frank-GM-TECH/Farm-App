package com.example.farmapp.network

/** Repositorio inyectable: el ViewModel no conoce Retrofit ni JSON. */
interface RemoteProductsRepository {
    suspend fun loadSkinCare(): List<RemoteProduct>
    suspend fun previewRawJson(): String
}

class RetrofitRemoteProductsRepository(
    private val service: RemoteProductsService
) : RemoteProductsRepository {
    override suspend fun loadSkinCare(): List<RemoteProduct> =
        service.getSkinCareProducts().products.map(RemoteProductDto::toDomain)

    override suspend fun previewRawJson(): String = service.getRawSkinCareJson(limit = 2)
}
