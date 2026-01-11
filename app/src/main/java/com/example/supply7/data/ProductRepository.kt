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
            // Start with base collection
            var firestoreQuery: Query = productsCollection

            // 1. Apply Equality Filters First (Firestore Best Practice)
            if (category != null && category.isNotBlank()) {
                firestoreQuery = firestoreQuery.whereEqualTo("category", category)
            }
            
            if (condition != null && condition.isNotBlank()) {
                 // Note: Ensure UI sends exact string like "New" or "Used-Good"
                 firestoreQuery = firestoreQuery.whereEqualTo("condition", condition)
            }

            // 2. Apply Range Filters (Price)
            if (minPrice != null) {
                firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("price", minPrice)
            }
            if (maxPrice != null) {
                firestoreQuery = firestoreQuery.whereLessThanOrEqualTo("price", maxPrice)
            }

            // 3. Order By
            // Firestore restriction: If filtering by range, must order by that field first.
            if (minPrice != null || maxPrice != null) {
                val direction = if (sortDescending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
                firestoreQuery = firestoreQuery.orderBy("price", direction)
            } else {
                // Default ordering
                firestoreQuery = firestoreQuery.orderBy("timestamp", Query.Direction.DESCENDING)
            }

            // 4. Limit (Increased to 500 for better "Search" capability since text is local)
            firestoreQuery = firestoreQuery.limit(500)

            val snapshot = firestoreQuery.get().await()
            var products = snapshot.toObjects(Product::class.java)

            // 5. Text Search (Still Client-Side)
            if (query.isNotBlank()) {
                val q = query.lowercase()
                products = products.filter {
                    it.title.lowercase().contains(q) || 
                    it.description.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
                }
            }

            // Note: If multiple filters are applied (e.g. Category + Price), 
            // Firestore might require a composite index. The app will throw an error with a link to create it.
            // This is expected behavior for robust querying.

            Result.success(products)
        } catch (e: Exception) {
            // Use fallback if index is missing to prevent crash? 
            // For now, returning failure so dev sees index link in logs.
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
