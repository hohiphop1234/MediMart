package com.example.medimart.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimart.data.model.Order
import com.example.medimart.data.model.OrderDetail
import com.example.medimart.data.repository.OrderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _listError = MutableStateFlow<String?>(null)
    val listError = _listError.asStateFlow()

    private val _orderDetail = MutableStateFlow<OrderDetail?>(null)
    val orderDetail = _orderDetail.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading = _isDetailLoading.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError = _detailError.asStateFlow()

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling = _isCancelling.asStateFlow()

    private val _cancelError = MutableStateFlow<String?>(null)
    val cancelError = _cancelError.asStateFlow()

    private val _cancelSuccessMessage = MutableStateFlow<String?>(null)
    val cancelSuccessMessage = _cancelSuccessMessage.asStateFlow()

    private var listJob: Job? = null
    private var detailJob: Job? = null

    fun loadOrders(status: String? = _selectedStatus.value) {
        val statusChanged = _selectedStatus.value != status
        _selectedStatus.value = status
        if (statusChanged) {
            _orders.value = emptyList()
        }
        listJob?.cancel()
        listJob = viewModelScope.launch {
            _isLoading.value = true
            _listError.value = null
            orderRepository.getMyOrders(status).fold(
                onSuccess = { _orders.value = it },
                onFailure = {
                    _listError.value = it.message ?: "Không thể tải danh sách đơn hàng"
                }
            )
            _isLoading.value = false
        }
    }

    fun selectStatus(status: String?) {
        if (_selectedStatus.value == status && _orders.value.isNotEmpty()) return
        loadOrders(status)
    }

    fun loadOrderDetail(orderId: String) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _orderDetail.value = null
            _isDetailLoading.value = true
            _detailError.value = null
            _cancelError.value = null
            orderRepository.getMyOrderById(orderId).fold(
                onSuccess = { _orderDetail.value = it },
                onFailure = {
                    _detailError.value = it.message ?: "Không thể tải chi tiết đơn hàng"
                }
            )
            _isDetailLoading.value = false
        }
    }

    fun cancelOrder(orderId: String) {
        if (_isCancelling.value) return

        viewModelScope.launch {
            _isCancelling.value = true
            _cancelError.value = null
            orderRepository.cancelMyOrder(orderId).fold(
                onSuccess = { cancelledOrder ->
                    _orderDetail.value = cancelledOrder
                    _orders.value = if (
                        _selectedStatus.value != null &&
                        _selectedStatus.value != cancelledOrder.status
                    ) {
                        _orders.value.filterNot { it._id == cancelledOrder._id }
                    } else {
                        _orders.value.map { order ->
                            if (order._id == cancelledOrder._id) {
                                order.copy(
                                    status = cancelledOrder.status,
                                    updatedAt = cancelledOrder.updatedAt
                                )
                            } else {
                                order
                            }
                        }
                    }
                    _cancelSuccessMessage.value = "Đơn hàng đã được hủy"
                },
                onFailure = { error ->
                    _cancelError.value = cancelErrorMessage(error)
                }
            )
            _isCancelling.value = false
        }
    }

    fun consumeCancelSuccessMessage() {
        _cancelSuccessMessage.value = null
    }

    fun clearCancelError() {
        _cancelError.value = null
    }

    private fun cancelErrorMessage(error: Throwable): String {
        val httpError = error as? HttpException
        if (httpError?.code() == 409) {
            return "Chỉ có thể hủy đơn đang chờ xác nhận"
        }
        if (httpError?.code() == 404) {
            return "Không tìm thấy đơn hàng"
        }
        return error.message ?: "Không thể hủy đơn hàng"
    }
}
