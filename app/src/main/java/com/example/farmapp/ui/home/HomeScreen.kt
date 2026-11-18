
package com.example.farmapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.ui.components.FarmAppBrandHeader

@Composable
fun HomeScreen(
    email: String,
    onSignOut: () -> Unit,
    onOpenCatalog: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenSqlLab: () -> Unit = {},
    onOpenRoomDemo: () -> Unit = {},
    onOpenBackgroundReport: () -> Unit = {},
    onOpenLegacyViews: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .testTag("home_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FarmAppBrandHeader(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() }
            )

            Text(
                text = stringResource(
                    R.string.home_session_email,
                    email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.home_auth_section),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.home_next_steps)
                    )
                }
            }

            // Abrir catálogo
            Button(
                onClick = onOpenCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_catalog")
            ) {
                Text(stringResource(R.string.nav_open_catalog))
            }

            // HU02: Abrir perfil del usuario
            OutlinedButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_profile")
            ) {
                Text("Mi perfil")
            }

            // Laboratorio SQL
            OutlinedButton(
                onClick = onOpenSqlLab,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_sql")
            ) {
                Text(stringResource(R.string.sql_home_open))
            }

            // Demostración Room
            OutlinedButton(
                onClick = onOpenRoomDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_room")
            ) {
                Text(stringResource(R.string.room_home_open))
            }

            // Trabajo en segundo plano
            OutlinedButton(
                onClick = onOpenBackgroundReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_work")
            ) {
                Text(stringResource(R.string.work_home_open))
            }

            // Interoperabilidad con objetos View
            OutlinedButton(
                onClick = onOpenLegacyViews,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("navigate_to_legacy_views")
            ) {
                Text(stringResource(R.string.legacy_home_open))
            }

            // Cerrar sesión
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_sign_out")
            ) {
                Text(stringResource(R.string.home_sign_out))
            }
        }
    }
}
