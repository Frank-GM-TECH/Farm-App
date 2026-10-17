package com.example.farmapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import com.example.farmapp.ui.catalog.CatalogAvailabilityPanel
import com.example.farmapp.ui.catalog.CatalogSyncProgressPanel
import com.example.farmapp.ui.catalog.RemoteProductsPanel
import com.example.farmapp.network.RemoteProductsViewModel
import com.example.farmapp.catalog.CatalogSyncViewModel
import com.example.farmapp.catalog.CatalogAvailabilityViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.catalog.CatalogAnalyzer
import com.example.farmapp.catalog.CatalogContentLayout
import com.example.farmapp.catalog.CatalogUiState
import com.example.farmapp.catalog.CatalogViewModel
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoCategory
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.catalogContentLayoutFor
import com.example.farmapp.ui.catalog.CatalogScreenContent
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * La ruta recoge StateFlow del ViewModel (cotejo 14). El contenido no conoce
 * ViewModel ni NavController; recibe el estado y callbacks (state hoisting).
 */
@Composable
fun CatalogRouteScreen(
    onBack: () -> Unit,
    onSelectProduct: (DemoProduct) -> Unit,
    onShareProduct: (DemoProduct) -> Unit,
    onOpenRemoteGallery: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory),
    availabilityViewModel: CatalogAvailabilityViewModel = viewModel(factory = CatalogAvailabilityViewModel.Factory),
    syncViewModel: CatalogSyncViewModel = viewModel(factory = CatalogSyncViewModel.Factory),
    remoteProductsViewModel: RemoteProductsViewModel = viewModel(factory = RemoteProductsViewModel.Factory)
) {
    val uiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
    val availabilityState by availabilityViewModel.uiState.collectAsStateWithLifecycle()
    val syncState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val remoteProductsState by remoteProductsViewModel.uiState.collectAsStateWithLifecycle()
    CatalogAdaptiveContent(
        uiState = uiState,
        onQueryChanged = catalogViewModel::onQueryChanged,
        onCategorySelected = catalogViewModel::onCategorySelected,
        onBack = onBack,
        onSelectProduct = onSelectProduct,
        onShareProduct = onShareProduct,
        availabilityContent = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CatalogAvailabilityPanel(
                    uiState = availabilityState,
                    onRefresh = availabilityViewModel::refresh,
                    onCancel = availabilityViewModel::cancelRefresh
                )
                CatalogSyncProgressPanel(
                    uiState = syncState,
                    onStartOrResume = syncViewModel::startOrResume,
                    onPause = syncViewModel::pause,
                    onReset = syncViewModel::reset
                )
                RemoteProductsPanel(
                    uiState = remoteProductsState,
                    onRefresh = remoteProductsViewModel::refresh,
                    onRawJson = remoteProductsViewModel::previewJson,
                    onOpenGallery = onOpenRemoteGallery
                )
            }
        }
    )
}

/**
 * El tamaño de la VENTANA determina la navegación (C17); el espacio restante
 * determina el CONTENIDO (C18). No debemos crear dos paneles de ~300dp solo
 * porque la ventana se clasifique como Expanded y tenga un drawer de 240dp.
 * Un único LazyColumn vertical por panel y una ficha desplazable por separado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogAdaptiveContent(
    uiState: CatalogUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (DemoCategory?) -> Unit,
    onBack: () -> Unit,
    onSelectProduct: (DemoProduct) -> Unit,
    onShareProduct: (DemoProduct) -> Unit,
    modifier: Modifier = Modifier,
    availabilityContent: @Composable () -> Unit = {}
) {
    var selectedSku by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier = modifier.fillMaxSize().testTag("catalog_screen")) {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_catalog_title)) },
            navigationIcon = {
                TextButton(onClick = onBack, modifier = Modifier.testTag("back_from_catalog")) {
                    Text(stringResource(R.string.nav_back))
                }
            }
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val layout = catalogContentLayoutFor(maxWidth.value)
            if (layout == CatalogContentLayout.LIST_DETAIL) {
                val selectedProduct = uiState.snapshot.products.firstOrNull { it.sku == selectedSku }
                    ?: uiState.snapshot.products.firstOrNull()
                Row(modifier = Modifier.fillMaxSize().testTag("catalog_two_pane")) {
                    Box(
                        modifier = Modifier.weight(0.55f).fillMaxHeight()
                            .testTag("catalog_list_pane")
                    ) {
                        CatalogScreenContent(
                            uiState = uiState,
                            onQueryChanged = onQueryChanged,
                            onCategorySelected = onCategorySelected,
                            onProductSelected = { selectedSku = it.sku },
                            header = availabilityContent,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.width(1.dp).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Column(
                        modifier = Modifier.weight(0.45f).fillMaxHeight()
                            .testTag("catalog_detail_pane")
                    ) {
                        Text(
                            text = stringResource(R.string.adaptive_detail_pane_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                        )
                        ProductDetailBody(
                            product = selectedProduct,
                            onShare = onShareProduct,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxSize().testTag("catalog_single_pane")) {
                    CatalogScreenContent(
                        uiState = uiState,
                        onQueryChanged = onQueryChanged,
                        onCategorySelected = onCategorySelected,
                        onProductSelected = onSelectProduct,
                        header = availabilityContent,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/** Vista previa sin Firebase ni ViewModelStoreOwner, con datos de demostración. */
@Composable
private fun AdaptiveCatalogPreview(windowWidth: WindowWidthSizeClass) {
    val analyzer = remember { CatalogAnalyzer(DemoCatalogData.repository) }
    var previewState by remember {
        mutableStateOf(CatalogUiState("", null, analyzer.search()))
    }
    FarmAppTheme {
        FarmAppAdaptiveNavigation(
            widthSizeClass = windowWidth,
            selectedDestination = FarmAppDestinations.CATALOG,
            onNavigateHome = {},
            onNavigateCatalog = {},
            modifier = Modifier.fillMaxSize()
        ) {
            CatalogAdaptiveContent(
                uiState = previewState,
                onQueryChanged = { q ->
                    previewState = CatalogUiState(
                        q, previewState.selectedCategory,
                        analyzer.search(q, previewState.selectedCategory)
                    )
                },
                onCategorySelected = { category ->
                    previewState = CatalogUiState(
                        previewState.query, category,
                        analyzer.search(previewState.query, category)
                    )
                },
                onBack = {},
                onSelectProduct = {},
                onShareProduct = {}
            )
        }
    }
}

@Preview(name = "C18 - Teléfono 390 dp", widthDp = 390, heightDp = 800, showBackground = true)
@Composable
private fun CatalogCompactContentPreview() = AdaptiveCatalogPreview(WindowWidthSizeClass.Compact)

@Preview(name = "C18 - Tablet 750 dp", widthDp = 750, heightDp = 800, showBackground = true)
@Composable
private fun CatalogMediumContentPreview() = AdaptiveCatalogPreview(WindowWidthSizeClass.Medium)

@Preview(name = "C18 - Tablet amplia 1280 dp", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun CatalogExpandedContentPreview() = AdaptiveCatalogPreview(WindowWidthSizeClass.Expanded)
