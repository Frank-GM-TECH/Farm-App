package com.example.farmapp.auth.password

import com.example.farmapp.auth.AuthValidator

/** Requisitos del registro. Solo la longitud es obligatoria para HU01. */
enum class PasswordRequirement { MINIMUM_LENGTH, CONTAINS_LETTER, CONTAINS_NUMBER }

enum class PasswordStrength(val filledSegments: Int) {
    EMPTY(0), SHORT(1), BASIC(2), IMPROVED(3)
}

/** Sealed: las pantallas pueden distinguir variantes mediante `when` + `is`. */
sealed interface RequirementCheck {
    val requirement: PasswordRequirement

    data class Met(override val requirement: PasswordRequirement) : RequirementCheck
    data class Missing(override val requirement: PasswordRequirement) : RequirementCheck
}

data class PasswordAssessment(
    val strength: PasswordStrength,
    val checks: List<RequirementCheck>,
    val meetsMinimumForRegistration: Boolean
) {
    /** Getter calculado: ejemplo de accesor de propiedades personalizado. */
    val completedChecks: Int
        get() = checks.count { it is RequirementCheck.Met }
}

/** Abstracción de una regla. `protected` comparte una ayuda solo con las subclases. */
internal abstract class PasswordRule(val requirement: PasswordRequirement) {
    protected fun hasContent(password: String): Boolean = password.isNotEmpty()
    abstract fun matches(password: String): Boolean
}

internal class LengthRule(private val minimum: Int) : PasswordRule(PasswordRequirement.MINIMUM_LENGTH) {
    override fun matches(password: String): Boolean = hasContent(password) && password.length >= minimum
}

internal class LetterRule : PasswordRule(PasswordRequirement.CONTAINS_LETTER) {
    override fun matches(password: String): Boolean = password.any { it.isLetter() }
}

internal class NumberRule : PasswordRule(PasswordRequirement.CONTAINS_NUMBER) {
    override fun matches(password: String): Boolean = password.any { it.isDigit() }
}

/**
 * HAS-A: PasswordAdvisor contiene reglas. IS-A: cada regla hereda de PasswordRule.
 * `private` protege el detalle de implementación. `by lazy` es delegación real.
 * Los indicadores son recomendaciones; NO se impide el registro si faltan letra o número.
 */
class PasswordAdvisor(
    private val minimumLength: Int = AuthValidator.MIN_PASSWORD_LENGTH
) {
    init {
        require(minimumLength > 0) { "La longitud mínima debe ser positiva" }
    }

    private val rules: List<PasswordRule> by lazy {
        listOf(LengthRule(minimumLength), LetterRule(), NumberRule())
    }

    fun evaluate(password: String): PasswordAssessment {
        // Lambda con `it`: map es una función de orden superior.
        val checks = rules.map { rule ->
            if (rule.matches(password)) {
                RequirementCheck.Met(rule.requirement)
            } else {
                RequirementCheck.Missing(rule.requirement)
            }
        }
        val metCount = checks.count { it is RequirementCheck.Met }
        val minimumMet = password.length >= minimumLength

        // if, else if, else, rangos `in`: `if` funciona como expresión en Kotlin.
        val strength = if (password.isEmpty()) {
            PasswordStrength.EMPTY
        } else if (password.length in 1 until minimumLength) {
            PasswordStrength.SHORT
        } else if (password.length >= 10 && metCount == rules.size) {
            PasswordStrength.IMPROVED
        } else {
            PasswordStrength.BASIC
        }
        return PasswordAssessment(strength, checks, minimumMet)
    }
}
