package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Order(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING", // PENDING, COMPLETED, CANCELLED
    val timestamp: Long = System.currentTimeMillis()
)
