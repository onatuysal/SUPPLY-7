package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WalletRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getWalletBalance(): Double {
        val uid = currentUserId ?: return 0.0
        return try {
            val doc = db.collection("users").document(uid).collection("wallet").document("info").get().await()
            doc.getDouble("balance") ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getCards(): List<Card> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid).collection("cards").get().await()
            snapshot.toObjects(Card::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addCard(card: Card) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid).collection("cards").document()
        val newCard = card.copy(id = docRef.id)
        docRef.set(newCard).await()
    }

    suspend fun addBalance(amount: Double) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid).collection("wallet").document("info")
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentBalance = snapshot.getDouble("balance") ?: 0.0
            val newBalance = currentBalance + amount
            transaction.set(docRef, mapOf("balance" to newBalance))
        }.await()
    }
}
