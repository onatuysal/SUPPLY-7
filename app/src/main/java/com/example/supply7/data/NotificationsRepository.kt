package com.example.supply7.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")

    suspend fun getNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val notifications = snapshot.toObjects(Notification::class.java)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNotification(notification: Notification): Result<Boolean> {
        return try {
            notificationsCollection.add(notification).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId).update("isRead", true).await()
        } catch (e: Exception) {
            // Log error
        }
    }
}
