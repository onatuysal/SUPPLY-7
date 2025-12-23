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
            val products = snapshot.toObjects(Product::class.java)
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

    suspend fun searchProducts(
        query: String = "",
        minPrice: Double? = null,
        maxPrice: Double? = null,
        category: String? = null,
        condition: String? = null,
        sortDescending: Boolean = true
    ): Result<List<Product>> {
        return try {
            // Fetch ALL products ordered by timestamp (newest first)
            // For MVP/Demo scale, fetching all (or limit 100) and filtering client-side is safer
            // than managing complex Firestore indices for every combination of filters.
            val snapshot = productsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100) 
                .get()
                .await()

            var products = snapshot.toObjects(Product::class.java)

            // 1. Text Search (Local)
            if (query.isNotBlank()) {
                val q = query.lowercase()
                products = products.filter {
                    it.title.lowercase().contains(q) || 
                    it.description.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
                }
            }

            // 2. Price Filter
            if (minPrice != null) {
                products = products.filter { it.price >= minPrice }
            }
            if (maxPrice != null) {
                // If maxPrice is at the slider's max (e.g. 1000), treat it as "1000+"? 
                // For now, strict filtering. 
                // If UI sends 1000.0 as max but user wants anything, we might be filtering out >1000.
                // Let's assume UI handles "max+" logic or we just filter strictly.
                products = products.filter { it.price <= maxPrice }
            }

            // 3. Category Filter
            if (category != null && category.isNotBlank()) {
                 products = products.filter { it.category.equals(category, ignoreCase = true) }
            }

            // 4. Condition Filter
            if (condition != null && condition.isNotBlank()) {
                 // Exact match or contains? "New", "Used-Good"
                 products = products.filter { it.condition.equals(condition, ignoreCase = true) }
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
