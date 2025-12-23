package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId
    val id: String = "",
    val participants: List<String> = emptyList(), // [userId1, userId2]
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val otherUserName: String = "", // For display convenience (denormalized)
    val otherUserImage: String = "",
    val unreadCount: Int = 0
)
