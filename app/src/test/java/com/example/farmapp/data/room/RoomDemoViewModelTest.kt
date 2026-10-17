package com.example.farmapp.data.room

import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomDemoViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private class FakeRoomRepo : RoomDemoRepository {
        private val mutable = MutableStateFlow<List<RoomDemoItem>>(emptyList())
        override val items = mutable
        override fun observeItem(id: Long) = mutable.map { all -> all.firstOrNull { it.id == id } }
        override suspend fun updateRecord(id: Long, name: String, priceCents: Long, stock: Int): Boolean {
            if (mutable.value.none { it.id == id }) return false
            mutable.value = mutable.value.map { if (it.id == id) it.copy(name = name, priceCents = priceCents, stock = stock) else it }
            return true
        }
        override suspend fun sellOne(id: Long): Boolean {
            val item = mutable.value.firstOrNull { it.id == id && it.stock > 0 } ?: return false
            mutable.value = mutable.value.map { if (it.id == item.id) it.copy(stock = it.stock-1) else it }
            return true
        }
        override suspend fun deleteRecord(id: Long): Boolean {
            val existing = mutable.value.any { it.id == id }
            mutable.value = mutable.value.filterNot { it.id == id }
            return existing
        }
        var nextId = 1L
        var failOnAdd = false
        override suspend fun add(item: RoomDemoItem) {
            if (failOnAdd) throw IllegalStateException("Error simulado")
            mutable.value = mutable.value + item.copy(id = nextId++)
        }
        override suspend fun increaseStock(id: Long) {
            mutable.value = mutable.value.map { if (it.id == id) it.copy(stock = it.stock + 1) else it }
        }
        override suspend fun remove(item: RoomDemoItem) {
            mutable.value = mutable.value.filterNot { it.id == item.id }
        }
    }

    @Test fun insertarYObservarFlow() = runTest(main.dispatcher) {
        val vm = RoomDemoViewModel(FakeRoomRepo())
        advanceUntilIdle()
        vm.save("Gasas", "6.50", "3")
        advanceUntilIdle()
        assertEquals(RoomDemoMessage.SAVED, vm.uiState.value.message)
        assertEquals("Gasas", vm.uiState.value.items.single().name)
    }

    @Test fun validacionImpideDatosIncorrectos() = runTest(main.dispatcher) {
        val vm = RoomDemoViewModel(FakeRoomRepo())
        advanceUntilIdle()
        vm.save("", "invalido", "-2")
        advanceUntilIdle()
        assertEquals(RoomDemoMessage.INVALID, vm.uiState.value.message)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test fun rutaErrorMuestraMensajeSinBloquearLaApp() = runTest(main.dispatcher) {
        val repo = FakeRoomRepo().apply { failOnAdd = true }
        val vm = RoomDemoViewModel(repo)
        advanceUntilIdle()
        vm.save("Gasas", "6.50", "3")
        advanceUntilIdle()
        assertEquals(RoomDemoMessage.ERROR, vm.uiState.value.message)
        assertTrue(!vm.uiState.value.busy)
    }

    @Test fun actualizarYEliminarSeReflejaEnFlow() = runTest(main.dispatcher) {
        val vm = RoomDemoViewModel(FakeRoomRepo())
        advanceUntilIdle()
        vm.save("Gel", "7", "1")
        advanceUntilIdle()
        val item = vm.uiState.value.items.single()
        vm.increase(item.id)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.items.single().stock)
        vm.remove(vm.uiState.value.items.single())
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
    }
}
