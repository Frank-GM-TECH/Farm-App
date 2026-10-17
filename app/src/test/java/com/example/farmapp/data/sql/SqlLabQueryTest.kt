package com.example.farmapp.data.sql

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas JVM del esquema de consultas; sin Android ni SQLite. */
class SqlLabQueryTest {
    @Test fun hayCincoConsultasSeleccionables() {
        assertEquals(5, SqlLabQuery.entries.size)
    }

    @Test fun parametrosProtegenLosValoresDeBusqueda() {
        assertTrue(SqlLabQuery.LOW_STOCK.statement.contains("BETWEEN ? AND ?"))
        assertEquals(listOf("1", "5"), SqlLabQuery.LOW_STOCK.arguments.toList())
        assertTrue(SqlLabQuery.NAME_LIKE.statement.contains("LIKE ?"))
        assertEquals(listOf("%gel%"), SqlLabQuery.NAME_LIKE.arguments.toList())
    }

    @Test fun incluyeOperadoresDelCurso() {
        val statements = SqlLabQuery.entries.joinToString(" ") { it.statement }
        listOf("SELECT", "WHERE", "LIKE", "DISTINCT", "GROUP BY", "ORDER BY", "LIMIT").forEach {
            assertTrue("Falta $it", statements.contains(it))
        }
    }

    @Test fun resultadosCuentanConModelosInmutables() {
        val product = SqlLabProduct("DEMO", "Producto", "Higiene", 4)
        assertEquals(4, product.stock)
        assertEquals("DEMO", product.copy(stock = 7).sku)
    }
}
