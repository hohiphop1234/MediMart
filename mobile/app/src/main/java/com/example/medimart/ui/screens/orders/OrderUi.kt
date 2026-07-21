package com.example.medimart.ui.screens.orders

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal data class OrderStatusUi(
    val label: String,
    val foreground: Color,
    val background: Color
)

internal fun orderStatusUi(status: String): OrderStatusUi = when (status.uppercase()) {
    "PENDING" -> OrderStatusUi("Chờ xác nhận", Color(0xFFB45309), Color(0xFFFFF3CD))
    "SHIPPING" -> OrderStatusUi("Đang giao", Color(0xFF0369A1), Color(0xFFE0F2FE))
    "DELIVERED" -> OrderStatusUi("Đã giao", Color(0xFF047857), Color(0xFFD1FAE5))
    "RETURNED" -> OrderStatusUi("Đã hoàn trả", Color(0xFF6D28D9), Color(0xFFEDE9FE))
    "CANCELLED" -> OrderStatusUi("Đã hủy", Color(0xFFB91C1C), Color(0xFFFEE2E2))
    else -> OrderStatusUi(status, Color(0xFF475569), Color(0xFFF1F5F9))
}

internal fun formatMoney(value: Int): String =
    "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value)} đ"

internal fun formatOrderDate(value: String): String {
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
        val date = requireNotNull(parser.parse(value))
        SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date)
    }.getOrElse {
        if (value.length < 10) return@getOrElse value
        val parts = value.substring(0, 10).split('-')
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else value
    }
}

internal fun shortOrderCode(orderId: String): String = orderId.takeLast(8).uppercase()

internal fun paymentMethodLabel(method: String): String = when (method.uppercase()) {
    "COD" -> "Thanh toán khi nhận hàng (COD)"
    else -> method
}
