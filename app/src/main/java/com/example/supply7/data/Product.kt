package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

@kotlinx.parcelize.Parcelize
data class Product(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val faculty: String = "",
    val category: String = "",
    val brand: String = "",
    val color: String = "",
    val condition: String = "",
    val department: String = "",
    val type: String = "listing",
    val timestamp: Long = System.currentTimeMillis()
) : android.os.Parcelable
