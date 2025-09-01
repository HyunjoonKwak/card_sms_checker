package com.example.firstapplication.db

import androidx.lifecycle.LiveData

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val cardBillingCycleDao: CardBillingCycleDao
) {

    val allPayments: LiveData<List<CardPayment>> = paymentDao.getAllPayments()
    val allBillingCycles: LiveData<List<CardBillingCycle>> = cardBillingCycleDao.getAllBillingCycles()

    suspend fun insert(payment: CardPayment) {
        paymentDao.insert(payment)
    }

    // Billing Cycle methods
    suspend fun insert(billingCycle: CardBillingCycle) {
        cardBillingCycleDao.insert(billingCycle)
    }

    suspend fun update(billingCycle: CardBillingCycle) {
        cardBillingCycleDao.update(billingCycle)
    }

    suspend fun delete(billingCycle: CardBillingCycle) {
        cardBillingCycleDao.delete(billingCycle)
    }

    suspend fun getBillingCycleByCardName(cardName: String): CardBillingCycle? {
        return cardBillingCycleDao.getBillingCycleByCardName(cardName)
    }

    suspend fun insertDefaultBillingCycles() {
        cardBillingCycleDao.insertAll(DefaultBillingCycles.cycles)
    }

    suspend fun getCardSummaryByBillingCycle(startDate: Long, endDate: Long): List<CardBillingSummary> {
        return paymentDao.getCardSummaryByBillingCycle(startDate, endDate)
    }

    suspend fun getCardBillingCycleTotal(cardName: String, startDate: Long, endDate: Long): Double? {
        return paymentDao.getCardBillingCycleTotal(cardName, startDate, endDate)
    }
}
