package com.example.medimart.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.ChatMessage
import com.example.medimart.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

private const val TAG = "ChatViewModel"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
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

        val botMessageId = UUID.randomUUID().toString()
        val initialBotMessage = ChatMessage(
            id = botMessageId,
            text = "",
            isUser = false
        )

        _messages.value = _messages.value + userMessage + initialBotMessage
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responseBody = chatRepository.streamChat(text)
                val reader = responseBody.byteStream().bufferedReader()
                var line: String?

                var accumulatedText = ""
                var isFirstChunk = true

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val dataStr = currentLine.substring(6).trim()
                        if (dataStr == "[DONE]") break

                        val chunkContent = parseChunk(dataStr)
                        if (chunkContent.isNotEmpty()) {
                            accumulatedText += chunkContent

                            if (isFirstChunk) {
                                _isLoading.value = false
                                isFirstChunk = false
                            }

                            val currentAccumulated = accumulatedText

                            withContext(Dispatchers.Main) {
                                _messages.value = _messages.value.map { msg ->
                                    if (msg.id == botMessageId) {
                                        msg.copy(text = currentAccumulated)
                                    } else {
                                        msg
                                    }
                                }
                            }
                        }
                    }
                }

                if (accumulatedText.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == botMessageId) {
                                msg.copy(text = "Xin lỗi, tôi chưa thể trả lời câu hỏi này.")
                            } else {
                                msg
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during chat streaming", e)
                withContext(Dispatchers.Main) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) {
                            msg.copy(text = "Xin lỗi, hiện tại tôi không thể kết nối. Vui lòng thử lại sau.")
                        } else {
                            msg
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseChunk(jsonStr: String): String {
        return try {
            val json = JSONObject(jsonStr)
            val choices = json.optJSONArray("choices")
            val rawContent = if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val delta = choice.optJSONObject("delta")
                if (delta != null && delta.has("content") && !delta.isNull("content")) {
                    delta.optString("content", "")
                } else {
                    val messageObj = choice.optJSONObject("message")
                    if (messageObj != null && messageObj.has("content") && !messageObj.isNull("content")) {
                        messageObj.optString("content", "")
                    } else ""
                }
            } else {
                if (json.has("reply") && !json.isNull("reply")) {
                    json.optString("reply", "")
                } else if (json.has("content") && !json.isNull("content")) {
                    json.optString("content", "")
                } else ""
            }
            if (rawContent.equals("null", ignoreCase = true)) "" else rawContent
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing chunk: $jsonStr", e)
            ""
        }
    }
}
