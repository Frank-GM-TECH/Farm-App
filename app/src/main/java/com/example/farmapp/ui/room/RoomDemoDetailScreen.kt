package com.example.farmapp.ui.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoDetailState
import com.example.farmapp.data.room.RoomDemoDetailViewModel
import com.example.farmapp.data.room.RoomDemoInventoryRules
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.data.room.RoomDemoMessage
import com.example.farmapp.ui.interop.LegacyRoomEditDialog
import com.example.farmapp.ui.theme.FarmAppTheme

@Composable
fun RoomDemoDetailRoute(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: RoomDemoDetailViewModel = viewModel(factory = RoomDemoDetailViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RoomDemoDetailScreen(
        state, onBack, onEdit, viewModel::sellOne, viewModel::delete, onSaveWithViews = viewModel::update
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDemoDetailScreen(
    state: RoomDemoDetailState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSell: () -> Unit,
    onDelete: () -> Unit,
    onSaveWithViews: ((String, String, String) -> Unit)? = null
) {
    var showViewEditor by rememberSaveable(state.item?.id) { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.testTag("room26_detail_screen"),
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.room26_detail_title)) },
            navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) } }
        ) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.room_disclaimer), style = MaterialTheme.typography.bodyMedium)
            val item = state.item
            if (!state.loaded) {
                Text(stringResource(R.string.room26_loading))
            } else if (item == null) {
                Text(stringResource(R.string.room26_not_found), modifier = Modifier.testTag("room26_missing"))
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(item.name, style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(R.string.room_item_summary, item.priceCents / 100,
                            (item.priceCents % 100).toString().padStart(2, '0'), item.stock),
                            modifier = Modifier.testTag("room26_stock"))
                        Button(
                            onClick = onSell,
                            enabled = RoomDemoInventoryRules.canSell(item) && !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room26_sell")
                        ) { Text(stringResource(R.string.room26_sell_one)) }
                        if (item.stock == 0) Text(stringResource(R.string.room26_out_of_stock),
                            color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = onEdit, enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room26_edit")) {
                            Text(stringResource(R.string.room26_edit_button))
                        }
                        if (onSaveWithViews != null) {
                            OutlinedButton(
                                onClick = { showViewEditor = true },
                                enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().testTag("room32_open_dialog")
                            ) { Text(stringResource(R.string.room32_open_button)) }
                        }
                        OutlinedButton(onClick = onDelete, enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room26_delete")) {
                            Text(stringResource(R.string.room_delete))
                        }
                    }
                }
                if (showViewEditor && onSaveWithViews != null) {
                    LegacyRoomEditDialog(
                        item = item,
                        busy = state.busy,
                        onDismiss = { showViewEditor = false },
                        onSave = { name, price, stock ->
                            onSaveWithViews(name, price, stock)
                            showViewEditor = false
                        }
                    )
                }
            }
            val feedbackRes = room26Message(state.feedback)
            if (feedbackRes != null) Text(stringResource(feedbackRes),
                modifier = Modifier.testTag("room26_feedback"))
        }
    }
}

internal fun room26Message(message: RoomDemoMessage): Int? = when (message) {
    RoomDemoMessage.NONE -> null
    RoomDemoMessage.SAVED -> R.string.room_saved
    RoomDemoMessage.UPDATED -> R.string.room_updated
    RoomDemoMessage.DELETED -> R.string.room_deleted
    RoomDemoMessage.INVALID -> R.string.room_invalid
    RoomDemoMessage.ERROR -> R.string.room_error
    RoomDemoMessage.SOLD -> R.string.room26_sold
    RoomDemoMessage.NO_STOCK -> R.string.room26_out_of_stock
    RoomDemoMessage.NOT_FOUND -> R.string.room26_not_found
}

@Preview(showBackground = true)
@Composable
private fun RoomDemoDetailPreview() {
    FarmAppTheme { RoomDemoDetailScreen(
        RoomDemoDetailState(item = RoomDemoItem(1, "Gasas (demo)", 650, 2), loaded = true),
        {}, {}, {}, {}
    ) }
}
