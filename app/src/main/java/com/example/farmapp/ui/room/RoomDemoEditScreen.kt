package com.example.farmapp.ui.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoDetailState
import com.example.farmapp.data.room.RoomDemoDetailViewModel
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.ui.theme.FarmAppTheme

@Composable
fun RoomDemoEditRoute(
    onBack: () -> Unit,
    viewModel: RoomDemoDetailViewModel = viewModel(factory = RoomDemoDetailViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RoomDemoEditScreen(state, onBack, viewModel::update)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDemoEditScreen(
    state: RoomDemoDetailState,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    Scaffold(
        modifier = Modifier.testTag("room26_edit_screen"),
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.room26_edit_title)) },
            navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) } }
        ) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val item = state.item
            if (!state.loaded) Text(stringResource(R.string.room26_loading))
            else if (item == null) Text(stringResource(R.string.room26_not_found))
            else {
                // Las claves evitan sobrescribir lo que escribe el usuario cuando Flow emite.
                var name by rememberSaveable(item.id) { mutableStateOf(item.name) }
                var price by rememberSaveable(item.id) {
                    mutableStateOf("${item.priceCents / 100}.${(item.priceCents % 100).toString().padStart(2, '0')}")
                }
                var stock by rememberSaveable(item.id) { mutableStateOf(item.stock.toString()) }
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.room_name)) },
                    enabled = !state.busy, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("room26_edit_name"))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text(stringResource(R.string.room_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !state.busy, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("room26_edit_price"))
                OutlinedTextField(value = stock, onValueChange = { stock = it },
                    label = { Text(stringResource(R.string.room_stock)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !state.busy, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("room26_edit_stock"))
                Button(
                    onClick = { onSave(name, price, stock) }, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().testTag("room26_update")
                ) { Text(stringResource(R.string.room26_save_changes)) }
            }
            val feedback = room26Message(state.feedback)
            if (feedback != null) Text(stringResource(feedback),
                modifier = Modifier.testTag("room26_feedback"),
                color = if (state.feedback.name == "ERROR") MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomDemoEditPreview() {
    FarmAppTheme { RoomDemoEditScreen(
        RoomDemoDetailState(item = RoomDemoItem(1,"Alcohol (demo)",895,4),loaded=true),
        {}, { _,_,_ -> }
    ) }
}
