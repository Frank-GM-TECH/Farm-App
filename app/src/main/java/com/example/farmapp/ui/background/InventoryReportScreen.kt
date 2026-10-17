package com.example.farmapp.ui.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.data.background.InventoryReportViewModel
import com.example.farmapp.data.background.ReportPhase
import com.example.farmapp.data.background.ReportWorkUiState
import com.example.farmapp.ui.theme.FarmAppTheme

@Composable
fun InventoryReportRoute(
    onBack: () -> Unit,
    viewModel: InventoryReportViewModel = viewModel(factory = InventoryReportViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryReportScreen(uiState, onBack, viewModel::start, viewModel::cancel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReportScreen(
    state: ReportWorkUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    val running = state.phase in setOf(ReportPhase.QUEUED, ReportPhase.PREPARING, ReportPhase.SAVING)
    val statusId = when(state.phase) {
        ReportPhase.IDLE -> R.string.work_state_idle
        ReportPhase.QUEUED -> R.string.work_state_queued
        ReportPhase.PREPARING -> R.string.work_state_preparing
        ReportPhase.SAVING -> R.string.work_state_saving
        ReportPhase.COMPLETED -> R.string.work_state_completed
        ReportPhase.FAILED -> R.string.work_state_failed
        ReportPhase.CANCELLED -> R.string.work_state_cancelled
    }
    Scaffold(
        modifier = Modifier.testTag("work_screen"),
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.work_title)) },
            navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) } }
        ) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text(stringResource(R.string.work_disclaimer), style = MaterialTheme.typography.bodyMedium) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.work_steps_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.work_steps_description))
                    }
                }
            }
            item {
                Text(
                    stringResource(statusId),
                    modifier = Modifier.testTag("work_status").semantics {
                        liveRegion = LiveRegionMode.Polite
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (state.phase == ReportPhase.COMPLETED && !state.fileName.isNullOrBlank()) {
                item { Text(stringResource(R.string.work_saved_file, state.fileName),
                    modifier = Modifier.testTag("work_file_name")) }
            }
            item {
                Button(
                    onClick = onStart,
                    enabled = !running,
                    modifier = Modifier.fillMaxWidth().testTag("work_start")
                ) { Text(stringResource(R.string.work_start)) }
            }
            item {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = running,
                    modifier = Modifier.fillMaxWidth().testTag("work_cancel")
                ) { Text(stringResource(R.string.work_cancel)) }
            }
            item {
                Text(
                    stringResource(R.string.work_constraints_notice),
                    modifier = Modifier.testTag("work_constraints_notice"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item { Text(stringResource(R.string.work_local_file_note), style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Preview(name = "Cotejo 28 - trabajo pendiente")
@Composable
private fun WorkQueuedPreview() = FarmAppTheme {
    InventoryReportScreen(ReportWorkUiState(ReportPhase.QUEUED), {}, {}, {})
}

@Preview(name = "Cotejo 28 - archivo guardado")
@Composable
private fun WorkCompletedPreview() = FarmAppTheme {
    InventoryReportScreen(ReportWorkUiState(ReportPhase.COMPLETED, "informe_room_demo.txt"), {}, {}, {})
}
