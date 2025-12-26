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
    private val firestore = FirebaseFirestore.getInstance()
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

                // 1) Fotoğrafı Storage'a yükle
                val imageUrl = if (imageUri != null) {
                    uploadImageToStorage(imageUri)
                } else {
                    ""
                }

                // 2) Product objesini oluştur
                val product = Product(
                    // id'yi Firestore otomatik verecekse burada boş bırakıyoruz
                    // eğer data class'ta 'id' yoksa bunu sil
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
        val fileName = "product_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("product_images/$fileName")

        // Dosyayı bu ref'e gerçekten yüklüyoruz
        ref.putFile(imageUri).await()

        // Aynı ref'ten downloadUrl çekiyoruz
        return ref.downloadUrl.await().toString()
    }
}

