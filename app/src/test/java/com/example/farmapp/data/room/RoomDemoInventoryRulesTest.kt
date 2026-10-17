package com.example.farmapp.data.room

import org.junit.Assert.*
import org.junit.Test

class RoomDemoInventoryRulesTest {
    @Test fun stockCeroDeshabilitaVenta() {
        assertFalse(RoomDemoInventoryRules.canSell(RoomDemoItem(1,"Gasas",300,0)))
    }
    @Test fun stockPositivoHabilitaVenta() {
        assertTrue(RoomDemoInventoryRules.canSell(RoomDemoItem(1,"Gasas",300,1)))
    }
    @Test fun ausenciaDeshabilitaVenta() { assertFalse(RoomDemoInventoryRules.canSell(null)) }
    @Test fun idDebeSerPositivo() {
        assertFalse(RoomDemoInventoryRules.validId(0))
        assertTrue(RoomDemoInventoryRules.validId(42))
    }
}
