package com.example.farmapp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.dashboard.CatalogDashboardState
import com.example.farmapp.dashboard.CatalogDashboardViewModel
import com.example.farmapp.dashboard.DashboardMetrics
import com.example.farmapp.dashboard.OrdersDashboardState
import com.example.farmapp.dashboard.OrdersDashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val spanish = Locale.forLanguageTag("es-PE")

private fun soles(cents: Long): String = String.format(spanish, "S/ %.2f", cents / 100.0)

private fun lastSyncLabel(time: Long): String {
    val formatted = Instant.ofEpochMilli(time)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", spanish))
    return "Última sincronización verificada con Firestore: $formatted"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        },
        content = content
    )
}

/** Acceso único para mostrar claramente los tres dashboards en la exposición. */
@Composable
fun DashboardsOverviewScreen(
    onBack: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCatalog: () -> Unit
) {
    DashboardScaffold("Paneles de análisis", onBack, Modifier.testTag("dashboards_overview")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Tres dashboards con datos de FarmApp",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item { Text("Los paneles utilizan el catálogo sincronizado y los pedidos de la cuenta actual. No incluyen cifras inventadas.") }
            item {
                DashboardEntry(
                    title = "1. Inventario",
                    description = "Stock disponible, agotados, bajo stock y productos por categoría.",
                    onClick = onOpenInventory
                )
            }
            item {
                DashboardEntry(
                    title = "2. Mis pedidos",
                    description = "Compras registradas en Firestore, importes y evolución mensual.",
                    onClick = onOpenOrders
                )
            }
            item {
                DashboardEntry(
                    title = "3. Análisis del catálogo",
                    description = "Distribución de precios, promedio por categoría y disponibilidad.",
                    onClick = onOpenCatalog
                )
            }
        }
    }
}

@Composable
private fun DashboardEntry(title: String, description: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir dashboard")
            }
        }
    }
}

