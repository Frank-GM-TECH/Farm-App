package com.example.farmapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun emailValidoEsAceptado() {
        assertTrue(AuthValidator.isValidEmail("cliente@farmapp.pe"))
    }

    @Test
    fun emailSinArrobaEsRechazado() {
        assertFalse(AuthValidator.isValidEmail("cliente.farmapp.pe"))
    }

    @Test
    fun contrasenaCortaEsRechazada() {
        assertFalse(AuthValidator.isValidPassword("12345"))
    }

    @Test
    fun contrasenaDeSeisCaracteresEsAceptada() {
        assertTrue(AuthValidator.isValidPassword("123456"))
    }
    @Test
    fun emailConEspaciosExternosEsValido() {
        assertTrue(AuthValidator.isValidEmail("  cliente@farmapp.pe  "))
    }

    @Test
    fun correoSinDominioEsRechazado() {
        assertFalse(AuthValidator.isValidEmail("cliente@"))
    }

    @Test
    fun correoConTldDeUnSoloCaracterEsRechazado() {
        assertFalse(AuthValidator.isValidEmail("cliente@farmapp.p"))
    }

    @Test
    fun verificarLongitudMinimaConFuncionDeOrdenSuperior() {
        val regla = AuthValidator.minimumLengthChecker(8)
        assertFalse(regla("1234567"))
        assertTrue(regla("12345678"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noPermiteLongitudMinimaNegativa() {
        AuthValidator.minimumLengthChecker(-1)
    }

}
