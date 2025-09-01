package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CardBillingCycleDao {

    @Query("SELECT * FROM card_billing_cycles WHERE isActive = 1 ORDER BY cardName ASC")
    fun getActiveBillingCycles(): LiveData<List<CardBillingCycle>>

    @Query("SELECT * FROM card_billing_cycles ORDER BY cardName ASC")
    fun getAllBillingCycles(): LiveData<List<CardBillingCycle>>

    @Query("SELECT * FROM card_billing_cycles WHERE cardName = :cardName LIMIT 1")
    suspend fun getBillingCycleByCardName(cardName: String): CardBillingCycle?

    @Query("SELECT billingDay FROM card_billing_cycles WHERE cardName = :cardName AND isActive = 1 LIMIT 1")
    suspend fun getBillingDayForCard(cardName: String): Int?

    @Query("SELECT cutoffDay FROM card_billing_cycles WHERE cardName = :cardName AND isActive = 1 LIMIT 1")
    suspend fun getCutoffDayForCard(cardName: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(billingCycle: CardBillingCycle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(billingCycles: List<CardBillingCycle>)

    @Update
    suspend fun update(billingCycle: CardBillingCycle)

    @Delete
    suspend fun delete(billingCycle: CardBillingCycle)

    @Query("UPDATE card_billing_cycles SET isActive = :isActive WHERE cardName = :cardName")
    suspend fun updateActiveStatus(cardName: String, isActive: Boolean)

    @Query("DELETE FROM card_billing_cycles WHERE cardName = :cardName")
    suspend fun deleteByCardName(cardName: String)

    @Query("DELETE FROM card_billing_cycles")
    suspend fun deleteAll()
}