package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val targetUserId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
