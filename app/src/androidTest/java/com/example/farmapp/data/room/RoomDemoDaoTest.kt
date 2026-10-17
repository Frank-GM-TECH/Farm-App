package com.example.farmapp.data.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDemoDaoTest {
    private lateinit var db: RoomDemoDatabase
    private lateinit var dao: RoomDemoDao

    @Before fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, RoomDemoDatabase::class.java).build()
        dao = db.roomDemoDao()
    }
    @After fun tearDown() { db.close() }

    @Test fun insertaYConsulta() = runBlocking {
        val id = dao.insert(RoomDemoItem(name="Gasas", priceCents=650, stock=3))
        assertEquals("Gasas", dao.findById(id)?.name)
        assertEquals(1, dao.observeAll().first().size)
    }
    @Test fun actualizaCantidad() = runBlocking {
        val id = dao.insert(RoomDemoItem(name="Gel", priceCents=800, stock=2))
        val original = requireNotNull(dao.findById(id))
        assertEquals(1, dao.update(original.copy(stock=6)))
        assertEquals(6, dao.findById(id)?.stock)
    }
    @Test fun eliminaRegistro() = runBlocking {
        val id = dao.insert(RoomDemoItem(name="Vendas", priceCents=900, stock=1))
        assertEquals(1, dao.delete(requireNotNull(dao.findById(id))))
        assertNull(dao.findById(id))
        assertTrue(dao.observeAll().first().isEmpty())
    }
    @Test fun ordenaPorNombre() = runBlocking {
        dao.insert(RoomDemoItem(name="Venda", priceCents=300, stock=1))
        dao.insert(RoomDemoItem(name="Alcohol", priceCents=500, stock=2))
        assertEquals(listOf("Alcohol", "Venda"), dao.observeAll().first().map { it.name })
    }
    @Test fun iniciaVaciaSinConfundirConSqlLab() = runBlocking {
        assertTrue(dao.observeAll().first().isEmpty())
    }
}
