package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.FavoritesRepository
import com.example.supply7.data.Product
import com.example.supply7.data.ProductRepository
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val favoritesRepository = FavoritesRepository()
    private val productRepository = ProductRepository()

    private val _favorites = MutableLiveData<List<Product>>()
    val favorites: LiveData<List<Product>> = _favorites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadFavorites() {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Get IDs
            val idsResult = favoritesRepository.getFavoriteProductIds()
            if (idsResult.isSuccess) {
                val ids = idsResult.getOrNull() ?: emptyList()
                if (ids.isNotEmpty()) {
                    // 2. Get Products by IDs
                    // Firestore 'in' query supports up to 10/30 items. For simplicity/robustness, fetch all or batch?
                    // Or reuse search/filter logic? 
                    // Let's implement getProductsByIds in ProductRepository for efficiency or fetch individually.
                    // For MVP, if list is small, fetching one by one or simple query is fine.
                    // Actually, ProductRepository typically fetches all. Let's fetch all and filter client side or implement whereIn.
                    
                    fetchProductsByIds(ids)
                } else {
                    _favorites.value = emptyList()
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchProductsByIds(ids: List<String>) {
        // Needs enhancements in ProductRepository to fetch specific IDs ideally.
        // Or we can hack it: fetch all and filter. Not scalable but works for demo/scratch.
        val result = productRepository.getProducts() // This fetches ordered by timestamp
        if (result.isSuccess) {
            val allProducts = result.getOrNull() ?: emptyList()
            val favProducts = allProducts.filter { ids.contains(it.id) }
            _favorites.value = favProducts
        }
        _isLoading.value = false
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            val result = favoritesRepository.toggleFavorite(product.id)
            if (result.isSuccess) {
                 // Refresh if looking at list
                 // loadFavorites() // Optional depending on UI requirement
            }
        }
    }
}
