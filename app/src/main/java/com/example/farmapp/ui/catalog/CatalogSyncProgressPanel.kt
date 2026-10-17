package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.farmapp.catalog.CatalogSyncUiState
import com.example.farmapp.catalog.SyncPhase
import com.example.farmapp.ui.theme.FarmAppTheme

/** C20: interfaz sin ViewModel, alimentada por StateFlow + callbacks (state hoisting). */
@Composable
fun CatalogSyncProgressPanel(
    uiState: CatalogSyncUiState,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().testTag("sync_demo_panel")) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.sync_demo_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.sync_demo_disclaimer), style = MaterialTheme.typography.bodySmall)

            SyncProgressLine(
                title = stringResource(R.string.sync_demo_catalog),
                value = uiState.catalogProgress,
                tag = "sync_catalog_progress"
            )
            SyncProgressLine(
                title = stringResource(R.string.sync_demo_stock),
                value = uiState.stockProgress,
                tag = "sync_stock_progress"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.phase == SyncPhase.RUNNING) {
                    Button(
                        onClick = onPause,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("sync_pause")
                    ) { Text(stringResource(R.string.sync_demo_pause)) }
                } else {
                    Button(
                        onClick = onStartOrResume,
                        enabled = uiState.phase != SyncPhase.FINISHED,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("sync_start")
                    ) {
                        Text(stringResource(
                            if (uiState.phase == SyncPhase.PAUSED) R.string.sync_demo_resume
                            else R.string.sync_demo_start
                        ))
                    }
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("sync_reset")
                ) { Text(stringResource(R.string.sync_demo_reset)) }
            }

            val statusText = stringResource(when (uiState.phase) {
                SyncPhase.IDLE -> R.string.sync_demo_idle
                SyncPhase.RUNNING -> R.string.sync_demo_running
                SyncPhase.PAUSED -> R.string.sync_demo_paused
                SyncPhase.FINISHED -> R.string.sync_demo_finished
                SyncPhase.ERROR -> R.string.sync_demo_error
            })
            Text(
                text = statusText,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    .testTag("sync_status"),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SyncProgressLine(title: String, value: Int, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$title: $value%", style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(
            progress = { value.coerceIn(0,100) / 100f },
            modifier = Modifier.fillMaxWidth().testTag(tag)
        )
    }
}

@Preview(name = "C20 - pausado", showBackground = true)
@Composable
private fun CatalogSyncPausedPreview() = FarmAppTheme {
    CatalogSyncProgressPanel(
        uiState = CatalogSyncUiState(50, 90, SyncPhase.PAUSED),
        onStartOrResume = {}, onPause = {}, onReset = {}
    )
}

@Preview(name = "C20 - terminado", showBackground = true)
@Composable
private fun CatalogSyncFinishedPreview() = FarmAppTheme {
    CatalogSyncProgressPanel(
        uiState = CatalogSyncUiState(100, 100, SyncPhase.FINISHED),
        onStartOrResume = {}, onPause = {}, onReset = {}
    )
}
