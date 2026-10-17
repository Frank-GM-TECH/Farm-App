package com.example.farmapp.catalog

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** C20: como Race Tracker, pero con tareas de catálogo e inventario solo DEMO. */
interface SyncProgressTask {
    suspend fun run(fromProgress: Int, onProgress: (Int) -> Unit)
}

class SteppedSyncProgressTask(
    private val step: Int = 10,
    private val progressDelayMillis: Long
) : SyncProgressTask {
    init {
        require(step in 1..100) { "step debe estar entre 1 y 100" }
        require(progressDelayMillis > 0) { "El retardo debe ser positivo" }
    }

    override suspend fun run(fromProgress: Int, onProgress: (Int) -> Unit) {
        var current = fromProgress.coerceIn(0, 100)
        while (current < 100) {
            delay(progressDelayMillis)  // suspensión cooperativa, no Thread.sleep
            current = (current + step).coerceAtMost(100)
            onProgress(current)
        }
    }
}

/** Ambos launch son hijos: se espera por los dos, cancelación y errores se propagan. */
class DualCatalogSyncRunner(
    private val catalogTask: SyncProgressTask = SteppedSyncProgressTask(10, 200L),
    private val stockTask: SyncProgressTask = SteppedSyncProgressTask(10, 100L)
) {
    suspend fun run(
        catalogFrom: Int,
        stockFrom: Int,
        onCatalogProgress: (Int) -> Unit,
        onStockProgress: (Int) -> Unit
    ) = coroutineScope {
        launch { catalogTask.run(catalogFrom, onCatalogProgress) }
        launch { stockTask.run(stockFrom, onStockProgress) }
    }
}
