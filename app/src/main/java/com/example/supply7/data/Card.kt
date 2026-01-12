package com.example.supply7.data

data class Card(
    val id: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "", // Last 4 digits or masked
    val expiryDate: String = "",
    val type: String = "mastercard" // or visa
)
