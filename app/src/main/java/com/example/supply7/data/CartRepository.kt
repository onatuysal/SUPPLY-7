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
    
    suspend fun checkout(items: List<CartItem>, address: Address): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            db.runTransaction { transaction ->
                // 1. Validate Stock & Calculate Real Total
                var serverTotal = 0.0
                val serverVerifiedItems = mutableListOf<CartItem>()
                
                // We need to fetch fresh product data for every item in cart
                items.forEach { cartItem ->
                    val productRef = db.collection("products").document(cartItem.productId)
                    val snapshot = transaction.get(productRef)
                    
                    if (!snapshot.exists()) {
                        throw com.google.firebase.firestore.FirebaseFirestoreException(
                            "Product ${cartItem.productTitle} no longer exists",
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                    
                    val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                    val currentPrice = snapshot.getDouble("price") ?: 0.0
                    
                    // Check Stock
                    if (currentStock < cartItem.quantity) {
                         throw com.google.firebase.firestore.FirebaseFirestoreException(
                            "Product ${cartItem.productTitle} is out of stock!",
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                    
                    // Deduct Stock
                    val newStock = currentStock - cartItem.quantity
                    transaction.update(productRef, "stock", newStock)
                    
                    // Accumulate Price (Security: Use server price, not client price)
                    serverTotal += (currentPrice * cartItem.quantity)
                    
                    // Update Item with Server Price for the Receipt
                    serverVerifiedItems.add(cartItem.copy(price = currentPrice))
                }
                
                // 2. Create Order
                val orderRef = db.collection("orders").document()
                val order = Order(
                    id = orderRef.id,
                    userId = uid,
                    items = serverVerifiedItems, // now contains trusted prices
                    totalAmount = serverTotal,
                    status = "CONFIRMED", 
                    shippingAddress = address,
                    timestamp = System.currentTimeMillis()
                )
                transaction.set(orderRef, order)
                
                // 3. Clear Cart
                items.forEach { item ->
                    val itemRef = getCartCollection(uid).document(item.productId)
                    transaction.delete(itemRef)
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
