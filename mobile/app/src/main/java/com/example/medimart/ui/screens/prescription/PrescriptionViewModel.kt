package com.example.medimart.ui.screens.prescription

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Product
import com.example.medimart.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

private const val TAG = "PrescriptionViewModel"

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun scanPrescription(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _products.value = emptyList()

            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    val bytes = inputStream?.readBytes() ?: throw Exception("Cannot read image")
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", "prescription.jpg", requestFile)
                    productRepository.scanPrescription(body)
                }
                result.onSuccess {
                    _products.value = it
                }.onFailure {
                    Log.e(TAG, "scanPrescription failed", it)
                    _error.value = it.message ?: "Lỗi khi quét đơn thuốc"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in scanPrescription", e)
                _error.value = e.message ?: "Đã xảy ra lỗi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _products.value = emptyList()
        _error.value = null
    }
}
