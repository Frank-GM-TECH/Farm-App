package com.example.farmapp.ui.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private data class DemoPaymentOption(
    val id: String,
    val name: String,
    val explanation: String
)

private val demoPaymentOptions = listOf(
    DemoPaymentOption("Yape / Plin", "Yape / Plin", "Billetera digital (demostración)"),
    DemoPaymentOption("Tarjeta de débito o crédito", "Tarjeta de débito o crédito", "Sin ingresar datos bancarios"),
    DemoPaymentOption("Pago contraentrega", "Pago contraentrega", "Disponibilidad y condiciones por confirmar")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppPaymentMethodScreen(
    currentMethod: String?,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by rememberSaveable(currentMethod) {
        mutableStateOf(currentMethod?.takeIf { method ->
            demoPaymentOptions.any { it.id == method }
        })
    }

    Scaffold(
        modifier = modifier.testTag("payment_method_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Método de pago") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
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
                text = "¿Cómo deseas pagar?",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Selecciona una opción para este pedido. " +
                        "Esta pantalla es una demostración: no se procesan pagos ni se solicitan datos financieros.",
                style = MaterialTheme.typography.bodyMedium
            )
            demoPaymentOptions.forEach { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMethod = option.id }
                        .testTag("payment_option_${demoPaymentOptions.indexOf(option)}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == option.id,
                            onClick = { selectedMethod = option.id }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(option.name, style = MaterialTheme.typography.titleMedium)
                            Text(option.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { selectedMethod?.let(onSave) },
                enabled = selectedMethod != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_save_button")
            ) {
                Text("Guardar método de pago")
            }
        }
    }
}