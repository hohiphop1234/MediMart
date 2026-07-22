package com.example.medimart

import android.app.Application
import androidx.room.Room
import com.example.medimart.data.local.AppDatabase
import com.example.medimart.data.local.TokenManager
import com.example.medimart.data.remote.RetrofitClient
import com.example.medimart.data.repository.*
import org.osmdroid.config.Configuration

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

        configureMapTiles()
        
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

    private fun configureMapTiles() {
        Configuration.getInstance().apply {
            load(
                this@LongChauApp,
                getSharedPreferences(OSMDROID_PREFERENCES, MODE_PRIVATE)
            )
            // OSM blocks generic library/package User-Agents. Identify this app and
            // provide a contact URL as required by the public tile usage policy.
            userAgentValue =
                "MediMart/${BuildConfig.VERSION_NAME} (Android; +$PROJECT_URL)"
        }
    }

    private companion object {
        const val OSMDROID_PREFERENCES = "osmdroid"
        const val PROJECT_URL = "https://github.com/hohiphop1234/MediMart"
    }
}
