package com.example.farmapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.ui.home.HomeScreen
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * No crea ni almacena el NavController: lo administra FarmAppNavHost.
 * Los destinos de nivel superior son Inicio y Catálogo; Producto queda bajo Catálogo.
 * AuthScreen no utiliza esta navegación (no se muestran rutas privadas sin sesión).
 * Los controles de Material 3 aportan áreas táctiles/semántica por defecto.
 */
@Composable
fun FarmAppAdaptiveNavigation(
    widthSizeClass: WindowWidthSizeClass,
    selectedDestination: String,
    onNavigateHome: () -> Unit,
    onNavigateCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when (navigationTypeFor(widthSizeClass)) {
        FarmAppNavigationType.BOTTOM_BAR -> {
            Scaffold(
                modifier = modifier,
                bottomBar = {
                    NavigationBar(modifier = Modifier.testTag("adaptive_bottom_bar")) {
                        NavigationBarItem(
                            selected = selectedDestination == FarmAppDestinations.HOME,
                            onClick = onNavigateHome,
                            icon = { FarmAppTopLevelIcon(true) },
                            label = { Text(stringResource(R.string.nav_home_title)) },
                            modifier = Modifier.testTag("adaptive_go_home")
                        )
                        NavigationBarItem(
                            selected = selectedDestination == FarmAppDestinations.CATALOG,
                            onClick = onNavigateCatalog,
                            icon = { FarmAppTopLevelIcon(false) },
                            label = { Text(stringResource(R.string.nav_catalog_short)) },
                            modifier = Modifier.testTag("adaptive_go_catalog")
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) { content() }
            }
        }
        FarmAppNavigationType.NAVIGATION_RAIL -> {
            Row(modifier = modifier) {
                NavigationRail(modifier = Modifier.testTag("adaptive_navigation_rail")) {
                    NavigationRailItem(
                        selected = selectedDestination == FarmAppDestinations.HOME,
                        onClick = onNavigateHome,
                        icon = { FarmAppTopLevelIcon(true) },
                        label = { Text(stringResource(R.string.nav_home_title)) },
                        modifier = Modifier.testTag("adaptive_go_home")
                    )
                    NavigationRailItem(
                        selected = selectedDestination == FarmAppDestinations.CATALOG,
                        onClick = onNavigateCatalog,
                        icon = { FarmAppTopLevelIcon(false) },
                        label = { Text(stringResource(R.string.nav_catalog_short)) },
                        modifier = Modifier.testTag("adaptive_go_catalog")
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) { content() }
            }
        }
        FarmAppNavigationType.PERMANENT_DRAWER -> {
            PermanentNavigationDrawer(
                modifier = modifier,
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(240.dp)
                            .testTag("adaptive_navigation_drawer")
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                        NavigationDrawerItem(
                            selected = selectedDestination == FarmAppDestinations.HOME,
                            onClick = onNavigateHome,
                            icon = { FarmAppTopLevelIcon(true) },
                            label = { Text(stringResource(R.string.nav_home_title)) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                                .testTag("adaptive_go_home")
                        )
                        NavigationDrawerItem(
                            selected = selectedDestination == FarmAppDestinations.CATALOG,
                            onClick = onNavigateCatalog,
                            icon = { FarmAppTopLevelIcon(false) },
                            label = { Text(stringResource(R.string.nav_catalog_short)) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                                .testTag("adaptive_go_catalog")
                        )
                    }
                }
            ) { content() }
        }
    }
}

@Composable
private fun FarmAppTopLevelIcon(isHome: Boolean) {
    Icon(
        painter = painterResource(
            if (isHome) R.drawable.ic_farmapp_logo else R.drawable.ic_catalog_nav
        ),
        contentDescription = null // El texto del destino aporta la etiqueta accesible.
    )
}

/** Previews reales de Inicio: no necesitan Firebase ni NavHostController. */
@Composable
private fun AdaptiveHomePreview(width: WindowWidthSizeClass) {
    FarmAppTheme {
        FarmAppAdaptiveNavigation(
            widthSizeClass = width,
            selectedDestination = FarmAppDestinations.HOME,
            onNavigateHome = {},
            onNavigateCatalog = {},
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreen(
                email = "ejemplo@farmapp.test",
                onSignOut = {},
                onOpenCatalog = {}
            )
        }
    }
}

@Preview(name = "Cotejo 17 - Celular compacto", widthDp = 380, heightDp = 700)
@Composable
private fun FarmAppCompactPreview() = AdaptiveHomePreview(WindowWidthSizeClass.Compact)

@Preview(name = "Cotejo 17 - Pantalla mediana", widthDp = 700, heightDp = 700)
@Composable
private fun FarmAppMediumPreview() = AdaptiveHomePreview(WindowWidthSizeClass.Medium)

@Preview(name = "Cotejo 17 - Tablet expandida", widthDp = 1000, heightDp = 700)
@Composable
private fun FarmAppExpandedPreview() = AdaptiveHomePreview(WindowWidthSizeClass.Expanded)
