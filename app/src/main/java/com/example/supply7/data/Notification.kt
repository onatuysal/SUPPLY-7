package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId
    val id: String = "",
    val userId: String = "", // Who receives this notification
    val type: String = "MESSAGE", // MESSAGE, OFFER, SYSTEM
    val title: String = "",
    val body: String = "",
    val relatedId: String = "", // Chat ID, Product ID, etc.
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
