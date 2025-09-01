package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PaymentDao {

    @Query("SELECT * FROM card_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): LiveData<List<CardPayment>>

    @Query("SELECT * FROM card_payments WHERE categoryId = :categoryId ORDER BY paymentDate DESC")
    fun getPaymentsByCategory(categoryId: Int): LiveData<List<CardPayment>>

    @Query("""
        SELECT SUM(amount) FROM card_payments 
        WHERE strftime('%Y-%m', paymentDate / 1000, 'unixepoch') = :yearMonth
    """)
    suspend fun getMonthlyTotal(yearMonth: String): Double?

    @Query("""
        SELECT SUM(amount) FROM card_payments 
        WHERE strftime('%Y', paymentDate / 1000, 'unixepoch') = :year
    """)
    suspend fun getYearlyTotal(year: String): Double?

    @Query("SELECT * FROM card_payments WHERE isValidated = 0 ORDER BY paymentDate DESC")
    fun getUnvalidatedPayments(): LiveData<List<CardPayment>>

    @Query("""
        SELECT cardName, SUM(amount) as totalAmount, COUNT(*) as transactionCount
        FROM card_payments 
        WHERE strftime('%Y-%m', paymentDate / 1000, 'unixepoch') = :yearMonth
        GROUP BY cardName 
        ORDER BY totalAmount DESC
    """)
    suspend fun getCardSummaryByMonth(yearMonth: String): List<CardSummaryData>

    @Query("""
        SELECT cardName, SUM(amount) as totalAmount, COUNT(*) as transactionCount
        FROM card_payments 
        GROUP BY cardName 
        ORDER BY totalAmount DESC
    """)
    fun getAllTimeCardSummary(): LiveData<List<CardSummaryData>>

    @Query("""
        SELECT cp.cardName, 
               SUM(cp.amount) as totalAmount, 
               COUNT(*) as transactionCount,
               cbc.bankName,
               cbc.billingDay,
               cbc.cutoffDay
        FROM card_payments cp
        LEFT JOIN card_billing_cycles cbc ON cp.cardName = cbc.cardName
        WHERE cp.paymentDate >= :startDate 
        AND cp.paymentDate < :endDate
        AND cbc.isActive = 1
        GROUP BY cp.cardName, cbc.bankName, cbc.billingDay, cbc.cutoffDay
        ORDER BY totalAmount DESC
    """)
    suspend fun getCardSummaryByBillingCycle(startDate: Long, endDate: Long): List<CardBillingSummary>

    @Query("""
        SELECT SUM(amount) FROM card_payments cp
        LEFT JOIN card_billing_cycles cbc ON cp.cardName = cbc.cardName
        WHERE cp.cardName = :cardName
        AND cp.paymentDate >= :startDate 
        AND cp.paymentDate < :endDate
        AND cbc.isActive = 1
    """)
    suspend fun getCardBillingCycleTotal(cardName: String, startDate: Long, endDate: Long): Double?

data class CardSummaryData(
    val cardName: String,
    val totalAmount: Double,
    val transactionCount: Int
)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(payment: CardPayment)

    @Update
    suspend fun update(payment: CardPayment)

    @Delete
    suspend fun delete(payment: CardPayment)

    @Query("DELETE FROM card_payments")
    suspend fun deleteAll()
}
