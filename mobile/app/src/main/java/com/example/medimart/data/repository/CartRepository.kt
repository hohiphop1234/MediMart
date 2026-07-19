package com.example.medimart.data.repository

import com.example.medimart.data.local.CartDao
import com.example.medimart.data.local.CartEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CartRepository(private val cartDao: CartDao) {
    fun getAllItems(): Flow<List<CartEntity>> = cartDao.getAllItems()

    suspend fun addToCart(item: CartEntity) = withContext(Dispatchers.IO) {
        cartDao.insertItem(item)
    }

    suspend fun updateQuantity(productId: String, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.deleteItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeItem(productId: String) = withContext(Dispatchers.IO) {
        cartDao.deleteItem(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }
}
