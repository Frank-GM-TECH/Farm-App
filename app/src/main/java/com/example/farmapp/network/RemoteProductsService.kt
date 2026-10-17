package com.example.farmapp.network

import retrofit2.http.GET
import retrofit2.http.Query

/** API pública de datos falsos, sin autenticación, pacientes ni medicamentos. */
interface RemoteProductsService {
    @GET("products/category/skin-care")
    suspend fun getSkinCareProducts(@Query("limit") limit: Int = 8): RemoteProductsResponse

    /** Paso inicial del codelab: obtener el JSON como un String, sin parsearlo. */
    @GET("products/category/skin-care")
    suspend fun getRawSkinCareJson(@Query("limit") limit: Int = 2): String
}
