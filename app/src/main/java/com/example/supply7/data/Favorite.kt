package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Favorite(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
