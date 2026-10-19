package com.example.farmapp.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * HU03 - Pantalla comercial de Cuenta.
 *
 * Los callbacks permiten conectarla con
 * la navegación actual sin depender
 * directamente del NavController.
 */
@Composable
fun FarmAppAccountScreen(
    email: String,
    onOpenProfile: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenAddresses: () -> Unit,
    onSignOut: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenSqlLab: () -> Unit,
    onOpenRoomDemo: () -> Unit,
    onOpenBackgroundReport: () -> Unit,
    onOpenLegacyViews: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTechnicalTools by rememberSaveable {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("farmapp_account_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Encabezado
        item {
            Column {
                Text(
                    text = "Mi cuenta",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Gestiona tu información en FarmApp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tarjeta del usuario
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenProfile)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(8.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Mi perfil FarmApp",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Ver y editar mis datos",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }

        // Accesos rápidos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                AccountQuickAction(
                    title = "Mi perfil",
                    icon = Icons.Filled.Person,
                    onClick = onOpenProfile,
                    modifier = Modifier.weight(1f)
                )

                AccountQuickAction(
                    title = "Mis pedidos",
                    icon = Icons.Filled.Inventory2,
                    onClick = onOpenOrders,
                    modifier = Modifier.weight(1f)
                )

                AccountQuickAction(
                    title = "Favoritos",
                    icon = Icons.Filled.FavoriteBorder,
                    onClick = onOpenFavorites,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sección personal
        item {
            Text(
                text = "Mi información",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            AccountMenuOption(
                title = "Mis direcciones",
                subtitle = "Consulta tus direcciones de entrega",
                icon = Icons.Filled.LocationOn,
                onClick = onOpenAddresses
            )
        }

        item {
            AccountMenuOption(
                title = "Información personal",
                subtitle = "Nombre, teléfono, dirección y fotografía",
                icon = Icons.Filled.Person,
                onClick = onOpenProfile
            )
        }

        // Módulos previos conservados
        item {
            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            AccountMenuOption(
                title = "Herramientas de aprendizaje",
                subtitle = "Accede a los módulos anteriores",
                icon = Icons.Filled.Settings,
                onClick = {
                    showTechnicalTools = !showTechnicalTools
                }
            )
        }

        if (showTechnicalTools) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenCatalog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Catálogo educativo")
                    }

                    OutlinedButton(
                        onClick = onOpenSqlLab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Laboratorio SQL")
                    }

                    OutlinedButton(
                        onClick = onOpenRoomDemo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Demostración Room")
                    }

                    OutlinedButton(
                        onClick = onOpenBackgroundReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Informe en segundo plano")
                    }

                    OutlinedButton(
                        onClick = onOpenLegacyViews,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vistas tradicionales de Android")
                    }
                }
            }
        }

        // Cerrar sesión
        item {
            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_sign_out")
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Cerrar sesión",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Botón pequeño para los accesos rápidos.
 */
@Composable
private fun AccountQuickAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
    }
}

/**
 * Fila de opciones de Cuenta.
 */
@Composable
private fun AccountMenuOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null
        )
    }
}