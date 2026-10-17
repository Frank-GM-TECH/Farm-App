package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.network.RemoteProductsError
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import java.util.Locale

/** C21: solo UI. Los datos llegan desde ViewModel por StateFlow. */
@Composable
fun RemoteProductsPanel(
    uiState: RemoteProductsUiState,
    onRefresh: () -> Unit,
    onRawJson: () -> Unit = {},
    onOpenGallery: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().testTag("remote_products_panel")) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.net_demo_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.net_demo_warning),
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRefresh,
                    enabled = uiState.phase != RemoteProductsPhase.LOADING,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("net_demo_refresh")
                ) {
                    Text(stringResource(R.string.net_demo_fetch))
                }
                OutlinedButton(
                    onClick = onRawJson,
                    enabled = uiState.phase != RemoteProductsPhase.LOADING,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("net_demo_raw")
                ) {
                    Text(stringResource(R.string.net_demo_raw_button))
                }
            }
            if (onOpenGallery != null) {
                OutlinedButton(
                    onClick = onOpenGallery,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("net_demo_open_gallery")
                ) { Text(stringResource(R.string.gallery_open)) }
            }
            Column(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (uiState.phase) {
                    RemoteProductsPhase.IDLE -> Text(
                        stringResource(R.string.net_demo_idle),
                        modifier = Modifier.testTag("net_demo_idle")
                    )
                    RemoteProductsPhase.LOADING -> Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("net_demo_loading"))
                        Text(stringResource(R.string.net_demo_loading_text))
                    }
                    RemoteProductsPhase.SUCCESS -> {
                        Text(
                            stringResource(R.string.net_demo_success, uiState.products.size),
                            modifier = Modifier.testTag("net_demo_success")
                        )
                        uiState.products.forEach { product ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(product.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    stringResource(
                                        R.string.net_demo_price,
                                        String.format(Locale.US, "%.2f", product.priceUsd)
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    RemoteProductsPhase.RAW_SUCCESS -> {
                        Text(stringResource(R.string.net_demo_raw_caption))
                        Text(
                            uiState.rawJsonPreview.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("net_demo_raw_result")
                        )
                    }
                    RemoteProductsPhase.EMPTY -> Text(
                        stringResource(R.string.net_demo_empty),
                        modifier = Modifier.testTag("net_demo_empty")
                    )
                    RemoteProductsPhase.ERROR -> {
                        val message = when (uiState.error) {
                            RemoteProductsError.NETWORK -> R.string.net_demo_error_network
                            RemoteProductsError.HTTP -> R.string.net_demo_error_http
                            RemoteProductsError.DATA -> R.string.net_demo_error_parse
                            else -> R.string.net_demo_error_generic
                        }
                        Text(
                            stringResource(message),
                            modifier = Modifier.testTag("net_demo_error")
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "C21 - API: estado correcto", showBackground = true)
@Composable
private fun RemoteProductsSuccessPreview() = FarmAppTheme {
    RemoteProductsPanel(
        uiState = RemoteProductsUiState(
            phase = RemoteProductsPhase.SUCCESS,
            products = listOf(
                RemoteProduct(1, "Crema hidratante (demo)", 12.99),
                RemoteProduct(2, "Loción facial (demo)", 8.50)
            )
        ),
        onRefresh = {}
    )
}

@Preview(name = "C21 - API: sin Internet", showBackground = true)
@Composable
private fun RemoteProductsNetworkErrorPreview() = FarmAppTheme {
    RemoteProductsPanel(
        uiState = RemoteProductsUiState(
            phase = RemoteProductsPhase.ERROR,
            error = RemoteProductsError.NETWORK
        ),
        onRefresh = {}
    )
}
