package com.example.farmapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.farmapp.ui.home.HomeScreen
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * HU03 - Navegación principal de FarmApp.
 *
 * Destinos de la barra de navegación:
 *
 * 1. Inicio
 * 2. Categorías
 * 3. Cuenta
 *
 * Conserva navegación adaptable:
 * - Celulares: barra inferior
 * - Pantallas medianas: NavigationRail
 * - Tablets: menú lateral permanente
 *
 * Compatibilidad:
 * - CATEGORIES: nuevo catálogo comercial
 * - CATALOG: catálogo educativo anterior
 *
 * El NavController es administrado por
 * FarmAppNavHost, no por este componente.
 */
@Composable
fun FarmAppAdaptiveNavigation(
    widthSizeClass: WindowWidthSizeClass,
    selectedDestination: String,
    onNavigateHome: () -> Unit,
    onNavigateCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateAccount: () -> Unit = {},
    content: @Composable () -> Unit
) {

    // La pestaña Categorías se considera activa
    // tanto en la nueva pantalla comercial
    // como en el catálogo educativo anterior.
    val isCategoriesSelected =
        selectedDestination == FarmAppDestinations.CATEGORIES ||
                selectedDestination == FarmAppDestinations.CATALOG

    when (navigationTypeFor(widthSizeClass)) {

        // ========================================
        // CELULARES - BARRA INFERIOR
        // ========================================

        FarmAppNavigationType.BOTTOM_BAR -> {

            Scaffold(
                modifier = modifier,

                bottomBar = {

                    NavigationBar(
                        modifier = Modifier.testTag(
                            "adaptive_bottom_bar"
                        )
                    ) {

                        // -------------------------
                        // INICIO
                        // -------------------------

                        NavigationBarItem(
                            selected =
                                selectedDestination ==
                                        FarmAppDestinations.HOME,

                            onClick = onNavigateHome,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.Home
                                )
                            },

                            label = {
                                Text("Inicio")
                            },

                            modifier = Modifier.testTag(
                                "adaptive_go_home"
                            )
                        )

                        // -------------------------
                        // CATEGORÍAS
                        // -------------------------

                        NavigationBarItem(
                            selected = isCategoriesSelected,

                            onClick = onNavigateCatalog,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.GridView
                                )
                            },

                            label = {
                                Text("Categorías")
                            },

                            modifier = Modifier.testTag(
                                "adaptive_go_catalog"
                            )
                        )

                        // -------------------------
                        // CUENTA
                        // -------------------------

                        NavigationBarItem(
                            selected =
                                selectedDestination ==
                                        FarmAppDestinations.ACCOUNT,

                            onClick = onNavigateAccount,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.Person
                                )
                            },

                            label = {
                                Text("Cuenta")
                            },

                            modifier = Modifier.testTag(
                                "adaptive_go_account"
                            )
                        )
                    }
                }
            ) { padding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    content()
                }
            }
        }

        // ========================================
        // PANTALLAS MEDIANAS - BARRA LATERAL
        // ========================================

        FarmAppNavigationType.NAVIGATION_RAIL -> {

            Row(
                modifier = modifier
            ) {

                NavigationRail(
                    modifier = Modifier.testTag(
                        "adaptive_navigation_rail"
                    )
                ) {

                    // INICIO

                    NavigationRailItem(
                        selected =
                            selectedDestination ==
                                    FarmAppDestinations.HOME,

                        onClick = onNavigateHome,

                        icon = {
                            FarmAppTabIcon(
                                Icons.Filled.Home
                            )
                        },

                        label = {
                            Text("Inicio")
                        },

                        modifier = Modifier.testTag(
                            "adaptive_go_home"
                        )
                    )

                    // CATEGORÍAS

                    NavigationRailItem(
                        selected = isCategoriesSelected,

                        onClick = onNavigateCatalog,

                        icon = {
                            FarmAppTabIcon(
                                Icons.Filled.GridView
                            )
                        },

                        label = {
                            Text("Categorías")
                        },

                        modifier = Modifier.testTag(
                            "adaptive_go_catalog"
                        )
                    )

                    // CUENTA

                    NavigationRailItem(
                        selected =
                            selectedDestination ==
                                    FarmAppDestinations.ACCOUNT,

                        onClick = onNavigateAccount,

                        icon = {
                            FarmAppTabIcon(
                                Icons.Filled.Person
                            )
                        },

                        label = {
                            Text("Cuenta")
                        },

                        modifier = Modifier.testTag(
                            "adaptive_go_account"
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }

        // ========================================
        // TABLETS - MENÚ LATERAL PERMANENTE
        // ========================================

        FarmAppNavigationType.PERMANENT_DRAWER -> {

            PermanentNavigationDrawer(
                modifier = modifier,

                drawerContent = {

                    PermanentDrawerSheet(
                        modifier = Modifier
                            .width(240.dp)
                            .testTag(
                                "adaptive_navigation_drawer"
                            )
                    ) {

                        Text(
                            text = "FarmApp",

                            style =
                                MaterialTheme.typography.titleLarge,

                            modifier = Modifier.padding(20.dp)
                        )

                        // -------------------------
                        // INICIO
                        // -------------------------

                        NavigationDrawerItem(
                            selected =
                                selectedDestination ==
                                        FarmAppDestinations.HOME,

                            onClick = onNavigateHome,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.Home
                                )
                            },

                            label = {
                                Text("Inicio")
                            },

                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag(
                                    "adaptive_go_home"
                                )
                        )

                        // -------------------------
                        // CATEGORÍAS
                        // -------------------------

                        NavigationDrawerItem(
                            selected = isCategoriesSelected,

                            onClick = onNavigateCatalog,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.GridView
                                )
                            },

                            label = {
                                Text("Categorías")
                            },

                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag(
                                    "adaptive_go_catalog"
                                )
                        )

                        // -------------------------
                        // CUENTA
                        // -------------------------

                        NavigationDrawerItem(
                            selected =
                                selectedDestination ==
                                        FarmAppDestinations.ACCOUNT,

                            onClick = onNavigateAccount,

                            icon = {
                                FarmAppTabIcon(
                                    Icons.Filled.Person
                                )
                            },

                            label = {
                                Text("Cuenta")
                            },

                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag(
                                    "adaptive_go_account"
                                )
                        )
                    }
                }
            ) {
                content()
            }
        }
    }
}

