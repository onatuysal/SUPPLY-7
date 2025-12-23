package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class CartItem(
    @DocumentId
    val id: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val sellerId: String = ""
)
