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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(payment: CardPayment)

    @Update
    suspend fun update(payment: CardPayment)

    @Delete
    suspend fun delete(payment: CardPayment)

    @Query("DELETE FROM card_payments")
    suspend fun deleteAll()
}