@Composable
fun InventoryDashboardRoute(
    onBack: () -> Unit,
    viewModel: CatalogDashboardViewModel = viewModel(factory = CatalogDashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryDashboardScreen(state, onBack)
}

@Composable
fun InventoryDashboardScreen(state: CatalogDashboardState, onBack: () -> Unit) {
    DashboardScaffold("Dashboard de inventario", onBack, Modifier.testTag("inventory_dashboard")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SourceNote("Fuente: productos activos verificados en Firestore y persistidos en Room. Las métricas no utilizan semillas locales sin verificar.") }
            state.synchronizedAtMillis?.let { time ->
                item { SourceNote(lastSyncLabel(time)) }
            }
            when {
                state.isLoading -> item { CircularProgressIndicator() }
                state.errorMessage != null -> item { ErrorNote(state.errorMessage ?: "Error al cargar datos") }
                else -> {
                    val metrics = DashboardMetrics.inventory(state.products)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardMetricCard("Productos", metrics.totalProducts.toString(), Modifier.weight(1f))
                            DashboardMetricCard("Unidades", metrics.totalUnits.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardMetricCard("Agotados", metrics.outOfStock.toString(), Modifier.weight(1f))
                            DashboardMetricCard("Bajo stock (1-5)", metrics.lowStock.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        VicoDashboardColumnChart(
                            title = "Cantidad de productos por categoría",
                            bars = metrics.categories.map {
                                DashboardBar(it.name, it.productCount.toDouble(), it.productCount.toString())
                            }
                        )
                    }
                    item {
                        VicoDashboardColumnChart(
                            title = "Unidades disponibles por categoría",
                            bars = metrics.categories.map {
                                DashboardBar(it.name, it.availableUnits.toDouble(), it.availableUnits.toString())
                            }
                        )
                    }
                    if (metrics.totalProducts == 0) {
                        item { SourceNote("Todavía no se encuentran productos activos en la caché. Comprueba la sincronización con Firestore.") }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogDashboardRoute(
    onBack: () -> Unit,
    viewModel: CatalogDashboardViewModel = viewModel(factory = CatalogDashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogDashboardScreen(state, onBack)
}

@Composable
fun CatalogDashboardScreen(state: CatalogDashboardState, onBack: () -> Unit) {
    DashboardScaffold("Dashboard del catálogo", onBack, Modifier.testTag("catalog_dashboard")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SourceNote("Fuente: instantánea verificada de Firestore, también guardada en Room. Precios en soles; no se consultan pedidos de otros clientes.") }
            state.synchronizedAtMillis?.let { time ->
                item { SourceNote(lastSyncLabel(time)) }
            }
            when {
                state.isLoading -> item { CircularProgressIndicator() }
                state.errorMessage != null -> item { ErrorNote(state.errorMessage ?: "No se pudo cargar el dashboard.") }
                else -> {
                    val metrics = DashboardMetrics.catalog(state.products)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardMetricCard("Productos", metrics.totalProducts.toString(), Modifier.weight(1f))
                            DashboardMetricCard("Con stock", metrics.availableProducts.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardMetricCard("Precio promedio", soles(metrics.averagePriceCents), Modifier.weight(1f))
                            DashboardMetricCard("Categorías", metrics.categories.size.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        VicoDashboardColumnChart(
                            title = "Distribución de productos por precio",
                            bars = metrics.priceRanges.map { (name, count) ->
                                DashboardBar(name, count.toDouble(), count.toString())
                            }
                        )
                    }
                    item {
                        VicoDashboardColumnChart(
                            title = "Precio promedio por categoría",
                            bars = metrics.categories.map {
                                DashboardBar(
                                    it.name,
                                    it.averagePriceCents.toDouble(),
                                    soles(it.averagePriceCents)
                                )
                            }
                        )
                    }
                    if (metrics.totalProducts == 0) {
                        item { SourceNote("Aún no existen productos activos en el catálogo local para analizar.") }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersDashboardRoute(
    onBack: () -> Unit,
    viewModel: OrdersDashboardViewModel = viewModel(factory = OrdersDashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrdersDashboardScreen(state, onBack, viewModel::refresh)
}

@Composable
fun OrdersDashboardScreen(
    state: OrdersDashboardState,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    DashboardScaffold("Dashboard de mis pedidos", onBack, Modifier.testTag("orders_dashboard")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SourceNote("Fuente: Firestore (consulta al servidor). Solo pedidos del usuario autenticado. Los pagos de FarmApp son simulados.") }
            item {
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().testTag("orders_dashboard_refresh")
                ) { Text("Actualizar mis pedidos") }
            }
            when {
                state.isLoading -> item { CircularProgressIndicator() }
                state.errorMessage != null -> item { ErrorNote(state.errorMessage ?: "No se pudo cargar el dashboard.") }
                else -> {
                    val metrics = DashboardMetrics.orders(state.orders)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardMetricCard("Pedidos válidos", metrics.count.toString(), Modifier.weight(1f))
                            DashboardMetricCard("Importe acumulado", soles(metrics.totalCents), Modifier.weight(1f))
                        }
                    }
                    item {
                        VicoDashboardLineChart(
                            title = "Importe de pedidos por mes (últimos 6 meses)",
                            points = metrics.monthly.map { month ->
                                DashboardBar(
                                    month.month.format(DateTimeFormatter.ofPattern("MMM yy", spanish)),
                                    month.amountCents.toDouble(),
                                    soles(month.amountCents)
                                )
                            }
                        )
                    }
                    item {
                        VicoDashboardColumnChart(
                            title = "Cantidad de pedidos por mes",
                            bars = metrics.monthly.map { month ->
                                DashboardBar(
                                    month.month.format(DateTimeFormatter.ofPattern("MMM yy", spanish)),
                                    month.count.toDouble(),
                                    month.count.toString()
                                )
                            }
                        )
                    }
                    val latest = metrics.latest
                    if (latest != null) {
                        item {
                            val dateText = if (latest.createdAtMillis > 0L) {
                                Instant.ofEpochMilli(latest.createdAtMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", spanish))
                            } else "Fecha no registrada"
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Último pedido", style = MaterialTheme.typography.titleMedium)
                                    Text("Fecha: $dateText")
                                    Text("Importe: ${soles(latest.totalCents)}")
                                    Text("Estado: ${latest.status}")
                                }
                            }
                        }
                    } else {
                        item { SourceNote("No tienes pedidos válidos registrados en Firestore para mostrar.") }
                    }
                    if (state.orders.size != metrics.count) {
                        item { SourceNote("Se excluyeron pedidos con datos incompletos o inconsistentes de las estadísticas.") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceNote(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ErrorNote(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}
