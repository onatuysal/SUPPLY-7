package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.FavoritesRepository
import com.example.supply7.data.Product
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private val repo = FavoritesRepository()

    // Sadece ID set’i – kalpler bununla boyanıyor
    private val _favoriteIds = MutableLiveData<Set<String>>(emptySet())
    val favoriteIds: LiveData<Set<String>> = _favoriteIds

    // FavoritesFragment’te gösterilecek ürünler
    private val _favorites = MutableLiveData<List<Product>>(emptyList())
    val favorites: LiveData<List<Product>> = _favorites

    // Home’dan gelen tüm ürünler
    private var allProducts: List<Product> = emptyList()

    init {
        // VM yaratılınca Firestore’dan mevcut favorileri çek
        loadFavorites()
    }

    fun setAllProducts(products: List<Product>) {
        allProducts = products
        val ids = _favoriteIds.value ?: emptySet()
        _favorites.value = products.filter { ids.contains(it.id) }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val result = repo.getFavoriteProductIds()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val set = list.toSet()
                _favoriteIds.value = set
                _favorites.value = allProducts.filter { set.contains(it.id) }
            } else {
                result.exceptionOrNull()?.printStackTrace()
                _favoriteIds.value = emptySet()
                _favorites.value = emptyList()
            }
        }
    }

    /** Home’daki kalbe tıklanınca burası çağrılıyor */
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            // 1) Önce LOKAL olarak güncelle → UI ANINDA tepki versin
            val current = _favoriteIds.value ?: emptySet()
            val newSet = if (current.contains(product.id)) {
                current - product.id
            } else {
                current + product.id
            }
            _favoriteIds.value = newSet
            _favorites.value = allProducts.filter { newSet.contains(it.id) }

            // 2) Firestore’a “best effort” yaz (başarısız olsa bile UI bozulmasın)
            val result = repo.toggleFavorite(product.id)
            if (result.isFailure) {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }
}








