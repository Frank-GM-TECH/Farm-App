package com.example.farmapp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Adaptadores para Vico 2.3.6, versión compatible con Kotlin 2.2.21 y SDK 36.
 *
 * Los valores se calculan en DashboardMetrics; este archivo solo los dibuja.
 * Vico 2 usa core.cartesian.data.columnSeries/lineSeries, no columnModel/lineModel
 * de Vico 3. El eje horizontal comienza en índice 0.
 */
@Composable
fun VicoDashboardColumnChart(
    title: String,
    bars: List<DashboardBar>,
    modifier: Modifier = Modifier
) {
    VicoDashboardChart(title, bars, line = false, modifier = modifier)
}

@Composable
fun VicoDashboardLineChart(
    title: String,
    points: List<DashboardBar>,
    modifier: Modifier = Modifier
) {
    VicoDashboardChart(title, points, line = true, modifier = modifier)
}

@Composable
private fun VicoDashboardChart(
    title: String,
    entries: List<DashboardBar>,
    line: Boolean,
    modifier: Modifier = Modifier
) {
    val data = entries.filter { it.value.isFinite() && it.value >= 0.0 }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(modelProducer, data, line) {
        if (data.isNotEmpty()) {
            modelProducer.runTransaction {
                if (line) {
                    lineSeries { series(data.map { it.value }) }
                } else {
                    columnSeries { series(data.map { it.value }) }
                }
            }
        }
    }

    Card(modifier = modifier.fillMaxWidth().testTag("vico_dashboard_chart")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            if (data.isEmpty()) {
                Text(
                    "Todavía no hay datos para este gráfico.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (data.any { it.value > 0.0 }) {
                    ProvideVicoTheme(rememberM3VicoTheme()) {
                        val chart = if (line) {
                            rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom()
                            )
                        } else {
                            rememberCartesianChart(
                                rememberColumnCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom()
                            )
                        }
                        CartesianChartHost(
                            chart = chart,
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth().height(230.dp)
                        )
                    }
                } else {
                    Text(
                        "Todos los valores del período son cero.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    "Leyenda: índice del eje horizontal, categoría y valor",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                data.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index}. ${item.label}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = item.valueLabel,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
