package com.example.firstapplication.db

data class CardBillingSummary(
    val cardName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val bankName: String?,
    val billingDay: Int?,
    val cutoffDay: Int?
)