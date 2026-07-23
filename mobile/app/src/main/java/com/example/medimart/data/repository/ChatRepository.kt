package com.example.medimart.data.repository

import com.example.medimart.data.model.ChatRequest
import com.example.medimart.data.remote.ApiService
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun streamChat(message: String): ResponseBody {
        return apiService.streamChat(ChatRequest(message = message, stream = true))
    }
}
