package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun getFavoritesCollection(uid: String) = db.collection("users").document(uid).collection("favorites")

    suspend fun toggleFavorite(productId: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val docRef = getFavoritesCollection(uid).document(productId)
        
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.delete().await()
                Result.success(false) // Removed
            } else {
                val favorite = Favorite(
                    userId = uid,
                    productId = productId,
                    timestamp = System.currentTimeMillis()
                )
                docRef.set(favorite).await()
                Result.success(true) // Added
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteProductIds(): Result<List<String>> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = getFavoritesCollection(uid).get().await()
            val ids = snapshot.documents.map { it.id }
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
