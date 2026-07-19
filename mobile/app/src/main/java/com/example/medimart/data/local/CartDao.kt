package com.example.medimart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: CartEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    fun updateQuantity(productId: String, quantity: Int): Int

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    fun deleteItem(productId: String): Int

    @Query("DELETE FROM cart_items")
    fun clearCart(): Int
}
