package com.example.farmapp.ui.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.data.room.RoomDemoMessage
import com.example.farmapp.data.room.RoomDemoUiState
import com.example.farmapp.data.room.RoomDemoViewModel
import com.example.farmapp.ui.theme.FarmAppTheme

@Composable
fun RoomDemoRoute(
    onBack: () -> Unit,
    onItemDetail: (Long) -> Unit = {},
    viewModel: RoomDemoViewModel = viewModel(factory = RoomDemoViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RoomDemoScreen(
        state = state,
        onBack = onBack,
        onSave = viewModel::save,
        onIncrease = viewModel::increase,
        onDelete = viewModel::remove,
        onItemDetail = onItemDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDemoScreen(
    state: RoomDemoUiState,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onIncrease: (Long) -> Unit,
    onDelete: (RoomDemoItem) -> Unit,
    onItemDetail: (Long) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.testTag("room_demo_screen"),
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.room_title)) },
            navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) } }
        ) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("room_demo_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(stringResource(R.string.room_disclaimer), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            stringResource(R.string.room_form_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.room_name)) },
                            singleLine = true,
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room_name")
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text(stringResource(R.string.room_price)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room_price")
                        )
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text(stringResource(R.string.room_stock)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().testTag("room_stock")
                        )
                        Button(
                            onClick = { onSave(name, price, stock) },
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("room_save")
                        ) { Text(stringResource(R.string.room_save)) }
                    }
                }
            }
            item {
                val message = when (state.message) {
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
                if (message != null) Text(stringResource(message), modifier = Modifier.testTag("room_feedback"))
                if (state.busy) Text(stringResource(R.string.room_busy))
            }
            item {
                Text(
                    stringResource(R.string.room_count, state.items.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            if (state.items.isEmpty()) {
                item { Text(stringResource(R.string.room_empty), modifier = Modifier.testTag("room_empty")) }
            }
            items(state.items, key = { it.id }) { product ->
                Card(modifier = Modifier.fillMaxWidth().testTag("room_item_${product.id}")) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.room_item_summary, product.priceCents / 100, (product.priceCents % 100).toString().padStart(2, '0'), product.stock))
                        OutlinedButton(
                            onClick = { onItemDetail(product.id) },
                            modifier = Modifier.fillMaxWidth().testTag("room_detail_${product.id}")
                        ) { Text(stringResource(R.string.room26_view_detail)) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { onIncrease(product.id) }, enabled = !state.busy && product.stock < 99999, modifier = Modifier.testTag("room_increase_${product.id}")) {
                                Text(stringResource(R.string.room_increase))
                            }
                            OutlinedButton(onClick = { onDelete(product) }, enabled = !state.busy, modifier = Modifier.testTag("room_delete_${product.id}")) {
                                Text(stringResource(R.string.room_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomDemoScreenPreview() {
    FarmAppTheme {
        RoomDemoScreen(
            state = RoomDemoUiState(items = listOf(RoomDemoItem(id = 1, name = "Gasas (demo)", priceCents = 650, stock = 3))),
            onBack = {}, onSave = { _, _, _ -> }, onIncrease = {}, onDelete = {}
        )
    }
}
