package com.example.medimart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _otpSuccess = MutableStateFlow(false)
    val otpSuccess = _otpSuccess.asStateFlow()

    fun login(phone: String) {
        if (phone.length < 10) {
            _error.value = "Số điện thoại không hợp lệ"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.login(phone).fold(
                onSuccess = { _loginSuccess.value = true },
                onFailure = { _error.value = it.message ?: "Lỗi đăng nhập" }
            )
            _isLoading.value = false
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        if (otp.length != 4) {
            _error.value = "Mã OTP phải gồm 4 số"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.verifyOtp(phone, otp).fold(
                onSuccess = { _otpSuccess.value = true },
                onFailure = { _error.value = it.message ?: "Mã OTP không đúng" }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
