package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Order
import com.example.supply7.data.OrderRepository
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {
    private val repository = OrderRepository()
    
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders
    
    fun loadOrders() {
        viewModelScope.launch {
            val result = repository.getUserOrders()
            if (result.isSuccess) {
                _orders.value = result.getOrNull() ?: emptyList()
            }
        }
    }
}
