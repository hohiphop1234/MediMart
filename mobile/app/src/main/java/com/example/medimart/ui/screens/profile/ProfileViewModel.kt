package com.example.medimart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.User
import com.example.medimart.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            userRepository.getProfile().fold(
                onSuccess = { _user.value = it },
                onFailure = { _error.value = it.message ?: "Không tải được thông tin tài khoản" }
            )
            _isLoading.value = false
        }
    }
}
