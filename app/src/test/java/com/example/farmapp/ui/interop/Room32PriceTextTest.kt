package com.example.farmapp.ui.interop

import org.junit.Assert.assertEquals
import org.junit.Test

class Room32PriceTextTest {
    @Test fun formateaPrecioCero() = assertEquals("0.00", room32PriceText(0))
    @Test fun formateaCentimos() = assertEquals("0.05", room32PriceText(5))
    @Test fun formateaPrecioConDosDecimales() = assertEquals("18.90", room32PriceText(1890))
    @Test(expected = IllegalArgumentException::class)
    fun rechazaPrecioNegativo() { room32PriceText(-1) }
}
