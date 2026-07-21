package com.example.medimart.ui.screens.orders

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderUiTest {
    @Test
    fun orderStatuses_areShownInVietnamese() {
        assertEquals("Chờ xác nhận", orderStatusUi("PENDING").label)
        assertEquals("Đang giao", orderStatusUi("SHIPPING").label)
        assertEquals("Đã giao", orderStatusUi("DELIVERED").label)
        assertEquals("Đã hoàn trả", orderStatusUi("RETURNED").label)
        assertEquals("Đã hủy", orderStatusUi("CANCELLED").label)
    }

    @Test
    fun orderCode_usesReadableSuffix() {
        assertEquals("89ABCDEF", shortOrderCode("12345678-1234-1234-1234-567889abcdef"))
    }

    @Test
    fun codPayment_hasReadableLabel() {
        assertEquals("Thanh toán khi nhận hàng (COD)", paymentMethodLabel("cod"))
    }
}
