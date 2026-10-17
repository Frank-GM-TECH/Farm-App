package com.example.farmapp.data.room

import androidx.lifecycle.SavedStateHandle
import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomDemoDetailViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class FakeRepo(initial: List<RoomDemoItem>) : RoomDemoRepository {
        private val records = MutableStateFlow(initial)
        override val items = records
        override fun observeItem(id: Long) = records.map { all -> all.firstOrNull { it.id == id } }
        override suspend fun add(item: RoomDemoItem) { records.value += item }
        override suspend fun increaseStock(id: Long) { records.value = records.value.map { if(it.id==id)it.copy(stock=it.stock+1)else it } }
        override suspend fun remove(item: RoomDemoItem) { deleteRecord(item.id) }
        override suspend fun updateRecord(id: Long, name: String, priceCents: Long, stock: Int): Boolean {
            if (records.value.none { it.id == id }) return false
            records.value = records.value.map { if(it.id==id)it.copy(name=name,priceCents=priceCents,stock=stock)else it }
            return true
        }
        override suspend fun sellOne(id: Long): Boolean {
            if (records.value.none { it.id == id && it.stock > 0 }) return false
            records.value = records.value.map { if(it.id==id)it.copy(stock=it.stock-1)else it }
            return true
        }
        override suspend fun deleteRecord(id: Long): Boolean {
            val exists = records.value.any { it.id == id }
            records.value = records.value.filterNot { it.id == id }
            return exists
        }
    }
    private fun vm(repo: RoomDemoRepository,id:Long=1) =
        RoomDemoDetailViewModel(SavedStateHandle(mapOf("itemId" to id)),repo)

    @Test fun detalleObservaRoomDesdeFlow() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,2))))
        advanceUntilIdle()
        assertEquals("Gasas",vm.state.value.item?.name)
    }
    @Test fun ventaDescuentaUnidadYRecompone() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,2))))
        advanceUntilIdle(); vm.sellOne(); advanceUntilIdle()
        assertEquals(1,vm.state.value.item?.stock)
        assertEquals(RoomDemoMessage.SOLD,vm.state.value.feedback)
    }
    @Test fun sinStockNoVendeNiDejaStockNegativo() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,0))))
        advanceUntilIdle(); vm.sellOne(); advanceUntilIdle()
        assertEquals(0,vm.state.value.item?.stock)
        assertEquals(RoomDemoMessage.NO_STOCK,vm.state.value.feedback)
    }
    @Test fun editarNombrePrecioYStock() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,2))))
        advanceUntilIdle(); vm.update("Gasas premium","12.25","7"); advanceUntilIdle()
        assertEquals("Gasas premium",vm.state.value.item?.name)
        assertEquals(1225L,vm.state.value.item?.priceCents)
        assertEquals(7,vm.state.value.item?.stock)
    }
    @Test fun invalidosNoModificanFila() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,2))))
        advanceUntilIdle(); vm.update("","x","-1"); advanceUntilIdle()
        assertEquals(RoomDemoMessage.INVALID,vm.state.value.feedback)
        assertEquals(2,vm.state.value.item?.stock)
    }
    @Test fun eliminarActualizaEstadoVacio() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(listOf(RoomDemoItem(1,"Gasas",550,2))))
        advanceUntilIdle(); vm.delete(); advanceUntilIdle()
        assertNull(vm.state.value.item)
        assertEquals(RoomDemoMessage.DELETED,vm.state.value.feedback)
    }
    @Test fun idAusenteMuestraNotFound() = runTest(main.dispatcher) {
        val vm=vm(FakeRepo(emptyList()),33)
        advanceUntilIdle(); vm.sellOne(); advanceUntilIdle()
        assertEquals(RoomDemoMessage.NO_STOCK,vm.state.value.feedback)
        assertTrue(vm.state.value.loaded)
    }
}
