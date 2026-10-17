package com.example.farmapp.auth

/**
 * Encapsula la limpieza del correo antes de enviarlo al repositorio.
 * Demuestra constructor, setter personalizado y getter calculado.
 */
class EmailDraft(initialValue: String) {
    var value: String = ""
        set(newValue) {
            field = newValue.trim()
        }

    val isValid: Boolean
        get() = AuthValidator.isValidEmail(value)

    init {
        value = initialValue
    }
}