/**
 * Iconos compartidos para los
 * destinos de navegación.
 */
@Composable
private fun FarmAppTabIcon(
    icon: ImageVector
) {

    Icon(
        imageVector = icon,
        contentDescription = null
    )
}

// ============================================
// PREVIEWS - ANDROID STUDIO
// ============================================

/**
 * Las vistas previas no requieren
 * Firebase ni un NavController.
 */
@Composable
private fun AdaptiveHomePreview(
    width: WindowWidthSizeClass
) {

    FarmAppTheme {

        FarmAppAdaptiveNavigation(
            widthSizeClass = width,

            selectedDestination =
                FarmAppDestinations.HOME,

            onNavigateHome = {},

            onNavigateCatalog = {},

            onNavigateAccount = {},

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

// ============================================
// PREVIEW - CELULAR
// ============================================

@Preview(
    name = "HU03 - Teléfono",
    widthDp = 380,
    heightDp = 700
)
@Composable
private fun FarmAppCompactPreview() {

    AdaptiveHomePreview(
        WindowWidthSizeClass.Compact
    )
}

// ============================================
// PREVIEW - PANTALLA MEDIANA
// ============================================

@Preview(
    name = "HU03 - Pantalla mediana",
    widthDp = 700,
    heightDp = 700
)
@Composable
private fun FarmAppMediumPreview() {

    AdaptiveHomePreview(
        WindowWidthSizeClass.Medium
    )
}

// ============================================
// PREVIEW - TABLET
// ============================================

@Preview(
    name = "HU03 - Tablet",
    widthDp = 1000,
    heightDp = 700
)
@Composable
private fun FarmAppExpandedPreview() {

    AdaptiveHomePreview(
        WindowWidthSizeClass.Expanded
    )
}