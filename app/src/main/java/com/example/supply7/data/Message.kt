package com.example.supply7.data

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Message(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val productId: String = "",
    val type: String = "text", // "text", "offer", "image"
    val offerAmount: String? = null,
    val productTitle: String? = null,
    val status: String = "pending" // "pending", "accepted", "declined"
) : Serializable
