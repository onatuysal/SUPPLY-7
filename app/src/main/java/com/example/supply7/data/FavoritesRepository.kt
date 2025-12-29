package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Kullanıcı dokümanı; login değilse bile "guest" altında tut
    private fun userDoc() = db
        .collection("favorites")
        .document(auth.currentUser?.uid ?: "guest")

    suspend fun getFavoriteProductIds(): Result<List<String>> {
        return try {
            val snap = userDoc().get().await()
            val list = snap.get("productIds") as? List<String> ?: emptyList()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(productId: String): Result<Unit> {
        return try {
            db.runTransaction { tr ->
                val docRef = userDoc()
                val snap = tr.get(docRef)

                val current =
                    (snap.get("productIds") as? List<String>)?.toMutableList()
                        ?: mutableListOf()

                if (current.contains(productId)) {
                    current.remove(productId)
                } else {
                    current.add(productId)
                }

                tr.set(docRef, mapOf("productIds" to current))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


