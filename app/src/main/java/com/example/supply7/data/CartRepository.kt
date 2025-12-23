package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Cart is stored as a subcollection: users/{uid}/cart
    private fun getCartCollection(uid: String) = db.collection("users").document(uid).collection("cart")
    
    suspend fun addToCart(product: Product): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val cartItem = CartItem(
                productId = product.id,
                productTitle = product.title,
                price = product.price,
                imageUrl = product.imageUrl,
                quantity = 1,
                sellerId = product.sellerId
            )
            // Use productId as document ID to avoid duplicates (increment quantity if complex, overwrite if simple)
            // Use set to overwrite or update
            getCartCollection(uid).document(product.id).set(cartItem).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(cartItemId: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            getCartCollection(uid).document(cartItemId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCartItems(): Result<List<CartItem>> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = getCartCollection(uid).get().await()
            val items = snapshot.toObjects(CartItem::class.java)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkout(items: List<CartItem>, total: Double): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            db.runBatch { batch ->
                // Create Order
                val orderRef = db.collection("orders").document()
                val order = Order(
                    id = orderRef.id,
                    userId = uid,
                    items = items,
                    totalAmount = total,
                    status = "COMPLETED", // Assuming instant success for demo
                    timestamp = System.currentTimeMillis()
                )
                batch.set(orderRef, order)
                
                // Clear Cart
                items.forEach { item ->
                    val itemRef = getCartCollection(uid).document(item.productId) // Using productId as ID as per addToCart
                    // Or if CartItem.id is different, use that.
                    // In addToCart we set doc ID = product.id. 
                    // CartItem.id from Firestore @DocumentId will be the doc ID (product.id).
                    batch.delete(itemRef)
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
