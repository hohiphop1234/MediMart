package com.example.medimart.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource

import com.example.medimart.R
import com.example.medimart.data.remote.ApiService
import com.example.medimart.data.repository.AuthRepository
import com.example.medimart.data.repository.CartRepository
import com.example.medimart.data.repository.ProductRepository
import com.example.medimart.data.repository.UserRepository
import com.example.medimart.data.repository.OrderRepository
import com.example.medimart.ui.components.BottomNavBar
import com.example.medimart.ui.screens.auth.AuthViewModel
import com.example.medimart.ui.screens.auth.LoginScreen
import com.example.medimart.ui.screens.auth.OtpScreen
import com.example.medimart.ui.screens.cart.CartScreen
import com.example.medimart.ui.screens.cart.CartViewModel
import com.example.medimart.ui.screens.store.StoreScreen
import com.example.medimart.ui.screens.store.StoreViewModel
import com.example.medimart.ui.screens.home.HomeScreen
import com.example.medimart.ui.screens.home.HomeViewModel
import com.example.medimart.ui.screens.profile.ProfileScreen
import com.example.medimart.ui.screens.profile.ProfileViewModel
import com.example.medimart.ui.screens.checkout.CheckoutScreen
import com.example.medimart.ui.screens.checkout.CheckoutViewModel
import com.example.medimart.ui.screens.orders.OrderDetailScreen
import com.example.medimart.ui.screens.orders.OrdersScreen
import com.example.medimart.ui.screens.orders.OrdersViewModel
import com.example.medimart.ui.screens.product.ProductDetailScreen
import com.example.medimart.ui.screens.product.ProductDetailViewModel
import com.example.medimart.ui.screens.products.ProductListScreen
import com.example.medimart.ui.screens.products.ProductListViewModel
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.ui.screens.prescription.PrescriptionViewModel
import com.example.medimart.ui.screens.prescription.PrescriptionScreen
import com.example.medimart.ui.screens.chat.ChatViewModel
import com.example.medimart.ui.components.FloatingChatBox
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    userRepository: UserRepository,
    orderRepository: OrderRepository,
    apiService: ApiService
) {
    var initialDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(authRepository) {
        initialDestination = if (authRepository.restoreSession()) "home" else "login"
    }

    if (initialDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MediMartOrange)
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: initialDestination!!
    val scope = rememberCoroutineScope()

    val authViewModel = remember { AuthViewModel(authRepository) }
    val homeViewModel = remember { HomeViewModel(productRepository) }
    val cartViewModel = remember { CartViewModel(cartRepository) }
    val storeViewModel = remember { StoreViewModel() }
    val profileViewModel = remember { ProfileViewModel(userRepository) }
    val checkoutViewModel = remember { CheckoutViewModel(cartRepository, orderRepository, userRepository) }
    val ordersViewModel = remember { OrdersViewModel(orderRepository) }
    val productDetailViewModel = remember { ProductDetailViewModel(productRepository) }
    val chatViewModel = remember { ChatViewModel(apiService) }
    val prescriptionViewModel = remember { PrescriptionViewModel(productRepository) }
    
    var showPrescriptionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            "profile" -> profileViewModel.loadProfile()
            "checkout" -> checkoutViewModel.loadAddresses()
        }
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartBadgeCount = cartItems.sumOf { it.quantity }

    val isBottomBarVisible = currentRoute in listOf("home", "store", "cart", "profile")

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    cartBadgeCount = cartBadgeCount,
                    onPrescriptionClick = { showPrescriptionSheet = true }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialDestination!!,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToOtp = { email -> navController.navigate("otp/${Uri.encode(email)}") }
                )
            }
            
            composable("otp/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                OtpScreen(
                    email = email,
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product._id)}")
                    },
                    onAddToCartClick = { product ->
                        cartViewModel.addToCart(
                            com.example.medimart.data.local.CartEntity(
                                productId = product._id,
                                name = product.name,
                                price = product.salePrice ?: product.price,
                                imageUrl = product.imageUrl,
                                quantity = 1
                            )
                        )
                    },
                    onCategoryClick = { category ->
                        navController.navigate(
                            "products/category/${Uri.encode(category._id)}/${Uri.encode(category.name)}"
                        )
                    },
                    onSearch = { query ->
                        navController.navigate("products/search/${Uri.encode(query)}")
                    }
                )
            }

            composable("product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    viewModel = productDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(
                            com.example.medimart.data.local.CartEntity(
                                productId = product._id,
                                name = product.name,
                                price = product.salePrice ?: product.price,
                                imageUrl = product.imageUrl,
                                quantity = 1
                            )
                        )
                    }
                )
            }

            composable("products/category/{categoryId}/{categoryName}") { backStackEntry ->
                val categoryId = Uri.decode(
                    backStackEntry.arguments?.getString("categoryId").orEmpty()
                )
                val categoryName = Uri.decode(
                    backStackEntry.arguments?.getString("categoryName").orEmpty()
                )
                val productListViewModel = remember(backStackEntry) {
                    ProductListViewModel(productRepository)
                }
                ProductListScreen(
                    title = categoryName.ifBlank { stringResource(R.string.category_products_title) },
                    initialQuery = "",
                    categoryId = categoryId,
                    viewModel = productListViewModel,
                    onBack = { navController.popBackStack() },
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product._id)}")
                    },
                    onAddToCartClick = { product ->
                        cartViewModel.addToCart(
                            com.example.medimart.data.local.CartEntity(
                                productId = product._id,
                                name = product.name,
                                price = product.salePrice ?: product.price,
                                imageUrl = product.imageUrl,
                                quantity = 1
                            )
                        )
                    }
                )
            }

            composable("products/search/{query}") { backStackEntry ->
                val query = Uri.decode(backStackEntry.arguments?.getString("query").orEmpty())
                val productListViewModel = remember(backStackEntry) {
                    ProductListViewModel(productRepository)
                }
                ProductListScreen(
                    title = stringResource(R.string.search_results_title),
                    initialQuery = query,
                    categoryId = null,
                    viewModel = productListViewModel,
                    onBack = { navController.popBackStack() },
                    onProductClick = { product ->
                        navController.navigate("product/${Uri.encode(product._id)}")
                    },
                    onAddToCartClick = { product ->
                        cartViewModel.addToCart(
                            com.example.medimart.data.local.CartEntity(
                                productId = product._id,
                                name = product.name,
                                price = product.salePrice ?: product.price,
                                imageUrl = product.imageUrl,
                                quantity = 1
                            )
                        )
                    }
                )
            }

            composable("store") {
                StoreScreen(viewModel = storeViewModel)
            }
            
            composable("cart") {
                CartScreen(
                    viewModel = cartViewModel,
                    onNavigateToHome = { navController.navigate("home") },
                    onNavigateToCheckout = { navController.navigate("checkout") }
                )
            }
            
            composable("checkout") {
                CheckoutScreen(
                    viewModel = checkoutViewModel,
                    onBack = { navController.popBackStack() },
                    onCheckoutSuccess = { orderId ->
                        navController.navigate("order/${Uri.encode(orderId)}") {
                            popUpTo("cart") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("orders") {
                OrdersScreen(
                    viewModel = ordersViewModel,
                    onBack = { navController.popBackStack() },
                    onOrderClick = { orderId ->
                        navController.navigate("order/${Uri.encode(orderId)}")
                    }
                )
            }

            composable("order/{orderId}") { backStackEntry ->
                val orderId = Uri.decode(backStackEntry.arguments?.getString("orderId").orEmpty())
                OrderDetailScreen(
                    orderId = orderId,
                    viewModel = ordersViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onOrdersClick = { navController.navigate("orders") },
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        
        if (showPrescriptionSheet) {
            PrescriptionScreen(
                viewModel = prescriptionViewModel,
                onProductClick = { product ->
                    navController.navigate("product/${Uri.encode(product._id)}")
                },
                onAddToCartClick = { product ->
                    cartViewModel.addToCart(
                        com.example.medimart.data.local.CartEntity(
                            productId = product._id,
                            name = product.name,
                            price = product.salePrice ?: product.price,
                            imageUrl = product.imageUrl,
                            quantity = 1
                        )
                    )
                },
                onDismiss = { showPrescriptionSheet = false }
            )
        }
    } // end Scaffold
        
    // FloatingChatBox OUTSIDE Scaffold so it's not clipped by bottom bar
    if (currentRoute !in listOf("login", "otp/{email}")) {
        FloatingChatBox(viewModel = chatViewModel)
    }
    } // end Box
}
