package com.example.firstapplication.db

import androidx.lifecycle.LiveData

class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: LiveData<List<CardPayment>> = paymentDao.getAllPayments()

    suspend fun insert(payment: CardPayment) {
        paymentDao.insert(payment)
    }
}
