package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String, name: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = repository.register(email, password, name)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _user.value = null
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
             _isLoading.value = false
        }
    }
}
