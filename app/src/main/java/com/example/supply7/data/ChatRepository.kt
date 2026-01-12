package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Create or Get Chat
    suspend fun getOrCreateChat(otherUserId: String): String {
        val uid = currentUserId ?: throw Exception("Not logged in")
        
        // Simple logic: check if a chat exists with these two participants
        // Ideally we query collection "chats" where participants array-contains uid
        // This is complex in Firestore (need composite queries or reliable IDs).
        // Simplest: Chat ID = sorted(uid1, uid2).join("_")
        
        val participants = listOf(uid, otherUserId).sorted()
        val chatId = participants.joinToString("_")
        
        val chatRef = db.collection("chats").document(chatId)
        val snapshot = chatRef.get().await()
        
        if (!snapshot.exists()) {
            val chat = Chat(
                id = chatId,
                participants = participants,
                lastMessage = "",
                lastMessageTimestamp = System.currentTimeMillis()
            )
            chatRef.set(chat).await()
        } else {
             // Ensure participants are correct (fix for visibility)
             chatRef.update("participants", participants).await()
        }
        return chatId
    }

    suspend fun sendMessage(
        chatId: String, 
        content: String, 
        receiverId: String, 
        offerAmount: String? = null,
        productTitle: String? = null,
        productId: String? = null,
        productImageUrl: String? = null
    ) {
        val uid = currentUserId ?: return
        val messageRef = db.collection("chats").document(chatId).collection("messages").document()
        
        val type = if (offerAmount != null) "offer" else "text"
        
        val message = Message(
            id = messageRef.id,
            senderId = uid,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = type,
            offerAmount = offerAmount,
            productTitle = productTitle,
            productId = productId ?: "",
            productImageUrl = productImageUrl
        )
        
        db.runBatch { batch ->
            batch.set(messageRef, message)
            batch.update(db.collection("chats").document(chatId), 
                mapOf(
                    "lastMessage" to if (type == "offer") "Offer: $content" else content,
                    "lastMessageTimestamp" to message.timestamp
                )
            )
            
            // Create Notification
            val notificationRef = db.collection("notifications").document()
            val notifType = if (content.startsWith("Offer:")) "OFFER" else "MESSAGE"
            val notification = Notification(
                id = notificationRef.id,
                userId = receiverId,
                type = notifType,
                title = if (notifType == "OFFER") "New Offer Received" else "New Message",
                body = if (notifType == "OFFER") content else "You have a new message.",
                relatedId = chatId,
                timestamp = System.currentTimeMillis()
            )
            batch.set(notificationRef, notification)
            
        }.await()
    }
    
    // For real-time updates, we'd use SnapshotListener in ViewModel
    fun getMessagesQuery(chatId: String): Query {
        return db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }
    
    fun getChatsQuery(): Query {
        val uid = currentUserId ?: throw Exception("Not logged in")
        return db.collection("chats")
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
    }

    suspend fun updateMessageStatus(chatId: String, messageId: String, status: String) {
        val messageRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        messageRef.update("status", status).await()
    }
}
