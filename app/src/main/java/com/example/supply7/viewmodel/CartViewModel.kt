package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.CartItem
import com.example.supply7.data.CartRepository
import com.example.supply7.data.Product
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = CartRepository()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice
    
    private val _checkoutStatus = MutableLiveData<Result<Boolean>?>()
    val checkoutStatus: LiveData<Result<Boolean>?> = _checkoutStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadCart() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getCartItems()
            if (result.isSuccess) {
                val items = result.getOrNull() ?: emptyList()
                _cartItems.value = items
                calculateTotal(items)
            }
            _isLoading.value = false
        }
    }

    private fun calculateTotal(items: List<CartItem>) {
        val total = items.sumOf { it.price * it.quantity }
        _totalPrice.value = total
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.addToCart(product)
            // Optionally reload cart or show message
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            val result = repository.removeFromCart(item.id)
            if (result.isSuccess) {
                loadCart() // Refresh
            }
        }
    }

    fun checkout(shippingAddress: com.example.supply7.data.Address) {
        _isLoading.value = true
        val items = _cartItems.value ?: emptyList()
        val total = _totalPrice.value ?: 0.0
        
        if (items.isEmpty()) {
            _checkoutStatus.value = Result.failure(Exception("Cart is empty"))
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            val result = repository.checkout(items, total, shippingAddress)
            _checkoutStatus.value = result
            if (result.isSuccess) {
                _cartItems.value = emptyList() // clear local list immediately
                loadCart() // Sync with server (should be empty)
            }
            _isLoading.value = false
        }
    }
    
    fun resetCheckoutStatus() {
        _checkoutStatus.value = null
    }
}
