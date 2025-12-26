package com.example.supply7.data

import com.google.firebase.firestore.DocumentId

data class Order(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING", // PENDING, CONFIRMED, SHIPPED, DELIVERED
    val shippingAddress: Address = Address(),
    val paymentMethod: String = "Credit Card",
    val timestamp: Long = System.currentTimeMillis()
)

data class Address(
    val fullName: String = "",
    val addressLine: String = "",
    val city: String = "",
    val zipCode: String = "",
    val phoneNumber: String = ""
)
