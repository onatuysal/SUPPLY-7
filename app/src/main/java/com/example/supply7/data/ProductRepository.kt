package com.example.supply7.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = productsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java).filter { it.stock > 0 }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProducts(userId: String): Result<List<Product>> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("sellerId", userId)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
                .sortedByDescending { it.timestamp }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String = ""): Result<List<Product>> {
        return try {
            // Simple query: get all products ordered by timestamp
            val snapshot = productsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(500)
                .get()
                .await()
            
            var products = snapshot.toObjects(Product::class.java)

            // Filter by stock and text search only
            products = products.filter { product ->
                // Only show in-stock items
                if (product.stock <= 0) return@filter false
                
                // Text search
                if (query.isNotBlank()) {
                    val q = query.lowercase()
                    product.title.lowercase().contains(q) || 
                    product.description.lowercase().contains(q) ||
                    product.category.lowercase().contains(q)
                } else {
                    true
                }
            }

            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Placeholder for adding product later
    suspend fun addProduct(product: Product): Result<Boolean> {
        return try {
            productsCollection.add(product).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
