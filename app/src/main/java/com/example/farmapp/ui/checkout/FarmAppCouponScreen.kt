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
import androidx.compose.ui.unit.dp
import java.util.Locale

/** HU04 Paso 12: cupones locales de demostración, sin servidor ni pagos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppCouponScreen(
    currentCode: String?,
    subtotalCents: Long,
    onBack: () -> Unit,
    onApply: (String, Long) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by rememberSaveable(currentCode) {
        mutableStateOf(currentCode.orEmpty())
    }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val normalized = code.trim().uppercase(Locale.ROOT)
    val supported = normalized == "FARMAPP10"
    val canApply = supported && subtotalCents > 0L
    val discountCents = subtotalCents.coerceAtLeast(0L) / 10L

    Scaffold(
        modifier = modifier.testTag("coupon_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Cupón de descuento") },
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
            Text("Cupón de demostración", style = MaterialTheme.typography.titleLarge)
            Text("Prueba el código FARMAPP10 para obtener un 10 % de descuento simulado sobre el subtotal.")
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    attempted = false
                },
                label = { Text("Código del cupón") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("coupon_code_input"),
                isError = attempted && !canApply
            )
            if (attempted && !canApply) {
                Text(
                    if (subtotalCents <= 0L) "Agrega productos al carrito antes de aplicar un cupón."
                    else "Código no válido para esta demostración.",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (canApply) {
                Text("Descuento estimado: S/ ${String.format(Locale.US, "%.2f", discountCents / 100.0)}")
            }
            Button(
                onClick = {
                    attempted = true
                    if (canApply) onApply(normalized, discountCents)
                },
                enabled = subtotalCents > 0L,
                modifier = Modifier.fillMaxWidth().testTag("coupon_apply_button")
            ) {
                Text("Aplicar cupón")
            }
            if (!currentCode.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth().testTag("coupon_remove_button")
                ) {
                    Text("Quitar cupón actual")
                }
            }
            Text(
                "Solo demostración. El descuento no corresponde a una promoción real.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}