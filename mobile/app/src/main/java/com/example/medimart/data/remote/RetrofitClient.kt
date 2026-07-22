package com.example.medimart.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.example.medimart.data.local.TokenManager
import com.example.medimart.util.Constants
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun createApiService(context: Context, tokenManager: TokenManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Cấu hình Cache: 10MB
        val cacheSize = 10 * 1024 * 1024L
        val cache = Cache(context.cacheDir, cacheSize)

        val authInterceptor = AuthInterceptor(tokenManager)

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .addInterceptor { chain ->
                var request = chain.request()
                // Nếu có mạng, cache trong 1 phút. Nếu không mạng, cho phép dùng cache cũ trong 1 ngày.
                val headerValue = if (isNetworkAvailable(context)) {
                    "public, max-age=60"
                } else {
                    "public, only-if-cached, max-stale=${60 * 60 * 24}"
                }
                request = request.newBuilder()
                    .header("Cache-Control", headerValue)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
