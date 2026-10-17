package com.example.farmapp.data.sql

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas SQL REALES: requieren emulador. BD aislada, sin modificar datos de la app. */
@RunWith(AndroidJUnit4::class)
class SqlLabRepositoryInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: SqlLabDatabase
    private lateinit var repository: SqliteSqlLabRepository
    private val name = "farmapp_sql_lab_instrumented.db"

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(name)
        db = SqlLabDatabase(context, name)
        repository = SqliteSqlLabRepository(db)
    }

    @After fun tearDown() { db.close(); context.deleteDatabase(name) }

    @Test fun selectOrderByLimit_regresaSeisFilasY34Unidades() = runBlocking {
        val rows = (repository.runQuery(SqlLabQuery.ALL) as SqlLabResult.Products).items
        assertEquals(6, rows.size)
        assertEquals(34, rows.sumOf { it.stock })
        assertTrue(rows.zipWithNext().all { (a, b) -> a.name.compareTo(b.name, ignoreCase = true) <= 0 })
    }

    @Test fun whereSoloProductosConStockDeUnoACinco() = runBlocking {
        val rows = (repository.runQuery(SqlLabQuery.LOW_STOCK) as SqlLabResult.Products).items
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.stock in 1..5 })
    }

    @Test fun likeFiltraNombreSinConcatenarDatos() = runBlocking {
        val rows = (repository.runQuery(SqlLabQuery.NAME_LIKE) as SqlLabResult.Products).items
        assertEquals(listOf("FA-001"), rows.map { it.sku })
    }

    @Test fun distinctDevuelveSoloCategoriasUnicas() = runBlocking {
        val rows = (repository.runQuery(SqlLabQuery.DISTINCT_CATEGORIES) as SqlLabResult.Categories).items
        assertEquals(3, rows.size)
        assertEquals(rows.size, rows.distinct().size)
    }

    @Test fun groupBySumaYCuentaCorrectamente() = runBlocking {
        val rows = (repository.runQuery(SqlLabQuery.GROUP_BY_CATEGORY) as SqlLabResult.Groups).items
        assertEquals(3, rows.size)
        assertEquals(6, rows.sumOf { it.products })
        assertEquals(34, rows.sumOf { it.totalUnits })
    }

    @Test fun insertUpdateDeleteYRestriccionUnica() = runBlocking {
        assertTrue(repository.insertExample())
        assertFalse(repository.insertExample())
        assertEquals(7, (repository.runQuery(SqlLabQuery.ALL) as SqlLabResult.Products).items.size)
        assertEquals(1, repository.updateExample())
        val extra = (repository.runQuery(SqlLabQuery.ALL) as SqlLabResult.Products).items.single { it.sku == "FA-SQL-EXTRA" }
        assertEquals(11, extra.stock)
        assertEquals(1, repository.deleteExample())
        assertEquals(0, repository.deleteExample())
        assertEquals(6, (repository.runQuery(SqlLabQuery.ALL) as SqlLabResult.Products).items.size)
    }

    @Test fun restauracionEsTransaccional() = runBlocking {
        repository.insertExample()
        repository.resetExamples()
        val result = (repository.runQuery(SqlLabQuery.ALL) as SqlLabResult.Products).items
        assertEquals(6, result.size)
        assertEquals(34, result.sumOf { it.stock })
    }

    @Test fun basePersisteDespuesDeCerrarYVolverAAbrir() = runBlocking {
        repository.insertExample()
        db.close()
        val reopened = SqlLabDatabase(context, name)
        try {
            val persisted = SqliteSqlLabRepository(reopened).runQuery(SqlLabQuery.ALL) as SqlLabResult.Products
            assertTrue(persisted.items.any { it.sku == "FA-SQL-EXTRA" })
        } finally { reopened.close() }
    }
}
