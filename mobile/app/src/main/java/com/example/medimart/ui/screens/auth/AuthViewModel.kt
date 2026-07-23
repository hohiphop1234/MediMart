package com.example.medimart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _otpSuccess = MutableStateFlow(false)
    val otpSuccess = _otpSuccess.asStateFlow()

    fun login(email: String) {
        if (!email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            _error.value = "Email không hợp lệ"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.login(email).fold(
                onSuccess = { _loginSuccess.value = true },
                onFailure = { _error.value = it.message ?: "Không thể gửi mã xác thực" }
            )
            _isLoading.value = false
        }
    }

    fun verifyOtp(email: String, otp: String) {
        if (otp.length != 6) {
            _error.value = "Mã OTP phải gồm 6 số"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.verifyOtp(email, otp).fold(
                onSuccess = { _otpSuccess.value = true },
                onFailure = { _error.value = it.message ?: "Mã OTP không đúng hoặc đã hết hạn" }
            )
            _isLoading.value = false
        }
    }

    suspend fun restoreSession(): Boolean {
        return authRepository.restoreSession()
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun consumeLoginSuccess() {
        _loginSuccess.value = false
    }

    fun consumeOtpSuccess() {
        _otpSuccess.value = false
    }
}
