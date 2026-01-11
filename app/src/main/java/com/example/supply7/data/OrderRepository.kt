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

    suspend fun getUserSales(sellerId: String): Result<List<Order>> {
        return try {
            // MVP Solution: Fetch recent orders and filter. 
            // Scalable Solution: Add 'sellerIds' array field to Order document and use whereArrayContains.
            val snapshot = db.collection("orders")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val allOrders = snapshot.toObjects(Order::class.java)
            
            // Filter: Order contains at least one item sold by me
            val mySales = allOrders.filter { order ->
                order.items.any { it.sellerId == sellerId }
            }
            Result.success(mySales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
