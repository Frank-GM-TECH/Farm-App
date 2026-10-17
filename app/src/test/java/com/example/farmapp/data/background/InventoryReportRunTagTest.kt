package com.example.farmapp.data.background

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InventoryReportRunTagTest {
    @Test fun limite_sinTrabajosNoHayEjecucion() {
        assertNull(mostRecentReportRunTag(emptyList()))
    }

    @Test fun exito_eligeLaEjecucionMasRecienteSinDependerDelOrden() {
        val oldTag = ReportWorkKeys.RUN_TAG_PREFIX + "0000000000010_old"
        val newTag = ReportWorkKeys.RUN_TAG_PREFIX + "0000000000020_new"
        val tags = listOf(setOf(newTag, ReportWorkKeys.TAG_SAVE),
            setOf(oldTag, ReportWorkKeys.TAG_PREPARE),
            setOf(newTag, ReportWorkKeys.TAG_PREPARE))
        assertEquals(newTag, mostRecentReportRunTag(tags.reversed()))
    }

    @Test fun limite_ignoraEtiquetasDeOtrosTrabajos() {
        assertNull(mostRecentReportRunTag(listOf(setOf("otro_trabajo"), setOf(ReportWorkKeys.TAG_REPORT))))
    }
}
