package com.example.farmapp.data.background

import com.example.farmapp.data.room.RoomDemoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryReportSummaryTest {
    private val items = listOf(
        RoomDemoItem(1L, "Gel demo", 500L, 2),
        RoomDemoItem(2L, "Gasa demo", 250L, 0),
        RoomDemoItem(3L, "Termómetro demo", 900L, 3)
    )
    @Test fun exito_calculaArticulosYUnidades() {
        val summary = InventoryReportSummary.from(items)
        assertEquals(3, summary.productCount)
        assertEquals(5L, summary.totalUnits)
    }
    @Test fun exito_sumaValorEnCentimos() {
        assertEquals(3700L, InventoryReportSummary.from(items).totalValueCents)
    }
    @Test fun limite_registroAgotadoSeCuenta() {
        assertEquals(1, InventoryReportSummary.from(items).outOfStock)
    }
    @Test fun limite_inventarioVacioTieneTotalesCero() {
        assertEquals(InventoryReportSummary(0,0,0,0), InventoryReportSummary.from(emptyList()))
    }
    @Test fun exito_reporteTieneFechaYAdvertencia() {
        val report = reportText(InventoryReportSummary.from(items), "2026-10-16 18:00:00")
        assertTrue(report.contains("2026-10-16"))
        assertTrue(report.contains("Artículos: 3"))
        assertTrue(report.contains("ficticios"))
    }
}
