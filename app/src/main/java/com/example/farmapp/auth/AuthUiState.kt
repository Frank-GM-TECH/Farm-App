package com.example.farmapp.auth

/**
 * Estado inmutable. Los mensajes son IDs de strings.xml, no textos literales.
 * La capa Compose resuelve el idioma usando stringResource(id).
 */
data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null,
    val infoMessageRes: Int? = null
)
