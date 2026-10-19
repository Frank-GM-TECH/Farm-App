package com.example.farmapp.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

/** HU04 - Paso 11. Solo datos de demostración, sin emisión electrónica. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppReceiptScreen(
    currentType: String?,
    currentRuc: String,
    currentBusinessName: String,
    onBack: () -> Unit,
    onSave: (type: String, ruc: String, businessName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by rememberSaveable { mutableStateOf(currentType ?: "Boleta simple") }
    var ruc by rememberSaveable { mutableStateOf(currentRuc) }
    var businessName by rememberSaveable { mutableStateOf(currentBusinessName) }

    val isInvoice = selectedType == "Factura"
    // Validación de formato, NO verificación real de RUC con SUNAT.
    val isInvoiceFormValid = ruc.length == 11 && ruc.all(Char::isDigit) &&
            businessName.trim().length >= 3
    val canSave = !isInvoice || isInvoiceFormValid

    Scaffold(
        modifier = modifier.testTag("receipt_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Comprobante de pago") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Elige tu comprobante", style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedType == "Boleta simple",
                    onClick = { selectedType = "Boleta simple" }
                )
                Text("Boleta simple")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isInvoice,
                    onClick = { selectedType = "Factura" }
                )
                Text("Factura")
            }

            if (isInvoice) {
                OutlinedTextField(
                    value = ruc,
                    onValueChange = { value -> ruc = value.filter(Char::isDigit).take(11) },
                    label = { Text("RUC (11 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = ruc.isNotEmpty() && ruc.length != 11,
                    modifier = Modifier.fillMaxWidth().testTag("receipt_ruc")
                )
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it.take(150) },
                    label = { Text("Razón social") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("receipt_business_name")
                )
                Text(
                    "Se comprueba solo el formato. No se verifica la existencia " +
                            "o el estado del RUC en SUNAT.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onSave(
                        selectedType,
                        if (isInvoice) ruc else "",
                        if (isInvoice) businessName.trim() else ""
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().testTag("receipt_save_button")
            ) {
                Text("Guardar comprobante")
            }
            Text(
                "Versión de demostración: no se emiten boletas ni facturas reales.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}