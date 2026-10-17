package com.example.farmapp.auth.password

import com.example.farmapp.auth.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordAdvisorTest {
    private val advisor = PasswordAdvisor()

    @Test fun vaciaNoCumple() {
        val result = advisor.evaluate("")
        assertEquals(PasswordStrength.EMPTY, result.strength)
        assertFalse(result.meetsMinimumForRegistration)
        assertEquals(3, result.checks.count { it is RequirementCheck.Missing })
    }

    @Test fun cortaNoCumple() {
        val result = advisor.evaluate("Ab1")
        assertEquals(PasswordStrength.SHORT, result.strength)
        assertFalse(result.meetsMinimumForRegistration)
    }

    @Test fun seisCaracteresEsValidaAunqueFaltenRecomendaciones() {
        val result = advisor.evaluate("abcdef")
        assertTrue(result.meetsMinimumForRegistration)
        assertEquals(PasswordStrength.BASIC, result.strength)
        assertTrue(result.checks.any { it is RequirementCheck.Missing && it.requirement == PasswordRequirement.CONTAINS_NUMBER })
    }

    @Test fun contrasenaMasComplejaMejoraIndicador() {
        val result = advisor.evaluate("Farmacia2026")
        assertEquals(PasswordStrength.IMPROVED, result.strength)
        assertEquals(3, result.completedChecks)
    }

    @Test fun funcionDevueltaPorOtraFuncionSePuedeInvocar() {
        val checker: (String) -> Boolean = AuthValidator.minimumLengthChecker(8)
        assertFalse(checker("1234567"))
        assertTrue(checker("12345678"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun minimoInvalidoEsRechazado() {
        PasswordAdvisor(minimumLength = 0)
    }
    @Test fun longitudMinimaExactaEsValida() {
        val result = advisor.evaluate("Ab1234")
        assertTrue(result.meetsMinimumForRegistration)
        assertEquals(3, result.completedChecks)
    }

    @Test fun nueveCaracteresNoActivaNivelMejorado() {
        val result = advisor.evaluate("Abcdef123")
        assertEquals(PasswordStrength.BASIC, result.strength)
    }

    @Test fun numeroYLetrasSinMinimoNoPermitenRegistro() {
        val result = advisor.evaluate("Ab12")
        assertFalse(result.meetsMinimumForRegistration)
    }

}
