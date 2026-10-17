package com.example.farmapp.data.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** SQLite Room real in-memory: no afecta la base persistente ni otros cotejos. */
@RunWith(AndroidJUnit4::class)
class RoomDemoInventoryDaoTest {
    private lateinit var db: RoomDemoDatabase
    private lateinit var dao: RoomDemoDao
    @Before fun setup() {
        db=Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, RoomDemoDatabase::class.java
        ).build()
        dao=db.roomDemoDao()
    }
    @After fun close() { db.close() }

    @Test fun observarItemEnDetalle() = runBlocking {
        val id=dao.insert(RoomDemoItem(name="Gel",priceCents=350,stock=2))
        assertEquals("Gel",dao.observeById(id).first()?.name)
    }
    @Test fun ventaDescuentaUnaUnidad() = runBlocking {
        val id=dao.insert(RoomDemoItem(name="Gel",priceCents=350,stock=2))
        assertEquals(1,dao.sellOneIfAvailable(id))
        assertEquals(1,dao.findById(id)?.stock)
    }
    @Test fun dosVentasConStockUnoNoCreanNegativos() = runBlocking {
        val id=dao.insert(RoomDemoItem(name="Gel",priceCents=350,stock=1))
        assertEquals(1,dao.sellOneIfAvailable(id))
        assertEquals(0,dao.sellOneIfAvailable(id))
        assertEquals(0,dao.findById(id)?.stock)
    }
    @Test fun editarYObservar() = runBlocking {
        val id=dao.insert(RoomDemoItem(name="Gel",priceCents=350,stock=1))
        val updated=requireNotNull(dao.findById(id)).copy(name="Crema",priceCents=450,stock=4)
        assertEquals(1,dao.update(updated))
        assertEquals("Crema",dao.observeById(id).first()?.name)
    }
    @Test fun borrarIdEliminaFila() = runBlocking {
        val id=dao.insert(RoomDemoItem(name="Gel",priceCents=350,stock=1))
        assertEquals(1,dao.deleteById(id))
        assertNull(dao.findById(id))
    }
}
