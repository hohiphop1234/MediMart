package com.example.medimart.data.repository

import android.util.Base64
import com.example.medimart.data.local.TokenManager
import com.example.medimart.data.model.LoginRequest
import com.example.medimart.data.model.OtpRequest
import com.example.medimart.data.model.RefreshRequest
import com.example.medimart.data.model.User
import com.example.medimart.data.remote.ApiService
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

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
            tokenManager.saveSession(response.token, response.refreshToken)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreSession(): Boolean {
        val storedSession = tokenManager.sessionFlow.firstOrNull() ?: return false
        if (isAccessTokenUsable(storedSession.accessToken)) return true

        val refreshToken = storedSession.refreshToken
        if (refreshToken.isNullOrBlank()) {
            tokenManager.clearSession()
            return false
        }

        return try {
            val response = apiService.refreshSession(RefreshRequest(refreshToken))
            tokenManager.saveSession(response.token, response.refreshToken ?: refreshToken)
            true
        } catch (e: HttpException) {
            if (e.code() == 400 || e.code() == 401) {
                tokenManager.clearSession()
                false
            } else {
                true
            }
        } catch (_: IOException) {
            true
        } catch (_: Exception) {
            tokenManager.clearSession()
            false
        }
    }

    suspend fun logout() {
        tokenManager.clearSession()
    }

    private fun isAccessTokenUsable(token: String): Boolean {
        return try {
            val payload = token.split('.').getOrNull(1) ?: return false
            val decoded = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val expiresAt = JSONObject(String(decoded, Charsets.UTF_8)).optLong("exp", 0L)
            expiresAt > System.currentTimeMillis() / 1000L + 60L
        } catch (_: Exception) {
            false
        }
    }
}
