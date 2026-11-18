package com.example.farmapp.checkout

/**
 * HU04 - Cálculos del cupón de demostración.
 *
 * Los importes siempre están expresados
 * en céntimos de sol.
 */
object CouponPricing {

    const val DEMO_CODE = "FARMAPP10"

    fun calculateDiscount(
        couponCode: String?,
        subtotalCents: Long
    ): Long {

        if (subtotalCents <= 0L) {
            return 0L
        }

        if (
            !couponCode.equals(
                DEMO_CODE,
                ignoreCase = true
            )
        ) {
            return 0L
        }

        // 10 % del subtotal, redondeado
        // hacia abajo al céntimo.
        return subtotalCents / 10L
    }

    fun calculateTotal(
        subtotalCents: Long,
        couponCode: String?
    ): Long {

        val safeSubtotal =
            subtotalCents.coerceAtLeast(0L)

        val discount = calculateDiscount(
            couponCode = couponCode,
            subtotalCents = safeSubtotal
        )

        return safeSubtotal - discount
    }
}