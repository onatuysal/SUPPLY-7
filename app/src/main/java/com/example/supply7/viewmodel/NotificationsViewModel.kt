package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.AuthRepository
import com.example.supply7.data.Notification
import com.example.supply7.data.NotificationsRepository
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val repository = NotificationsRepository()
    private val authRepository = AuthRepository()

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadNotifications() {
        val currentUser = authRepository.currentUser
        if (currentUser == null) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getNotifications(currentUser.uid)
            if (result.isSuccess) {
                _notifications.value = result.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun markAsRead(notification: Notification) {
        viewModelScope.launch {
            repository.markAsRead(notification.id)
            // Optionally reload or update local list logic
        }
    }
}
