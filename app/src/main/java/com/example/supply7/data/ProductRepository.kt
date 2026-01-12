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

    suspend fun searchProducts(
        query: String = "",
        minPrice: Double? = null,
        maxPrice: Double? = null,
        category: String? = null,
        department: String? = null,
        brand: String? = null,
        city: String? = null,
        condition: String? = null,
        sortDescending: Boolean = true
    ): Result<List<Product>> {
        return try {
            // Simple query: just get all products ordered by timestamp
            // Then filter client-side to avoid complex Firestore index requirements
            val snapshot = productsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(500)
                .get()
                .await()
            
            var products = snapshot.toObjects(Product::class.java)

            // Client-Side Filtering
            products = products.filter { product ->
                // Stock check
                if (product.stock <= 0) return@filter false
                
                // Text search
                val matchesQuery = if (query.isNotBlank()) {
                    val q = query.lowercase()
                    product.title.lowercase().contains(q) || 
                    product.description.lowercase().contains(q) ||
                    product.category.lowercase().contains(q)
                } else {
                    true
                }
                
                // Category filter
                val matchesCategory = if (category != null && category.isNotBlank()) {
                    product.category.equals(category, ignoreCase = true)
                } else {
                    true
                }
                
                // Department filter
                val matchesDepartment = if (department != null && department.isNotBlank()) {
                    product.department?.equals(department, ignoreCase = true) == true
                } else {
                    true
                }
                
                // Brand filter
                val matchesBrand = if (brand != null && brand.isNotBlank()) {
                    product.brand?.equals(brand, ignoreCase = true) == true
                } else {
                    true
                }
                
                // City filter
                val matchesCity = if (city != null && city.isNotBlank()) {
                    product.city?.equals(city, ignoreCase = true) == true
                } else {
                    true
                }
                
                // Condition filter
                val matchesCondition = if (condition != null && condition.isNotBlank()) {
                    product.condition?.equals(condition, ignoreCase = true) == true
                } else {
                    true
                }
                
                // Price range filter
                val matchesPrice = if (minPrice != null || maxPrice != null) {
                    val min = minPrice ?: 0.0
                    val max = maxPrice ?: Double.MAX_VALUE
                    product.price in min..max
                } else {
                    true
                }
                
                matchesQuery && matchesCategory && matchesDepartment && 
                matchesBrand && matchesCity && matchesCondition && matchesPrice
            }
            
            // Sort by price if price filter is active, otherwise already sorted by timestamp
            if (minPrice != null || maxPrice != null) {
                products = if (sortDescending) {
                    products.sortedByDescending { it.price }
                } else {
                    products.sortedBy { it.price }
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
