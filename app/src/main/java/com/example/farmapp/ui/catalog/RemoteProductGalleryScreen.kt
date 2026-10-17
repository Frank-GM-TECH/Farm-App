package com.example.farmapp.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.farmapp.R
import com.example.farmapp.data.preferences.GalleryLayoutPreference
import com.example.farmapp.network.RemoteProduct
import com.example.farmapp.network.RemoteProductsError
import com.example.farmapp.network.RemoteProductsPhase
import com.example.farmapp.network.RemoteProductsUiState
import com.example.farmapp.network.RemoteProductsViewModel
import com.example.farmapp.ui.theme.FarmAppTheme
import java.util.Locale

/** Ruta: obtiene el ViewModel mediante la fábrica y el contenedor manual del C22. */
@Composable
fun RemoteProductGalleryRoute(
    onBack: () -> Unit,
    viewModel: RemoteProductsViewModel = viewModel(factory = RemoteProductsViewModel.Factory),
    galleryLayout: GalleryLayoutPreference = GalleryLayoutPreference.GRID,
    galleryLayoutReady: Boolean = true,
    onGalleryLayoutSelected: (GalleryLayoutPreference) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        if (viewModel.uiState.value.phase == RemoteProductsPhase.IDLE) viewModel.refresh()
    }
    RemoteProductGalleryScreen(
        uiState = state,
        onRetry = viewModel::refresh,
        onBack = onBack,
        galleryLayout = galleryLayout,
        galleryLayoutReady = galleryLayoutReady,
        onGalleryLayoutSelected = onGalleryLayoutSelected
    )
}

/** Pantalla sin estado propio, reutilizable para las pruebas de Compose con datos fake. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteProductGalleryScreen(
    uiState: RemoteProductsUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    galleryLayout: GalleryLayoutPreference = GalleryLayoutPreference.GRID,
    galleryLayoutReady: Boolean = true,
    onGalleryLayoutSelected: (GalleryLayoutPreference) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.testTag("remote_gallery_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gallery_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("gallery_back")) {
                        Text(stringResource(R.string.nav_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState.phase) {
            RemoteProductsPhase.SUCCESS -> {
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.pref_gallery_title),
                            style = MaterialTheme.typography.labelLarge
                        )
                        GalleryLayoutPreference.entries.forEach { option ->
                            FilterChip(
                                selected = galleryLayout == option,
                                enabled = galleryLayoutReady,
                                onClick = { onGalleryLayoutSelected(option) },
                                label = { Text(stringResource(
                                    if (option == GalleryLayoutPreference.GRID)
                                        R.string.pref_gallery_grid else R.string.pref_gallery_list
                                )) },
                                modifier = Modifier.heightIn(min = 48.dp)
                                    .testTag("gallery_layout_${option.name.lowercase()}")
                            )
                        }
                    }
                    if (galleryLayout == GalleryLayoutPreference.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth().testTag("gallery_grid"),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.products, key = RemoteProduct::id) { product ->
                                RemotePhotoCard(product, Modifier.fillMaxWidth())
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().testTag("gallery_list"),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                count = uiState.products.size,
                                key = { uiState.products[it].id }
                            ) { index ->
                                RemotePhotoListRow(uiState.products[index])
                            }
                        }
                    }
                }
            }
            RemoteProductsPhase.LOADING -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.testTag("gallery_loading"))
                }
            }
            RemoteProductsPhase.ERROR -> {
                val errorText = when (uiState.error) {
                    RemoteProductsError.NETWORK -> R.string.net_demo_error_network
                    RemoteProductsError.HTTP -> R.string.net_demo_error_http
                    RemoteProductsError.DATA -> R.string.net_demo_error_parse
                    else -> R.string.net_demo_error_generic
                }
                GalleryMessage(
                    text = stringResource(errorText),
                    actionLabel = stringResource(R.string.gallery_retry),
                    onAction = onRetry,
                    modifier = Modifier.padding(innerPadding).testTag("gallery_error")
                )
            }
            RemoteProductsPhase.EMPTY -> GalleryMessage(
                text = stringResource(R.string.net_demo_empty),
                actionLabel = stringResource(R.string.gallery_retry),
                onAction = onRetry,
                modifier = Modifier.padding(innerPadding).testTag("gallery_empty")
            )
            else -> GalleryMessage(
                text = stringResource(R.string.gallery_intro),
                actionLabel = stringResource(R.string.gallery_retry),
                onAction = onRetry,
                modifier = Modifier.padding(innerPadding).testTag("gallery_intro")
            )
        }
    }
}

@Composable
private fun GalleryMessage(
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 16.dp).heightIn(min = 48.dp).testTag("gallery_retry")
        ) { Text(actionLabel) }
    }
}

@Composable
fun RemotePhotoCard(product: RemoteProduct, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageDescription = stringResource(R.string.gallery_image_description, product.name)
    Card(modifier = modifier.testTag("gallery_photo_${product.id}")) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = imageDescription,
                placeholder = painterResource(R.drawable.ic_remote_photo_placeholder),
                error = painterResource(R.drawable.ic_remote_photo_error),
                fallback = painterResource(R.drawable.ic_remote_photo_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    .testTag("gallery_image_${product.id}")
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = stringResource(
                    R.string.net_demo_price,
                    String.format(Locale.US, "%.2f", product.priceUsd)
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            )
        }
    }
}

/** Variante de lista: datos iguales, presentación diferente (C27). */
@Composable
private fun RemotePhotoListRow(product: RemoteProduct) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth().testTag("gallery_row_${product.id}")) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl).crossfade(true).build(),
                contentDescription = stringResource(R.string.gallery_image_description, product.name),
                placeholder = painterResource(R.drawable.ic_remote_photo_placeholder),
                error = painterResource(R.drawable.ic_remote_photo_error),
                fallback = painterResource(R.drawable.ic_remote_photo_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(84.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
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
}

@Preview(name = "C27 - Galería persistente en lista", showBackground = true, widthDp = 390, heightDp = 680)
@Composable
private fun GalleryListPreview() = FarmAppTheme {
    RemoteProductGalleryScreen(
        uiState = RemoteProductsUiState(
            RemoteProductsPhase.SUCCESS,
            products = listOf(RemoteProduct(1, "Crema demo", 12.99), RemoteProduct(2, "Jabón demo", 6.50))
        ),
        onRetry = {}, onBack = {}, galleryLayout = GalleryLayoutPreference.LIST
    )
}

@Preview(name = "C23 - Galería con imágenes remotas", showBackground = true, widthDp = 390, heightDp = 680)
@Composable
private fun GallerySuccessPreview() = FarmAppTheme {
    RemoteProductGalleryScreen(
        uiState = RemoteProductsUiState(
            RemoteProductsPhase.SUCCESS,
            products = listOf(
                RemoteProduct(1, "Crema facial (demo)", 12.99),
                RemoteProduct(2, "Loción corporal (demo)", 8.50),
                RemoteProduct(3, "Protector cosmético (demo)", 17.25)
            )
        ), onRetry = {}, onBack = {}
    )
}

@Preview(name = "C23 - Error de conexión", showBackground = true, widthDp = 390, heightDp = 680)
@Composable
private fun GalleryErrorPreview() = FarmAppTheme {
    RemoteProductGalleryScreen(
        uiState = RemoteProductsUiState(
            RemoteProductsPhase.ERROR, error = RemoteProductsError.NETWORK
        ), onRetry = {}, onBack = {}
    )
}
