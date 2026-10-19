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
import com.example.farmapp.cart.CartViewModel
import com.example.farmapp.catalog.CatalogProductViewModel
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.checkout.CheckoutViewModel
import com.example.farmapp.data.preferences.GalleryLayoutPreference
import com.example.farmapp.profile.ProfileRoute
import com.example.farmapp.ui.account.FarmAppAccountScreen
import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.background.InventoryReportRoute
import com.example.farmapp.ui.cart.FarmAppCartScreen
import com.example.farmapp.ui.checkout.FarmAppOrderDetailScreen
import com.example.farmapp.ui.checkout.FarmAppPickupStoreScreen
import com.example.farmapp.ui.catalog.FarmAppCategoriesScreen
import com.example.farmapp.ui.catalog.RemoteProductGalleryRoute
import com.example.farmapp.ui.home.FarmAppStoreHomeScreen
import com.example.farmapp.ui.legacy.LegacyInventoryActivity
import com.example.farmapp.ui.room.RoomDemoDetailRoute
import com.example.farmapp.ui.room.RoomDemoEditRoute
import com.example.farmapp.ui.room.RoomDemoRoute
import com.example.farmapp.ui.scanner.FarmAppBarcodeScannerScreen
import com.example.farmapp.ui.sql.SqlLabRoute

