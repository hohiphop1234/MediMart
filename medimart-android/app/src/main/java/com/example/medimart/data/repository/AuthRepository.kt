package com.example.medimart.data.repository

import com.example.medimart.data.local.TokenManager
import com.example.medimart.data.model.LoginRequest
import com.example.medimart.data.model.OtpRequest
import com.example.medimart.data.model.User
import com.example.medimart.data.remote.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email))
            Result.success(response["message"] ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<User> {
        return try {
            val response = apiService.verifyOtp(OtpRequest(email, otp))
            tokenManager.saveToken(response.token)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}
