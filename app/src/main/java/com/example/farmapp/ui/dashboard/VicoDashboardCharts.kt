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
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Adaptadores reutilizables para Vico 3.3.1.
 *
 * Las métricas siguen calculándose en DashboardMetrics y se reciben desde los
 * ViewModels existentes. Vico solo representa esos valores: no accede a Firebase.
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
    // Un valor inválido no debe llegar al motor gráfico.
    val data = entries.filter { it.value.isFinite() && it.value >= 0.0 }
    val producer = remember { CartesianChartModelProducer() }
    val theme = rememberM3VicoTheme()

    LaunchedEffect(producer, data, line) {
        if (data.isNotEmpty()) {
            producer.runTransaction {
                if (line) {
                    lineModel { series(data.map { it.value }) }
                } else {
                    columnModel { series(data.map { it.value }) }
                }
            }
        }
    }

    // Las categorías largas se identifican con índices en el eje X y con una
    // lista completa de etiquetas y valores debajo del gráfico.
    val indexFormatter = remember(data) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt()
            if (index in data.indices && abs(value - index) < 0.001) {
                (index + 1).toString()
            } else {
                ""
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
                    ProvideVicoTheme(theme) {
                        val chart = if (line) {
                            rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = indexFormatter
                                )
                            )
                        } else {
                            rememberCartesianChart(
                                rememberColumnCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = indexFormatter
                                )
                            )
                        }
                        CartesianChartHost(
                            chart = chart,
                            modelProducer = producer,
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
                    "Leyenda: número del eje horizontal, categoría y valor",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                data.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${item.label}",
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
