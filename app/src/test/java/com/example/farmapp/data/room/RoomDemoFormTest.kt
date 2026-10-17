package com.example.farmapp.data.room

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomDemoFormTest {
    @Test fun valido_conPrecioDecimal() {
        assertEquals(RoomDemoValidInput("Gasas", 650, 3), RoomDemoForm.validate(" Gasas ", "6.50", "3"))
    }
    @Test fun valido_conComaDecimal() {
        assertEquals(820L, RoomDemoForm.validate("Termómetro", "8,2", "0")?.priceCents)
    }
    @Test fun precioSinDecimales() {
        assertEquals(1200L, RoomDemoForm.validate("Guantes", "12", "2")?.priceCents)
    }
    @Test fun sinNombreNoSeGuarda() { assertNull(RoomDemoForm.validate(" ", "8.50", "3")) }
    @Test fun tresDecimalesSeRechazan() { assertNull(RoomDemoForm.validate("Gasas", "8.123", "3")) }
    @Test fun stockNegativoSeRechaza() { assertNull(RoomDemoForm.validate("Gasas", "8.50", "-1")) }
    @Test fun stockExcesivoSeRechaza() { assertNull(RoomDemoForm.validate("Gasas", "8.50", "100000")) }
}
