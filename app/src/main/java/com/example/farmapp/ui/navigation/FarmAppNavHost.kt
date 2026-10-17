package com.example.farmapp.ui.navigation

import android.content.Intent
import com.example.farmapp.ui.legacy.LegacyInventoryActivity
import androidx.compose.runtime.Composable
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.farmapp.data.preferences.GalleryLayoutPreference
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.home.HomeScreen
import com.example.farmapp.ui.catalog.RemoteProductGalleryRoute
import com.example.farmapp.ui.background.InventoryReportRoute
import com.example.farmapp.ui.sql.SqlLabRoute
import com.example.farmapp.ui.room.RoomDemoRoute
import com.example.farmapp.ui.room.RoomDemoDetailRoute
import com.example.farmapp.ui.room.RoomDemoEditRoute

/**
 * Cotejo 16: el NavController pertenece al nivel de navegación, NO a las pantallas.
 * Las pantallas reciben lambdas. El cambio de sesión borra el back stack por privacidad.
 * Acepta un controlador externo para poder verificar el grafo en androidTest.
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
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    galleryLayout: GalleryLayoutPreference = GalleryLayoutPreference.GRID,
    galleryLayoutReady: Boolean = true,
    onGalleryLayoutSelected: (GalleryLayoutPreference) -> Unit = {}
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.nav_share_chooser_title)
    val shareLabel = stringResource(R.string.nav_share_message)
    val share: (DemoProduct) -> Unit = onShareProduct ?: { product ->
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$shareLabel: ${product.name} (${product.sku})")
        }
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }

    // La sesión puede iniciar persistida desde Firebase o cambiar al entrar/salir.
    // Nunca debe poderse regresar a pantallas protegidas tras cerrar sesión.
    // Recordar el punto de entrada evita recrear todo el grafo cuando cambia la sesión.
    val initialDestination = remember {
        if (uiState.isAuthenticated) FarmAppDestinations.HOME else FarmAppDestinations.AUTH
    }
    // Guardamos la última sesión observada: un simple giro de pantalla NO debe
    // devolver al usuario del detalle del producto a Inicio.
    var lastAuthenticated by rememberSaveable { mutableStateOf(uiState.isAuthenticated) }
    LaunchedEffect(uiState.isAuthenticated) {
        if (lastAuthenticated != uiState.isAuthenticated) {
            val target = if (uiState.isAuthenticated) FarmAppDestinations.HOME
                         else FarmAppDestinations.AUTH
            navController.navigate(target) {
                // Vaciar destinos de la sesión, conservando solo el NavGraph.
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }
            lastAuthenticated = uiState.isAuthenticated
        }
    }

    // NAVEGACIÓN DE NIVEL SUPERIOR: elimina detalles obsoletos del historial.
    // Usar lambdas para que las pantallas no dependan de NavController.
    val navigateTopLevel: (String) -> Unit = { destination ->
        if (navController.currentDestination?.route != destination) {
            val returningToCatalog = destination == FarmAppDestinations.CATALOG &&
                navController.currentDestination?.route == FarmAppDestinations.PRODUCT &&
                navController.popBackStack(FarmAppDestinations.CATALOG, inclusive = false)
            if (!returningToCatalog) {
                navController.navigate(destination) {
                    popUpTo(FarmAppDestinations.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialDestination,
        modifier = modifier
    ) {
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
        composable(FarmAppDestinations.HOME) {
            if (uiState.isAuthenticated) FarmAppAdaptiveNavigation(
                widthSizeClass = windowWidthSizeClass,
                selectedDestination = FarmAppDestinations.HOME,
                onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATALOG) },
                modifier = Modifier.fillMaxSize()
            ) {
                HomeScreen(
                    email = uiState.userEmail?.takeIf(String::isNotBlank)
                        ?: stringResource(R.string.home_email_unknown),
                    onSignOut = onSignOut,
                    onOpenCatalog = { navigateTopLevel(FarmAppDestinations.CATALOG) },
                    onOpenSqlLab = { navController.navigate(FarmAppDestinations.SQL_LAB) },
                    onOpenRoomDemo = { navController.navigate(FarmAppDestinations.ROOM_DEMO) },
                    onOpenBackgroundReport = { navController.navigate(FarmAppDestinations.BACKGROUND_REPORT) },
                    onOpenLegacyViews = {
                        context.startActivity(Intent(context, LegacyInventoryActivity::class.java))
                    }
                )
            }
        }
        composable(FarmAppDestinations.CATALOG) {
            if (uiState.isAuthenticated) FarmAppAdaptiveNavigation(
                widthSizeClass = windowWidthSizeClass,
                selectedDestination = FarmAppDestinations.CATALOG,
                onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATALOG) },
                modifier = Modifier.fillMaxSize()
            ) {
                CatalogRouteScreen(
                    onBack = { navController.popBackStack() },
                    onSelectProduct = { product ->
                        navController.navigate(FarmAppDestinations.productRoute(product.sku))
                    },
                    onShareProduct = share,
                    onOpenRemoteGallery = {
                        navController.navigate(FarmAppDestinations.REMOTE_GALLERY)
                    }
                )
            }
        }
        composable(FarmAppDestinations.BACKGROUND_REPORT) {
            if (uiState.isAuthenticated) {
                InventoryReportRoute(onBack = { navController.popBackStack() })
            }
        }
        composable(FarmAppDestinations.SQL_LAB) {
            if (uiState.isAuthenticated) {
                SqlLabRoute(onBack = { navController.popBackStack() })
            }
        }
        composable(FarmAppDestinations.ROOM_DEMO) {
            if (uiState.isAuthenticated) RoomDemoRoute(
                onBack = { navController.popBackStack() },
                onItemDetail = { id -> navController.navigate(FarmAppDestinations.roomDetailRoute(id)) }
            )
        }
        composable(
            route = FarmAppDestinations.ROOM_ITEM_DETAIL,
            arguments = listOf(navArgument(FarmAppDestinations.ROOM_ITEM_ARG) { type = NavType.LongType })
        ) { entry ->
            if (uiState.isAuthenticated) {
                val id = entry.arguments?.getLong(FarmAppDestinations.ROOM_ITEM_ARG) ?: 0L
                RoomDemoDetailRoute(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(FarmAppDestinations.roomEditRoute(id)) }
                )
            }
        }
        composable(
            route = FarmAppDestinations.ROOM_ITEM_EDIT,
            arguments = listOf(navArgument(FarmAppDestinations.ROOM_ITEM_ARG) { type = NavType.LongType })
        ) {
            if (uiState.isAuthenticated) RoomDemoEditRoute(onBack = { navController.popBackStack() })
        }
        composable(FarmAppDestinations.REMOTE_GALLERY) {
            if (uiState.isAuthenticated) {
                RemoteProductGalleryRoute(
                    onBack = { navController.popBackStack() },
                    galleryLayout = galleryLayout,
                    galleryLayoutReady = galleryLayoutReady,
                    onGalleryLayoutSelected = onGalleryLayoutSelected
                )
            }
        }
        composable(
            route = FarmAppDestinations.PRODUCT,
            arguments = listOf(navArgument(FarmAppDestinations.PRODUCT_ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val sku = backStackEntry.arguments?.getString(FarmAppDestinations.PRODUCT_ARG)
            // Nunca recibir ni serializar el producto completo por argumento de ruta.
            val product = DemoCatalogData.repository.all().firstOrNull { it.sku == sku }
            if (uiState.isAuthenticated) FarmAppAdaptiveNavigation(
                widthSizeClass = windowWidthSizeClass,
                selectedDestination = FarmAppDestinations.CATALOG,
                onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATALOG) },
                modifier = Modifier.fillMaxSize()
            ) {
                ProductDetailScreen(
                    product = product,
                    onBack = { navController.popBackStack() },
                    onShare = share
                )
            }
        }
    }
}
