package com.example.farmapp.checkout

import org.junit.Assert.assertEquals
import org.junit.Test

class CouponPricingTest {

    @Test
    fun discountIsTenPercent() {
        assertEquals(
            500L,
            CouponPricing.calculateDiscount(
                "FARMAPP10",
                5000L
            )
        )
    }

    @Test
    fun discountRecalculates() {
        assertEquals(
            300L,
            CouponPricing.calculateDiscount(
                "FARMAPP10",
                3000L
            )
        )
    }

    @Test
    fun invalidCouponGivesNoDiscount() {
        assertEquals(
            0L,
            CouponPricing.calculateDiscount(
                "INVALIDO",
                5000L
            )
        )
    }

    @Test
    fun zeroSubtotalGivesNoDiscount() {
        assertEquals(
            0L,
            CouponPricing.calculateDiscount(
                "FARMAPP10",
                0L
            )
        )
    }

    @Test
    fun totalUsesUpdatedDiscount() {
        assertEquals(
            2700L,
            CouponPricing.calculateTotal(
                3000L,
                "FARMAPP10"
            )
        )
    }

    @Test
    fun negativeSubtotalNeverProducesNegativeTotal() {
        assertEquals(
            0L,
            CouponPricing.calculateTotal(
                -100L,
                "FARMAPP10"
            )
        )
    }
}