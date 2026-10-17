package com.example.farmapp.catalog

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** C19. Contrato suspendible: futuro repositorio de inventario podría usar red o Room. */
interface DemoAvailabilitySource {
    suspend fun unitsFor(category: DemoCategory): Int
}

/** Solo DEMO: simula latencia cooperativa; nunca consulta stock real. */
class LocalDemoAvailabilitySource(
    private val repository: CatalogRepository<DemoProduct>,
    private val simulatedLatencyMs: Long = 450L
) : DemoAvailabilitySource {
    override suspend fun unitsFor(category: DemoCategory): Int {
        delay(simulatedLatencyMs) // Suspende; NO bloquea el hilo como Thread.sleep().
        return repository.all().filter { it.category == category }.sumOf { it.stock }
    }
}

data class DemoAvailabilitySummary(
    val unitsByCategory: Map<DemoCategory, Int>,
    val totalUnits: Int
)

/**
 * C19. Concurrencia estructurada: async de cada categoría es HIJO del scope.
 * awaitAll propaga errores; coroutineScope cancela las otras consultas si falla una.
 * Dispatchers.Default se inyecta para permitir pruebas deterministas.
 */
class CatalogAvailabilityLoader(
    private val source: DemoAvailabilitySource,
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun load(): DemoAvailabilitySummary = withContext(computationDispatcher) {
        coroutineScope {
            val requests = DemoCategory.entries.map { category ->
                async { category to source.unitsFor(category) }
            }
            val grouped = requests.awaitAll().toMap()
            DemoAvailabilitySummary(grouped, grouped.values.sum())
        }
    }
}
