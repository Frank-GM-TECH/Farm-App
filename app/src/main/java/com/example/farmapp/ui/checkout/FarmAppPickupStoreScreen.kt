package com.example.farmapp.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * HU04 - Selección de botica.
 *
 * Las sedes son ficticias y se usan
 * únicamente para probar el checkout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppPickupStoreScreen(
    selectedStoreId: String?,
    onBack: () -> Unit,
    onStoreSelected: (PickupStoreUi) -> Unit,
    modifier: Modifier = Modifier
) {

    val demoStores = remember {
        listOf(
            PickupStoreUi(
                id = "FARMAPP-DEMO-01",
                name = "FarmApp - Sede demo 01",
                address = "Dirección pendiente de configurar",
                openingHours =
                    "Horario de muestra: lunes a viernes, " +
                            "08:00 - 21:00"
            ),
            PickupStoreUi(
                id = "FARMAPP-DEMO-02",
                name = "FarmApp - Sede demo 02",
                address = "Dirección pendiente de configurar",
                openingHours =
                    "Horario de muestra: lunes a viernes, " +
                            "08:00 - 21:00"
            )
        )
    }

    val today = remember { LocalDate.now() }

    val schedule = remember(today) {
        estimatePickupSchedule(today)
    }

    val readyText = formatPickupDate(
        schedule.readyAt
    )

    val deadlineText = formatPickupDate(
        schedule.deadlineAt
    )

    Scaffold(
        modifier = modifier.testTag("pickup_store_screen"),

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Seleccionar botica")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "¿Dónde recogerás tu pedido?",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Selecciona una sede de demostración. " +
                            "Todavía no utilizamos boticas reales " +
                            "ni calculamos la distancia a tu ubicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(
                items = demoStores,
                key = { it.id }
            ) { store ->

                val selected = selectedStoreId == store.id

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )

                            Text(
                                text = store.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            if (selected) {
                                Icon(
                                    imageVector =
                                        Icons.Filled.CheckCircle,
                                    contentDescription =
                                        "Sede seleccionada",
                                    tint =
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = store.address,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = store.openingHours.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        Text(
                            text = "Disponible desde:",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(readyText)

                        Text(
                            text = "Plazo máximo de recojo:",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(deadlineText)

                        Text(
                            text = "Fechas estimadas de demostración. " +
                                    "No incluyen feriados ni disponibilidad real.",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {

                                val selectedStore = store.copy(
                                    estimatedReady =
                                        "Disponible desde: $readyText\n" +
                                                "Plazo máximo de recojo: " +
                                                deadlineText
                                )

                                onStoreSelected(selectedStore)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_pickup_${store.id}")
                        ) {
                            Text(
                                if (selected) {
                                    "Mantener esta sede"
                                } else {
                                    "Seleccionar esta sede"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}