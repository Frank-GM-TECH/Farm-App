package com.example.farmapp.ui.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.catalog.CatalogAnalyzer
import com.example.farmapp.catalog.CatalogUiState
import com.example.farmapp.catalog.CatalogViewModel
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoCategory
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Route/puente con ViewModel: recoge StateFlow respetando el ciclo de vida.
 * En los tests se puede inyectar un CatalogViewModel con un repositorio ficticio.
 * Conserva slots header y footer y UNA sola lista vertical, como en el Cotejo 08.
 */
@Composable
fun CatalogLearningPreview(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    catalogViewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory),
    onProductSelected: ((DemoProduct) -> Unit)? = null
) {
    val uiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreenContent(
        uiState = uiState,
        onQueryChanged = catalogViewModel::onQueryChanged,
        onCategorySelected = catalogViewModel::onCategorySelected,
        modifier = modifier,
        header = header,
        footer = footer,
        onProductSelected = onProductSelected
    )
}

/**
 * Componible SIN ESTADO de negocio: recibe datos y emite callbacks.
 * Es equivalente a la UI controlada por GameViewModel del codelab de ViewModel.
 */
@Composable
fun CatalogScreenContent(
    uiState: CatalogUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (DemoCategory?) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    onProductSelected: ((DemoProduct) -> Unit)? = null
) {
    val snapshot = uiState.snapshot
    LazyColumn(
        modifier = modifier.testTag("catalog_list"),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "home_header", contentType = "header") { header() }
        item(key = "catalog_controls", contentType = "controls") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.catalog_demo_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(R.string.catalog_demo_disclaimer),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChanged,
                    label = { Text(stringResource(R.string.catalog_search_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("catalog_search")
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text(stringResource(R.string.catalog_all_categories)) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("catalog_filter_all")
                    )
                    DemoCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(categoryText(category)) },
                            modifier = Modifier.heightIn(min = 48.dp)
                                .testTag("catalog_filter_${category.name}")
                        )
                    }
                }
                Text(
                    stringResource(
                        R.string.catalog_demo_counts,
                        snapshot.products.size,
                        snapshot.totalUnits
                    ),
                    modifier = Modifier.testTag("catalog_results_summary")
                )
            }
        }
        if (snapshot.products.isEmpty()) {
            item(key = "catalog_no_results", contentType = "empty") {
                Text(stringResource(R.string.catalog_empty), modifier = Modifier.testTag("catalog_empty"))
            }
        } else {
            snapshot.grouped.keys.sortedBy { it.name }.forEach { category ->
                item(key = "group_${category.name}", contentType = "group_title") {
                    Text(
                        categoryText(category),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                items(
                    items = snapshot.grouped[category].orEmpty(),
                    key = { product -> product.sku },
                    contentType = { "product_card" }
                ) { product ->
                    AnimatedProductDemoCard(product = product, onOpenProduct = onProductSelected)
                }
            }
        }
        item(key = "home_footer", contentType = "footer") { footer() }
    }
}

/**
 * Preview INTERACTIVA sin Firebase ni ViewModelStoreOwner.
 * El estado solo vive en esta demostración visual. La pantalla real utiliza
 * CatalogViewModel y StateFlow; NO se mueve la lógica de negocio a Compose.
 */
@Preview(name = "Cotejo 14 - UI interactiva", showBackground = true, heightDp = 620)
@Composable
private fun CatalogLearningPreviewPreview() {
    val analyzer = remember { CatalogAnalyzer(DemoCatalogData.repository) }
    var previewState by remember {
        mutableStateOf(CatalogUiState("", null, analyzer.search()))
    }
    FarmAppTheme {
        CatalogScreenContent(
            uiState = previewState,
            onQueryChanged = { query ->
                previewState = CatalogUiState(
                    query,
                    previewState.selectedCategory,
                    analyzer.search(query, previewState.selectedCategory)
                )
            },
            onCategorySelected = { category ->
                previewState = CatalogUiState(
                    previewState.query,
                    category,
                    analyzer.search(previewState.query, category)
                )
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}
