package com.example.supply7.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Product
import com.example.supply7.data.ProductRepository
import com.example.supply7.data.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val authRepository = AuthRepository()
    private val storage = FirebaseStorage.getInstance()

    private val _uploadStatus = MutableLiveData<Result<Boolean>>()
    val uploadStatus: LiveData<Result<Boolean>> = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun addProduct(
        title: String, 
        description: String, 
        price: Double, 
        faculty: String, 
        category: String,
        department: String,
        brand: String,
        color: String,
        condition: String,
        imageUri: Uri?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser
                if (user == null) {
                    _uploadStatus.value = Result.failure(Exception("User not logged in"))
                    return@launch
                }

                var downloadUrl = ""
                if (imageUri != null) {
                    val ref = storage.reference.child("products/${UUID.randomUUID()}")
                    ref.putFile(imageUri).await()
                    downloadUrl = ref.downloadUrl.await().toString()
                }

                val product = Product(
                    title = title,
                    description = description,
                    price = price,
                    faculty = faculty,
                    category = category,
                    department = department,
                    brand = brand,
                    color = color,
                    condition = condition,
                    imageUrl = downloadUrl,
                    sellerId = user.uid,
                    sellerName = user.email ?: "Unknown",
                    timestamp = System.currentTimeMillis()
                )

                val result = repository.addProduct(product)
                _uploadStatus.value = result
            } catch (e: Exception) {
                _uploadStatus.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
