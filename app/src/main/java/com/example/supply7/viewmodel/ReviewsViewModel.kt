package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.AuthRepository
import com.example.supply7.data.Review
import com.example.supply7.data.ReviewsRepository
import kotlinx.coroutines.launch

class ReviewsViewModel : ViewModel() {
    private val repository = ReviewsRepository()
    private val authRepository = AuthRepository()

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    val currentUserId: String?
        get() = authRepository.currentUser?.uid

    fun loadReviews(targetUserId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getReviews(targetUserId)
            if (result.isSuccess) {
                _reviews.value = result.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun submitReview(targetUserId: String, rating: Float, comment: String, reviewerName: String) {
        viewModelScope.launch {
            val review = Review(
                reviewerName = reviewerName, // In real app, fetch from profile
                targetUserId = targetUserId,
                rating = rating,
                comment = comment
            )
            repository.addReview(targetUserId, review)
            // Optionally reload if we are viewing that profile
        }
    }
}
