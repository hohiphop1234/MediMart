package com.example.medimart.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.ChatMessage
import com.example.medimart.data.model.ChatRequest
import com.example.medimart.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(private val apiService: ApiService) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen = _isChatOpen.asStateFlow()

    fun toggleChat() {
        _isChatOpen.value = !_isChatOpen.value
        if (_isChatOpen.value && _messages.value.isEmpty()) {
            _messages.value = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Xin chào! Tôi là trợ lý ảo MediMart. Tôi có thể giúp gì cho bạn?",
                    isUser = false
                )
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isUser = true
        )
        
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.chat(ChatRequest(message = text))
                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.reply,
                    isUser = false
                )
                _messages.value = _messages.value + botMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau.",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
