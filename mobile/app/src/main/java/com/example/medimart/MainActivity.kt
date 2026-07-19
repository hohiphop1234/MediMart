package com.example.medimart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.medimart.data.local.AppDatabase
import com.example.medimart.data.local.TokenManager
import com.example.medimart.data.remote.RetrofitClient
import com.example.medimart.data.repository.AuthRepository
import com.example.medimart.data.repository.CartRepository
import com.example.medimart.data.repository.ProductRepository
import com.example.medimart.data.repository.UserRepository
import com.example.medimart.theme.MediMartTheme
import com.example.medimart.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val tokenManager = TokenManager(this)
        val apiService = RetrofitClient.createApiService(tokenManager)
        val appDatabase = AppDatabase.getDatabase(this)

        val authRepository = AuthRepository(apiService, tokenManager)
        val productRepository = ProductRepository(apiService)
        val cartRepository = CartRepository(appDatabase.cartDao())
        val userRepository = UserRepository(apiService)
        val orderRepository = com.example.medimart.data.repository.OrderRepository(apiService)

        setContent {
            MediMartTheme {
                AppNavigation(
                    authRepository = authRepository,
                    productRepository = productRepository,
                    cartRepository = cartRepository,
                    userRepository = userRepository,
                    orderRepository = orderRepository
                )
            }
        }
    }
}
