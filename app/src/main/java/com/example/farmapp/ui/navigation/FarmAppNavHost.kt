package com.example.farmapp.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.data.preferences.GalleryLayoutPreference

import com.example.farmapp.profile.ProfileRoute

import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.home.HomeScreen
import com.example.farmapp.ui.catalog.RemoteProductGalleryRoute
import com.example.farmapp.ui.background.InventoryReportRoute
import com.example.farmapp.ui.sql.SqlLabRoute
import com.example.farmapp.ui.room.RoomDemoRoute
import com.example.farmapp.ui.room.RoomDemoDetailRoute
import com.example.farmapp.ui.room.RoomDemoEditRoute
import com.example.farmapp.ui.legacy.LegacyInventoryActivity

/**
 * Controlador principal de navegación de FarmApp.
 *
 * HU01: autenticación y protección de pantallas.
 * HU02: navegación hacia el perfil del usuario.
 *
 * Conserva las rutas implementadas en los 32 cotejos.
 */
@Composable
fun FarmAppNavHost(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onResetPassword: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onSignOut: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    isGooglePickerOpen: Boolean = false,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onShareProduct: ((DemoProduct) -> Unit)? = null,
    windowWidthSizeClass: WindowWidthSizeClass =
        WindowWidthSizeClass.Compact,
    galleryLayout: GalleryLayoutPreference =
        GalleryLayoutPreference.GRID,
    galleryLayoutReady: Boolean = true,
    onGalleryLayoutSelected:
        (GalleryLayoutPreference) -> Unit = {}
) {

    val context = LocalContext.current

    val chooserTitle = stringResource(
        R.string.nav_share_chooser_title
    )

    val shareLabel = stringResource(
        R.string.nav_share_message
    )

    val share: (DemoProduct) -> Unit =
        onShareProduct ?: { product ->

            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"

                    putExtra(
                        Intent.EXTRA_TEXT,
                        "$shareLabel: ${product.name} (${product.sku})"
                    )
                }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    chooserTitle
                )
            )
        }

    // HU01: seleccionar la pantalla inicial
    // según el estado de autenticación.

    val initialDestination = remember {
        if (uiState.isAuthenticated) {
            FarmAppDestinations.HOME
        } else {
            FarmAppDestinations.AUTH
        }
    }

    var lastAuthenticated by rememberSaveable {
        mutableStateOf(uiState.isAuthenticated)
    }

    // Limpiar el historial al iniciar o cerrar sesión.

    LaunchedEffect(uiState.isAuthenticated) {

        if (lastAuthenticated != uiState.isAuthenticated) {

            val target =
                if (uiState.isAuthenticated) {
                    FarmAppDestinations.HOME
                } else {
                    FarmAppDestinations.AUTH
                }

            navController.navigate(target) {

                popUpTo(navController.graph.id) {
                    inclusive = false
                }

                launchSingleTop = true
            }

            lastAuthenticated = uiState.isAuthenticated
        }
    }

    // Navegación entre destinos principales.

    val navigateTopLevel: (String) -> Unit = { destination ->

        if (navController.currentDestination?.route != destination) {

            val returningToCatalog =
                destination == FarmAppDestinations.CATALOG &&
                        navController.currentDestination?.route ==
                        FarmAppDestinations.PRODUCT &&
                        navController.popBackStack(
                            FarmAppDestinations.CATALOG,
                            inclusive = false
                        )

            if (!returningToCatalog) {

                navController.navigate(destination) {

                    popUpTo(FarmAppDestinations.HOME) {
                        inclusive = false
                    }

                    launchSingleTop = true
                }
            }
        }
    }

    // Grafo principal de navegación.

    NavHost(
        navController = navController,
        startDestination = initialDestination,
        modifier = modifier
    ) {

        // ==================================
        // HU01: AUTENTICACIÓN
        // ==================================

        composable(FarmAppDestinations.AUTH) {

            AuthScreen(
                uiState = uiState,
                onSignIn = onSignIn,
                onRegister = onRegister,
                onResetPassword = onResetPassword,
                onDismissMessage = onDismissMessage,
                onGoogleSignIn = onGoogleSignIn,
                isGooglePickerOpen = isGooglePickerOpen
            )
        }

        // ==================================
        // PANTALLA PRINCIPAL
        // ==================================

        composable(FarmAppDestinations.HOME) {

            if (uiState.isAuthenticated) {

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.HOME,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATALOG)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {

                    HomeScreen(
                        email = uiState.userEmail
                            ?.takeIf(String::isNotBlank)
                            ?: stringResource(
                                R.string.home_email_unknown
                            ),

                        onSignOut = onSignOut,

                        onOpenCatalog = {
                            navigateTopLevel(
                                FarmAppDestinations.CATALOG
                            )
                        },

                        // HU02: botón Mi perfil
                        onOpenProfile = {
                            navController.navigate(
                                FarmAppDestinations.PROFILE
                            ) {
                                launchSingleTop = true
                            }
                        },

                        onOpenSqlLab = {
                            navController.navigate(
                                FarmAppDestinations.SQL_LAB
                            )
                        },

                        onOpenRoomDemo = {
                            navController.navigate(
                                FarmAppDestinations.ROOM_DEMO
                            )
                        },

                        onOpenBackgroundReport = {
                            navController.navigate(
                                FarmAppDestinations.BACKGROUND_REPORT
                            )
                        },

                        onOpenLegacyViews = {
                            context.startActivity(
                                Intent(
                                    context,
                                    LegacyInventoryActivity::class.java
                                )
                            )
                        }
                    )
                }
            }
        }

        // ==================================
        // HU02: PERFIL DE USUARIO
        // ==================================

        composable(FarmAppDestinations.PROFILE) {

            if (uiState.isAuthenticated) {

                ProfileRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================================
        // CATÁLOGO
        // ==================================

        composable(FarmAppDestinations.CATALOG) {

            if (uiState.isAuthenticated) {

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATALOG,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATALOG)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {

                    CatalogRouteScreen(
                        onBack = {
                            navController.popBackStack()
                        },

                        onSelectProduct = { product ->
                            navController.navigate(
                                FarmAppDestinations.productRoute(
                                    product.sku
                                )
                            )
                        },

                        onShareProduct = share,

                        onOpenRemoteGallery = {
                            navController.navigate(
                                FarmAppDestinations.REMOTE_GALLERY
                            )
                        }
                    )
                }
            }
        }

        // ==================================
        // TRABAJO EN SEGUNDO PLANO
        // ==================================

        composable(FarmAppDestinations.BACKGROUND_REPORT) {

            if (uiState.isAuthenticated) {

                InventoryReportRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================================
        // LABORATORIO SQL
        // ==================================

        composable(FarmAppDestinations.SQL_LAB) {

            if (uiState.isAuthenticated) {

                SqlLabRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================================
        // DEMOSTRACIÓN ROOM
        // ==================================

        composable(FarmAppDestinations.ROOM_DEMO) {

            if (uiState.isAuthenticated) {

                RoomDemoRoute(
                    onBack = {
                        navController.popBackStack()
                    },

                    onItemDetail = { id ->
                        navController.navigate(
                            FarmAppDestinations.roomDetailRoute(id)
                        )
                    }
                )
            }
        }

        // ==================================
        // DETALLE DE ARTÍCULO ROOM
        // ==================================

        composable(
            route = FarmAppDestinations.ROOM_ITEM_DETAIL,

            arguments = listOf(
                navArgument(
                    FarmAppDestinations.ROOM_ITEM_ARG
                ) {
                    type = NavType.LongType
                }
            )
        ) { entry ->

            if (uiState.isAuthenticated) {

                val id = entry.arguments?.getLong(
                    FarmAppDestinations.ROOM_ITEM_ARG
                ) ?: 0L

                RoomDemoDetailRoute(
                    onBack = {
                        navController.popBackStack()
                    },

                    onEdit = {
                        navController.navigate(
                            FarmAppDestinations.roomEditRoute(id)
                        )
                    }
                )
            }
        }

        // ==================================
        // EDICIÓN DE ARTÍCULO ROOM
        // ==================================

        composable(
            route = FarmAppDestinations.ROOM_ITEM_EDIT,

            arguments = listOf(
                navArgument(
                    FarmAppDestinations.ROOM_ITEM_ARG
                ) {
                    type = NavType.LongType
                }
            )
        ) {

            if (uiState.isAuthenticated) {

                RoomDemoEditRoute(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================================
        // GALERÍA REMOTA
        // ==================================

        composable(FarmAppDestinations.REMOTE_GALLERY) {

            if (uiState.isAuthenticated) {

                RemoteProductGalleryRoute(
                    onBack = {
                        navController.popBackStack()
                    },

                    galleryLayout = galleryLayout,

                    galleryLayoutReady = galleryLayoutReady,

                    onGalleryLayoutSelected =
                        onGalleryLayoutSelected
                )
            }
        }

        // ==================================
        // DETALLE DE PRODUCTO
        // ==================================

        composable(
            route = FarmAppDestinations.PRODUCT,

            arguments = listOf(
                navArgument(
                    FarmAppDestinations.PRODUCT_ARG
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val sku = backStackEntry.arguments?.getString(
                FarmAppDestinations.PRODUCT_ARG
            )

            val product = DemoCatalogData.repository
                .all()
                .firstOrNull {
                    it.sku == sku
                }

            if (uiState.isAuthenticated) {

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATALOG,

                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },

                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATALOG)
                    },

                    modifier = Modifier.fillMaxSize()
                ) {

                    ProductDetailScreen(
                        product = product,

                        onBack = {
                            navController.popBackStack()
                        },

                        onShare = share
                    )
                }
            }
        }
    }
}
