package com.example.farmapp.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppDeliveryAddressScreen(
    currentAddress: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var address by rememberSaveable(currentAddress) {
        mutableStateOf(currentAddress)
    }

    var reference by rememberSaveable {
        mutableStateOf("")
    }

    val validAddress = address.trim().length >= 8

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dirección de entrega") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "¿Dónde entregaremos tu pedido?",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección completa") },
                placeholder = {
                    Text("Av. Principal 123, Lima")
                },
                isError = address.isNotBlank() && !validAddress,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = reference,
                onValueChange = { reference = it },
                label = { Text("Referencia (opcional)") },
                placeholder = {
                    Text("Frente al parque")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Dirección de demostración. " +
                        "Todavía no se calculan costos ni tiempos reales.",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = {
                    val fullAddress = buildString {
                        append(address.trim())
                        if (reference.isNotBlank()) {
                            append(" - Ref.: ")
                            append(reference.trim())
                        }
                    }
                    onSave(fullAddress)
                },
                enabled = validAddress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar dirección")
            }
        }
    }
}