package com.example.farmapp.ui.navigation

import android.content.Intent
import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.catalog.CatalogProductViewModel
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.data.preferences.GalleryLayoutPreference
import com.example.farmapp.profile.ProfileRoute
import com.example.farmapp.ui.account.FarmAppAccountScreen
import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.background.InventoryReportRoute
import com.example.farmapp.ui.catalog.FarmAppCategoriesScreen
import com.example.farmapp.ui.catalog.RemoteProductGalleryRoute
import com.example.farmapp.ui.home.FarmAppStoreHomeScreen
import com.example.farmapp.ui.legacy.LegacyInventoryActivity
import com.example.farmapp.ui.room.RoomDemoDetailRoute
import com.example.farmapp.ui.room.RoomDemoEditRoute
import com.example.farmapp.ui.room.RoomDemoRoute
import com.example.farmapp.ui.sql.SqlLabRoute
import com.example.farmapp.ui.scanner.FarmAppBarcodeScannerScreen

/**
 * HU03, paso 27: Inicio, Categorias, Cuenta y escaner con busqueda en Room.
 * Se conserva el catalogo educativo en CATALOG, ademas de HU01/HU02
 * y las pantallas de laboratorio existentes.
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
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$shareLabel: ${product.name} (${product.sku})")
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    // HU01: seleccion del destino inicial segun la sesion.
    val initialDestination = remember {
        if (uiState.isAuthenticated) FarmAppDestinations.HOME
        else FarmAppDestinations.AUTH
    }
    var lastAuthenticated by rememberSaveable {
        mutableStateOf(uiState.isAuthenticated)
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (lastAuthenticated != uiState.isAuthenticated) {
            val target = if (uiState.isAuthenticated) {
                FarmAppDestinations.HOME
            } else {
                FarmAppDestinations.AUTH
            }
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }
            lastAuthenticated = uiState.isAuthenticated
        }
    }

    val navigateTopLevel: (String) -> Unit = { destination ->
        if (navController.currentDestination?.route != destination) {
            // Compatibilidad con el regreso del detalle educativo.
            val returningToCatalog =
                destination == FarmAppDestinations.CATALOG &&
                        navController.currentDestination?.route == FarmAppDestinations.PRODUCT &&
                        navController.popBackStack(FarmAppDestinations.CATALOG, false)

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
        // HU01: autenticacion.
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

        // HU03: inicio comercial con productos disponibles de Room.
        composable(FarmAppDestinations.HOME) {
            if (uiState.isAuthenticated) {
                val catalogViewModel: CatalogProductViewModel =
                    viewModel(factory = CatalogProductViewModel.Factory)
                val catalogUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.HOME,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATEGORIES)
                    },
                    onNavigateAccount = {
                        navigateTopLevel(FarmAppDestinations.ACCOUNT)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    FarmAppStoreHomeScreen(
                        uiState = catalogUiState,
                        onQueryChanged = catalogViewModel::onQueryChanged,
                        onOpenAddress = {
                            Toast.makeText(
                                context,
                                "La seleccion de direccion estara disponible proximamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenDelivery = {
                            Toast.makeText(
                                context,
                                "Delivery estara disponible proximamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenCart = {
                            Toast.makeText(
                                context,
                                "El carrito estara disponible proximamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }

        // HU03: categorias comerciales, con busqueda y filtros de Room.
        composable(FarmAppDestinations.CATEGORIES) { categoryEntry ->
            if (uiState.isAuthenticated) {
                val catalogViewModel: CatalogProductViewModel =
                    viewModel(factory = CatalogProductViewModel.Factory)
                val categoriesUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()

                // El escaner deposita el codigo en el SavedStateHandle de Categorias.
                // Recuperamos el evento al volver desde la camara.
                val scannedBarcode by categoryEntry.savedStateHandle
                    .getStateFlow<String?>("scanned_barcode", null)
                    .collectAsStateWithLifecycle()

                LaunchedEffect(scannedBarcode) {
                    scannedBarcode?.let { barcode ->
                        // Eliminar filtros que pudieran ocultar el producto.
                        catalogViewModel.onCategorySelected(null)
                        // El ViewModel ya busca por nombre, SKU y codigo de barras.
                        catalogViewModel.onQueryChanged(barcode)
                        // Consumir el evento para que no se repita en recomposiciones.
                        categoryEntry.savedStateHandle["scanned_barcode"] = null
                    }
                }

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATEGORIES,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATEGORIES)
                    },
                    onNavigateAccount = {
                        navigateTopLevel(FarmAppDestinations.ACCOUNT)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    FarmAppCategoriesScreen(
                        uiState = categoriesUiState,
                        onQueryChanged = catalogViewModel::onQueryChanged,
                        onCategorySelected = catalogViewModel::onCategorySelected,
                        onOpenScanner = {
                            navController.navigate(FarmAppDestinations.BARCODE_SCANNER) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        // HU03: nueva pantalla comercial de Cuenta.
        composable(FarmAppDestinations.ACCOUNT) {
            if (uiState.isAuthenticated) {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.ACCOUNT,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATEGORIES)
                    },
                    onNavigateAccount = {
                        navigateTopLevel(FarmAppDestinations.ACCOUNT)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    FarmAppAccountScreen(
                        email = uiState.userEmail
                            ?.takeIf(String::isNotBlank)
                            ?: stringResource(R.string.home_email_unknown),
                        // HU02: perfil completo y fotografia.
                        onOpenProfile = {
                            navController.navigate(FarmAppDestinations.PROFILE) {
                                launchSingleTop = true
                            }
                        },
                        // Futuras funciones: aun no se almacenan pedidos,
                        // favoritos ni direcciones en el modulo comercial.
                        onOpenOrders = {
                            Toast.makeText(
                                context, "Mis pedidos: proximamente", Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenFavorites = {
                            Toast.makeText(
                                context, "Mis favoritos: proximamente", Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenAddresses = {
                            Toast.makeText(
                                context, "Mis direcciones: proximamente", Toast.LENGTH_SHORT
                            ).show()
                        },
                        // HU01: cierre de sesion existente.
                        onSignOut = onSignOut,
                        // Conservar catalogo educativo y laboratorios.
                        onOpenCatalog = {
                            navController.navigate(FarmAppDestinations.CATALOG)
                        },
                        onOpenSqlLab = {
                            navController.navigate(FarmAppDestinations.SQL_LAB)
                        },
                        onOpenRoomDemo = {
                            navController.navigate(FarmAppDestinations.ROOM_DEMO)
                        },
                        onOpenBackgroundReport = {
                            navController.navigate(FarmAppDestinations.BACKGROUND_REPORT)
                        },
                        onOpenLegacyViews = {
                            context.startActivity(
                                Intent(context, LegacyInventoryActivity::class.java)
                            )
                        }
                    )
                }
            }
        }

        // HU03: escaner de codigos de barras (CameraX + ML Kit).
        composable(FarmAppDestinations.BARCODE_SCANNER) {
            if (uiState.isAuthenticated) {
                FarmAppBarcodeScannerScreen(
                    onBack = { navController.popBackStack() },
                    onBarcodeScanned = { barcode ->
                        // Guardar el resultado en Categorias antes de regresar.
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scanned_barcode", barcode)

                        Toast.makeText(
                            context,
                            "Codigo detectado: $barcode",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.popBackStack()
                    }
                )
            }
        }

        // HU02: perfil.
        composable(FarmAppDestinations.PROFILE) {
            if (uiState.isAuthenticated) {
                ProfileRoute(onBack = { navController.popBackStack() })
            }
        }

        // Catalogo educativo original: se conserva en su propia ruta.
        composable(FarmAppDestinations.CATALOG) {
            if (uiState.isAuthenticated) {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATALOG,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATEGORIES)
                    },
                    onNavigateAccount = {
                        navigateTopLevel(FarmAppDestinations.ACCOUNT)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    CatalogRouteScreen(
                        onBack = { navController.popBackStack() },
                        onSelectProduct = { product ->
                            navController.navigate(
                                FarmAppDestinations.productRoute(product.sku)
                            )
                        },
                        onShareProduct = share,
                        onOpenRemoteGallery = {
                            navController.navigate(FarmAppDestinations.REMOTE_GALLERY)
                        }
                    )
                }
            }
        }

        // Reportes de segundo plano.
        composable(FarmAppDestinations.BACKGROUND_REPORT) {
            if (uiState.isAuthenticated) {
                InventoryReportRoute(onBack = { navController.popBackStack() })
            }
        }

        // Laboratorio SQL.
        composable(FarmAppDestinations.SQL_LAB) {
            if (uiState.isAuthenticated) {
                SqlLabRoute(onBack = { navController.popBackStack() })
            }
        }

        // Laboratorio Room.
        composable(FarmAppDestinations.ROOM_DEMO) {
            if (uiState.isAuthenticated) {
                RoomDemoRoute(
                    onBack = { navController.popBackStack() },
                    onItemDetail = { id ->
                        navController.navigate(FarmAppDestinations.roomDetailRoute(id))
                    }
                )
            }
        }

        // Detalle de un articulo Room.
        composable(
            route = FarmAppDestinations.ROOM_ITEM_DETAIL,
            arguments = listOf(
                navArgument(FarmAppDestinations.ROOM_ITEM_ARG) {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            if (uiState.isAuthenticated) {
                val id = entry.arguments
                    ?.getLong(FarmAppDestinations.ROOM_ITEM_ARG) ?: 0L
                RoomDemoDetailRoute(
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        navController.navigate(FarmAppDestinations.roomEditRoute(id))
                    }
                )
            }
        }

        // Edicion Room.
        composable(
            route = FarmAppDestinations.ROOM_ITEM_EDIT,
            arguments = listOf(
                navArgument(FarmAppDestinations.ROOM_ITEM_ARG) {
                    type = NavType.LongType
                }
            )
        ) {
            if (uiState.isAuthenticated) {
                RoomDemoEditRoute(onBack = { navController.popBackStack() })
            }
        }

        // Galeria remota de demostracion.
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

        // Detalle de producto del catalogo educativo.
        composable(
            route = FarmAppDestinations.PRODUCT,
            arguments = listOf(
                navArgument(FarmAppDestinations.PRODUCT_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val sku = backStackEntry.arguments
                ?.getString(FarmAppDestinations.PRODUCT_ARG)
            val product = DemoCatalogData.repository.all()
                .firstOrNull { it.sku == sku }

            if (uiState.isAuthenticated) {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATALOG,
                    onNavigateHome = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onNavigateCatalog = {
                        navigateTopLevel(FarmAppDestinations.CATEGORIES)
                    },
                    onNavigateAccount = {
                        navigateTopLevel(FarmAppDestinations.ACCOUNT)
                    },
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
}