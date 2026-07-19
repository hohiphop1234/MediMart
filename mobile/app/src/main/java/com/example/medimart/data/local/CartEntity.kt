package com.example.medimart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String,
    var quantity: Int
)
