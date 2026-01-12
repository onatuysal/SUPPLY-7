package com.example.supply7.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Product
import com.example.supply7.data.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddProductViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // Use default instance from google-services.json
    private val storage = FirebaseStorage.getInstance()
    private val repository = ProductRepository()

    private val _uploadStatus = MutableLiveData<Result<Boolean>>()
    val uploadStatus: LiveData<Result<Boolean>> = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * title, description, price, faculty, category, department, brand, color, condition
     * AddProductFragment'tan geldiği gibi.
     */
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser ?: throw Exception("User not logged in")

                // 1) Upload image to Storage
                val imageUrl = if (imageUri != null) {
                    uploadImageToStorage(imageUri)
                } else {
                    ""
                }

                // 2) Create Product object
                val product = Product(
                    // Leave ID empty if Firestore generates it automatically
                    // Remove if 'id' is not in data class
                    id = "",
                    title = title,
                    description = description,
                    price = price,
                    faculty = faculty,
                    category = category,
                    department = department,
                    brand = brand,
                    color = color,
                    condition = condition,
                    imageUrl = imageUrl,
                    sellerId = user.uid,
                    sellerName = user.displayName ?: (user.email ?: "Unknown"),
                    stock = 1, // Default stock
                    timestamp = System.currentTimeMillis()
                )

                // 3) Firestore'a kaydet
                val result = repository.addProduct(product)
                _uploadStatus.value = result
            } catch (e: Exception) {
                _uploadStatus.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Gerçek işi yapan kısım burası:
     * - product_images/ klasörüne atıyor
     * - sonra aynı referanstan downloadUrl alıyor
     * Böylece "Object does not exist at location" hatası kalkmalı.
     */
    private suspend fun uploadImageToStorage(imageUri: Uri): String {
        val fileName = "product_${java.util.UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("product_images/$fileName")

        // 1. Explicitly wait for upload to complete
        try {
            val uploadTask = ref.putFile(imageUri)
            val snapshot = uploadTask.await()
            
             if (snapshot.totalByteCount <= 0) {
                 throw Exception("Upload failed: 0 bytes transferred.")
             }
        } catch (e: Exception) {
            val bucketInfo = "Bucket: ${storage.reference.bucket}, AppBucket: ${storage.app.options.storageBucket}"
            throw Exception("Step 1 (Upload) failed: ${e.message} [$bucketInfo]")
        }

        // 2. WAIT for backend consistency (common fix for 'Object not found' immediately after put)
        kotlinx.coroutines.delay(2000)

        // 3. Now Fetch URL
        return try {
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
             // If download fails, try to return a temporary fallback or rethrow with specific context
             throw Exception("Step 2 (Get URL) failed: ${e.message}. Path: ${ref.path}")
        }
    }
}