/**
 * HU04 - Paso 8: carrito, detalle de pedido y seleccion de botica.
 * Conserva HU01, HU02, HU03, escaner y los laboratorios educativos.
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

    // HU04: un solo ViewModel para toda la navegacion.
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()

    // HU04: estado del borrador de compra compartido entre rutas.
    val checkoutViewModel: CheckoutViewModel = viewModel()
    val orderUiState by checkoutViewModel.uiState.collectAsStateWithLifecycle()

    // El borrador no debe conservarse al cerrar la sesion.
    LaunchedEffect(uiState.isAuthenticated) {
        if (!uiState.isAuthenticated) checkoutViewModel.resetCheckout()
    }

    val share: (DemoProduct) -> Unit = onShareProduct ?: { product ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$shareLabel: ${product.name} (${product.sku})")
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    // HU01: conservar transiciones seguras entre sesion y autenticacion.
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

        // HU03/HU04: Inicio comercial y boton Agregar al carrito.
        composable(FarmAppDestinations.HOME) {
            if (uiState.isAuthenticated) {
                val catalogViewModel: CatalogProductViewModel =
                    viewModel(factory = CatalogProductViewModel.Factory)
                val catalogUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.HOME,
                    onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                    onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATEGORIES) },
                    onNavigateAccount = { navigateTopLevel(FarmAppDestinations.ACCOUNT) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    FarmAppStoreHomeScreen(
                        uiState = catalogUiState,
                        onQueryChanged = catalogViewModel::onQueryChanged,
                        onOpenAddress = {
                            Toast.makeText(
                                context,
                                "La selección de dirección estará disponible próximamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenDelivery = {
                            Toast.makeText(
                                context,
                                "Delivery estará disponible próximamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onOpenCart = {
                            navController.navigate(FarmAppDestinations.CART) {
                                launchSingleTop = true
                            }
                        },
                        onAddToCart = { product ->
                            cartViewModel.addProduct(sku = product.sku, quantity = 1)
                        },
                        cartItemCount = cartUiState.totalUnits
                    )
                }
            }
        }

        // HU03/HU04: Categorias con filtros, escaner y carrito.
        composable(FarmAppDestinations.CATEGORIES) { categoryEntry ->
            if (uiState.isAuthenticated) {
                val catalogViewModel: CatalogProductViewModel =
                    viewModel(factory = CatalogProductViewModel.Factory)
                val categoriesUiState by catalogViewModel.uiState.collectAsStateWithLifecycle()

                val scannedBarcode by categoryEntry.savedStateHandle
                    .getStateFlow<String?>("scanned_barcode", null)
                    .collectAsStateWithLifecycle()

                LaunchedEffect(scannedBarcode) {
                    scannedBarcode?.let { barcode ->
                        catalogViewModel.onCategorySelected(null)
                        catalogViewModel.onQueryChanged(barcode)
                        categoryEntry.savedStateHandle["scanned_barcode"] = null
                    }
                }

                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATEGORIES,
                    onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                    onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATEGORIES) },
                    onNavigateAccount = { navigateTopLevel(FarmAppDestinations.ACCOUNT) },
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
                        },
                        onAddToCart = { product ->
                            cartViewModel.addProduct(sku = product.sku, quantity = 1)
                        },
                        onOpenCart = {
                            navController.navigate(FarmAppDestinations.CART) {
                                launchSingleTop = true
                            }
                        },
                        cartItemCount = cartUiState.totalUnits
                    )
                }
            }
        }

        // HU03: Cuenta, Perfil y herramientas anteriores.
        composable(FarmAppDestinations.ACCOUNT) {
            if (uiState.isAuthenticated) {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.ACCOUNT,
                    onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                    onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATEGORIES) },
                    onNavigateAccount = { navigateTopLevel(FarmAppDestinations.ACCOUNT) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    FarmAppAccountScreen(
                        email = uiState.userEmail
                            ?.takeIf(String::isNotBlank)
                            ?: stringResource(R.string.home_email_unknown),
                        onOpenProfile = {
                            navController.navigate(FarmAppDestinations.PROFILE) {
                                launchSingleTop = true
                            }
                        },
                        onOpenOrders = {
                            Toast.makeText(context, "Mis pedidos: próximamente", Toast.LENGTH_SHORT)
                                .show()
                        },
                        onOpenFavorites = {
                            Toast.makeText(context, "Mis favoritos: próximamente", Toast.LENGTH_SHORT)
                                .show()
                        },
                        onOpenAddresses = {
                            Toast.makeText(context, "Mis direcciones: próximamente", Toast.LENGTH_SHORT)
                                .show()
                        },
                        onSignOut = onSignOut,
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

        // HU04: Mi carrito, cantidades y resumen desplegable.
        composable(FarmAppDestinations.CART) {
            if (uiState.isAuthenticated) {
                FarmAppCartScreen(
                    uiState = cartUiState,
                    onBack = { navController.popBackStack() },
                    onContinueShopping = {
                        navigateTopLevel(FarmAppDestinations.HOME)
                    },
                    onOpenFavorites = {
                        Toast.makeText(context, "Mis favoritos: próximamente", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onIncreaseQuantity = { item ->
                        if (item.quantity < Int.MAX_VALUE) {
                            cartViewModel.changeQuantity(item.sku, item.quantity + 1)
                        }
                    },
                    onDecreaseQuantity = { item ->
                        cartViewModel.changeQuantity(item.sku, item.quantity - 1)
                    },
                    onRemoveProduct = { item ->
                        cartViewModel.removeProduct(item.sku)
                    },
                    onCheckout = {
                        navController.navigate(FarmAppDestinations.ORDER_DETAIL) {
                            launchSingleTop = true
                        }
                    },
                    onDismissMessage = { cartViewModel.clearMessage() }
                )
            }
        }

        // HU04: detalle del pedido (checkout de demostracion, sin cobrar).
        composable(FarmAppDestinations.ORDER_DETAIL) {
            if (uiState.isAuthenticated) {
                FarmAppOrderDetailScreen(
                    cartUiState = cartUiState,
                    orderUiState = orderUiState,
                    onBack = { navController.popBackStack() },
                    onDeliveryMethodSelected = { method ->
                        checkoutViewModel.selectDeliveryMethod(method)
                    },
                    onOpenAddress = {
                        Toast.makeText(
                            context,
                            "Agregar dirección: próximamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onChoosePickupStore = {
                        navController.navigate(FarmAppDestinations.PICKUP_STORE) {
                            launchSingleTop = true
                        }
                    },
                    onPickupRecipientSelected = { recipient ->
                        checkoutViewModel.selectPickupRecipient(recipient)
                    },
                    onOtherRecipientNameChanged = { name ->
                        checkoutViewModel.setOtherRecipientName(name)
                    },
                    onOpenPaymentMethod = {
                        Toast.makeText(
                            context,
                            "Métodos de pago: próximamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onOpenReceipt = {
                        Toast.makeText(
                            context,
                            "Comprobantes de pago: próximamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onOpenCoupon = {
                        Toast.makeText(
                            context,
                            "Cupones de descuento: próximamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onConfirmOrder = {
                        // En el paso 7 no se generan pedidos ni se cobran tarjetas.
                        Toast.makeText(
                            context,
                            "La confirmación estará disponible al completar el checkout.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }

        // HU04: seleccionar una botica de demostracion para recoger el pedido.
        composable(FarmAppDestinations.PICKUP_STORE) {
            if (uiState.isAuthenticated) {
                FarmAppPickupStoreScreen(
                    selectedStoreId = orderUiState.pickupStore?.id,
                    onBack = { navController.popBackStack() },
                    onStoreSelected = { store ->
                        // Actualiza el borrador compartido antes de volver al pedido.
                        checkoutViewModel.setPickupStore(store)
                        navController.popBackStack()
                    }
                )
            }
        }

        // HU03: CameraX + ML Kit; entrega el codigo a Categorias.
        composable(FarmAppDestinations.BARCODE_SCANNER) {
            if (uiState.isAuthenticated) {
                FarmAppBarcodeScannerScreen(
                    onBack = { navController.popBackStack() },
                    onBarcodeScanned = { barcode ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle?.set("scanned_barcode", barcode)
                        Toast.makeText(
                            context, "Código detectado: $barcode", Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                )
            }
        }

        // HU02: Mi perfil.
        composable(FarmAppDestinations.PROFILE) {
            if (uiState.isAuthenticated) {
                ProfileRoute(onBack = { navController.popBackStack() })
            }
        }

        // HU01-HU03: catalogo educativo original.
        composable(FarmAppDestinations.CATALOG) {
            if (uiState.isAuthenticated) {
                FarmAppAdaptiveNavigation(
                    widthSizeClass = windowWidthSizeClass,
                    selectedDestination = FarmAppDestinations.CATALOG,
                    onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                    onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATEGORIES) },
                    onNavigateAccount = { navigateTopLevel(FarmAppDestinations.ACCOUNT) },
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

        // Informes de segundo plano.
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

        // Detalle del laboratorio Room.
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

        // Edicion del laboratorio Room.
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

        // Galeria remota educativa.
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
                    onNavigateHome = { navigateTopLevel(FarmAppDestinations.HOME) },
                    onNavigateCatalog = { navigateTopLevel(FarmAppDestinations.CATEGORIES) },
                    onNavigateAccount = { navigateTopLevel(FarmAppDestinations.ACCOUNT) },
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