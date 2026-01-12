package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Product
import com.example.supply7.data.ProductRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProducts()
    }

    fun loadProducts() {
        searchProducts("")
    }

    fun searchProducts(query: String = "") {
        _isLoading.value = true
        android.util.Log.d("HomeViewModel", "searchProducts called: query=$query")
        viewModelScope.launch {
            val result = repository.searchProducts(query)
            if (result.isSuccess) {
                _products.value = result.getOrNull() ?: emptyList()
                android.util.Log.d("HomeViewModel", "Products loaded: ${_products.value?.size} items")
            } else {
                _error.value = result.exceptionOrNull()?.message
                android.util.Log.e("HomeViewModel", "Error loading products", result.exceptionOrNull())
            }
            _isLoading.value = false
        }
    }
}
