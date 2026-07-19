package com.example.medimart.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.example.medimart.ui.screens.category.CategoryScreen
import com.example.medimart.ui.screens.category.CategoryViewModel
import com.example.medimart.ui.screens.home.HomeScreen
import com.example.medimart.ui.screens.home.HomeViewModel
import com.example.medimart.ui.screens.profile.ProfileScreen
import com.example.medimart.ui.screens.profile.ProfileViewModel
import com.example.medimart.ui.screens.checkout.CheckoutScreen
import com.example.medimart.ui.screens.checkout.CheckoutViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    userRepository: UserRepository,
    orderRepository: OrderRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"
    val scope = rememberCoroutineScope()

    val authViewModel = remember { AuthViewModel(authRepository) }
    val homeViewModel = remember { HomeViewModel(productRepository) }
    val cartViewModel = remember { CartViewModel(cartRepository) }
    val categoryViewModel = remember { CategoryViewModel(productRepository) }
    val profileViewModel = remember { ProfileViewModel(userRepository) }
    val checkoutViewModel = remember { CheckoutViewModel(cartRepository, orderRepository, userRepository) }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            "profile" -> profileViewModel.loadProfile()
            "checkout" -> checkoutViewModel.loadAddresses()
        }
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartBadgeCount = cartItems.sumOf { it.quantity }

    val isBottomBarVisible = currentRoute in listOf("home", "category", "cart", "profile")

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
                    cartBadgeCount = cartBadgeCount
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
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
                    onProductClick = { /* TODO */ },
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

            composable("category") {
                CategoryScreen(
                    viewModel = categoryViewModel,
                    onCategoryClick = { /* TODO */ }
                )
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
                    onCheckoutSuccess = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    viewModel = profileViewModel,
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
    }
}
