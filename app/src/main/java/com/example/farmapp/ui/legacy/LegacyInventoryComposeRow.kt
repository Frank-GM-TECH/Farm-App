package com.example.farmapp.ui.legacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * C31: convierte la fila XML del codelab Juice Tracker en una fila declarativa.
 * Es stateless: recibe un objeto Room y notifica clics mediante una lambda.
 */
@Composable
fun LegacyInventoryComposeRow(
    item: RoomDemoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .defaultMinSize(minHeight = 72.dp)
            .testTag("legacy_compose_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.legacy_item_subtitle,
                        item.priceCents / 100.0,
                        item.stock
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.legacy_compose_edit_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(name = "Cotejo 31 - Compose dentro de RecyclerView", showBackground = true, widthDp = 390)
@Composable
private fun LegacyInventoryComposeRowPreview() {
    FarmAppTheme {
        LegacyInventoryComposeRow(
            item = RoomDemoItem(
                id = 7L,
                name = "Gasas estériles (demo)",
                priceCents = 650L,
                stock = 12
            ),
            onClick = {}
        )
    }
}
