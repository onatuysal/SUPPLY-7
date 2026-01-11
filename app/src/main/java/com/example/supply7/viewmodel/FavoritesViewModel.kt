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

    // ARTIK GEREK YOK: setAllProducts

    fun loadFavorites() {
        viewModelScope.launch {
            val result = repo.getFavoriteProductIds()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val set = list.toSet()
                _favoriteIds.value = set
                
                // Fetch REAL product details from server
                val productsResult = repo.getProductsByIds(list)
                _favorites.value = productsResult.getOrNull() ?: emptyList()
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
            // 1) Önce LOKAL olarak güncelle (Sadece ID seti) -> Kalp rengi anında değişsin
            val current = _favoriteIds.value ?: emptySet()
            val newSet = if (current.contains(product.id)) {
                current - product.id
            } else {
                current + product.id
            }
            _favoriteIds.value = newSet
            
            // UI listesini de güncelle (Product objesi elimizde var, server'a gitmeye gerek yok ekleme anında)
            val currentList = _favorites.value ?: emptyList()
            val newList = if (current.contains(product.id)) {
                 currentList.filter { it.id != product.id }
            } else {
                 currentList + product
            }
            _favorites.value = newList

            // 2) Firestore’a yaz
            repo.toggleFavorite(product.id)
            
            // Opsiyonel: Yazma bitince garanti olsun diye refresh çağrılabilir ama gerek yok
        }
    }
}








