package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.catalog.AvailabilityPhase
import com.example.farmapp.catalog.AvailabilityUiState
import com.example.farmapp.catalog.DemoAvailabilitySummary
import com.example.farmapp.catalog.DemoCategory
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * C19: componente sin estado de negocio, útil en pantalla compacta y dos paneles.
 * Muestra el ciclo IDLE / LOADING / SUCCESS / CANCELLED / ERROR.
 */
@Composable
fun CatalogAvailabilityPanel(
    uiState: AvailabilityUiState,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().testTag("async_demo_panel")) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.async_demo_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.async_demo_disclaimer),
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRefresh,
                    enabled = uiState.phase != AvailabilityPhase.LOADING,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("async_refresh")
                ) { Text(stringResource(R.string.async_demo_refresh)) }
                if (uiState.phase == AvailabilityPhase.LOADING) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("async_cancel")
                    ) { Text(stringResource(R.string.async_demo_cancel)) }
                }
            }
            val message = when (uiState.phase) {
                AvailabilityPhase.IDLE -> stringResource(R.string.async_demo_idle)
                AvailabilityPhase.LOADING -> stringResource(R.string.async_demo_loading)
                AvailabilityPhase.SUCCESS -> stringResource(
                    R.string.async_demo_result, uiState.summary?.totalUnits ?: 0
                )
                AvailabilityPhase.CANCELLED -> stringResource(R.string.async_demo_cancelled)
                AvailabilityPhase.ERROR -> stringResource(R.string.async_demo_error)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            ) {
                if (uiState.phase == AvailabilityPhase.LOADING) {
                    CircularProgressIndicator(modifier = Modifier.testTag("async_loading_indicator"))
                }
                Text(message, modifier = Modifier.testTag("async_demo_status"))
            }
        }
    }
}

@Preview(name = "Cotejo 19 - resultado", showBackground = true)
@Composable
private fun AvailabilityPanelPreview() = FarmAppTheme {
    CatalogAvailabilityPanel(
        uiState = AvailabilityUiState(
            AvailabilityPhase.SUCCESS,
            DemoAvailabilitySummary(mapOf(DemoCategory.HIGIENE to 19), 19)
        ),
        onRefresh = {}, onCancel = {}
    )
}
