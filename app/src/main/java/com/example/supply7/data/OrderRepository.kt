package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun getUserOrders(): Result<List<Order>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = db.collection("orders")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val orders = snapshot.toObjects(Order::class.java).sortedByDescending { it.timestamp }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
