package com.example.farmapp.ui.interop

import android.text.Editable
import android.text.TextWatcher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoForm
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.databinding.DialogLegacyItemBinding
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Cotejo 32: Android Views DENTRO de Compose.
 * Reutiliza exactamente el XML y ViewBinding del diálogo del cotejo 30.
 * No crea repositorios ni modifica Room: solo eleva el texto al estado Compose.
 */
@Composable
fun LegacyRoomEditDialog(
    item: RoomDemoItem,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val name = rememberSaveable(item.id) { mutableStateOf(item.name) }
    val price = rememberSaveable(item.id) { mutableStateOf(room32PriceText(item.priceCents)) }
    val stock = rememberSaveable(item.id) { mutableStateOf(item.stock.toString()) }
    val validationAttempted = rememberSaveable(item.id) { mutableStateOf(false) }
    val isInvalid = validationAttempted.value &&
        RoomDemoForm.validate(name.value, price.value, stock.value) == null

    AlertDialog(
        modifier = Modifier.testTag("room32_dialog"),
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.room32_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.room32_dialog_description))
                // key reconstruye los EditText cuando se cambia a otro artículo.
                key(item.id) {
                    AndroidViewBinding(
                        factory = DialogLegacyItemBinding::inflate,
                        modifier = Modifier.fillMaxWidth().testTag("room32_android_view")
                    ) {
                        // AndroidViewBinding puede reevaluar este bloque durante
                        // recomposiciones. Instalamos los TextWatcher UNA SOLA VEZ.
                        if (legacyItemName.getTag(R.id.room32_view_initialized) != true) {
                            legacyItemName.setText(name.value)
                            legacyItemPrice.setText(price.value)
                            legacyItemStock.setText(stock.value)

                            legacyItemName.onTextChange { name.value = it }
                            legacyItemPrice.onTextChange { price.value = it }
                            legacyItemStock.onTextChange { stock.value = it }
                            legacyItemName.setTag(R.id.room32_view_initialized, true)
                        }
                        legacyItemName.isEnabled = !busy
                        legacyItemPrice.isEnabled = !busy
                        legacyItemStock.isEnabled = !busy
                        legacyFormError.text = if (isInvalid) {
                            root.context.getString(R.string.legacy_validation_error)
                        } else {
                            ""
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("room32_save"),
                enabled = !busy,
                onClick = {
                    validationAttempted.value = true
                    if (RoomDemoForm.validate(name.value, price.value, stock.value) != null) {
                        onSave(name.value, price.value, stock.value)
                    }
                }
            ) { Text(stringResource(R.string.room26_save_changes)) }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.testTag("room32_cancel"),
                enabled = !busy,
                onClick = onDismiss
            ) { Text(stringResource(R.string.legacy_cancel)) }
        }
    )
}

/** Un único observador nativo por campo y por instancia inflada. */
private fun android.widget.EditText.onTextChange(onChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChanged(s?.toString().orEmpty())
        }
        override fun afterTextChanged(s: Editable?) = Unit
    })
}

/** Conversión exacta de centavos a texto para el editor XML. */
fun room32PriceText(priceCents: Long): String {
    require(priceCents >= 0L) { "El precio no puede ser negativo" }
    return "${priceCents / 100}.${(priceCents % 100).toString().padStart(2, '0')}"
}

@Preview(name = "Cotejo 32 - Views dentro de Compose", showBackground = true, widthDp = 390)
@Composable
private fun LegacyRoomEditDialogPreview() {
    FarmAppTheme {
        LegacyRoomEditDialog(
            item = RoomDemoItem(id = 1, name = "Gasas (demo)", priceCents = 650, stock = 3),
            busy = false,
            onDismiss = {},
            onSave = { _, _, _ -> }
        )
    }
}
