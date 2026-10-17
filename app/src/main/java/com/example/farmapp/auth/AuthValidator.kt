package com.example.farmapp.auth

/** Funciones sin dependencias Android para facilitar las pruebas unitarias. */
object AuthValidator {
    const val MIN_PASSWORD_LENGTH: Int = 6
    private val emailPattern = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Función que DEVUELVE otra función; la lambda recuerda `minimum`. */
    fun minimumLengthChecker(minimum: Int): (String) -> Boolean {
        require(minimum >= 1) { "La longitud mínima debe ser positiva" }
        return { text -> text.length >= minimum }
    }

    private val isLongEnough: (String) -> Boolean = minimumLengthChecker(MIN_PASSWORD_LENGTH)

    fun isValidEmail(email: String): Boolean = emailPattern.matches(email.trim())

    fun isValidPassword(password: String): Boolean = isLongEnough(password)
}
