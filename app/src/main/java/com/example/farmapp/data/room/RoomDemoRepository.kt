package com.example.farmapp.data.room

import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia, intercambiable por repositorio falso en pruebas. */
interface RoomDemoRepository {
    val items: Flow<List<RoomDemoItem>>
    fun observeItem(id: Long): Flow<RoomDemoItem?>
    suspend fun add(item: RoomDemoItem)
    suspend fun increaseStock(id: Long)
    suspend fun remove(item: RoomDemoItem)
    suspend fun updateRecord(id: Long, name: String, priceCents: Long, stock: Int): Boolean
    suspend fun sellOne(id: Long): Boolean
    suspend fun deleteRecord(id: Long): Boolean
}

class OfflineRoomDemoRepository(private val dao: RoomDemoDao) : RoomDemoRepository {
    override val items: Flow<List<RoomDemoItem>> = dao.observeAll()
    override fun observeItem(id: Long): Flow<RoomDemoItem?> = dao.observeById(id)

    override suspend fun add(item: RoomDemoItem) { dao.insert(item) }
    override suspend fun increaseStock(id: Long) {
        val current = dao.findById(id) ?: return
        if (current.stock < 99_999) dao.update(current.copy(stock = current.stock + 1))
    }
    override suspend fun remove(item: RoomDemoItem) { dao.delete(item) }

    override suspend fun updateRecord(id: Long, name: String, priceCents: Long, stock: Int): Boolean {
        val original = dao.findById(id) ?: return false
        return dao.update(original.copy(name = name, priceCents = priceCents, stock = stock)) == 1
    }

    override suspend fun sellOne(id: Long): Boolean = dao.sellOneIfAvailable(id) == 1

    override suspend fun deleteRecord(id: Long): Boolean = dao.deleteById(id) == 1
}
