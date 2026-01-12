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

    fun searchProducts(
        query: String = "",
        minPrice: Double? = null,
        maxPrice: Double? = null,
        category: String? = null,
        department: String? = null,
        brand: String? = null,
        city: String? = null,
        condition: String? = null,
        sortDescending: Boolean = true
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.searchProducts(query, minPrice, maxPrice, category, department, brand, city, condition, sortDescending)
            if (result.isSuccess) {
                _products.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
}
