package com.example.medimart

import android.app.Application
import androidx.room.Room
import com.example.medimart.data.local.AppDatabase
import com.example.medimart.data.local.TokenManager
import com.example.medimart.data.remote.RetrofitClient
import com.example.medimart.data.repository.*

class LongChauApp : Application() {
    lateinit var database: AppDatabase
    lateinit var tokenManager: TokenManager
    
    lateinit var apiService: com.example.medimart.data.remote.ApiService
    lateinit var authRepository: AuthRepository
    lateinit var productRepository: ProductRepository
    lateinit var cartRepository: CartRepository
    lateinit var userRepository: UserRepository
    lateinit var orderRepository: OrderRepository

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "medimart.db"
        ).build()
        
        tokenManager = TokenManager(applicationContext)
        apiService = RetrofitClient.createApiService(this, tokenManager)
        
        authRepository = AuthRepository(apiService, tokenManager)
        productRepository = ProductRepository(apiService)
        cartRepository = CartRepository(database.cartDao())
        userRepository = UserRepository(apiService)
        orderRepository = OrderRepository(apiService)
    }
}
