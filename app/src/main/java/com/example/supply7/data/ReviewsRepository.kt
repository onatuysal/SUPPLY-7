package com.example.supply7.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Reviews collection: users/{uid}/reviews OR global collection "reviews"?
    // Global is easier to query by targetUserId if we don't stash them under subcollections.
    // Or users/{targetUid}/reviews is cleaner for access rules (user can read their own reviews easily).
    // Let's go with users/{targetUid}/reviews.
    
    private fun getReviewsCollection(targetUserId: String) = 
        db.collection("users").document(targetUserId).collection("reviews")

    suspend fun addReview(targetUserId: String, review: Review): Result<Boolean> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        
        // Ensure reviewer info is correct
        val finalReview = review.copy(
            reviewerId = currentUser.uid,
            // reviewerName should be passed or fetched. for now assume passed or we fetch profile.
        )
        
        return try {
            getReviewsCollection(targetUserId).add(finalReview).await()
            // Also need to update User's average rating?
            // Doing that client side for now or cloud function later.
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviews(targetUserId: String): Result<List<Review>> {
        return try {
            val snapshot = getReviewsCollection(targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val reviews = snapshot.toObjects(Review::class.java)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
