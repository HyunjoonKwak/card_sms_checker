package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PaymentCategoryDao {
    @Query("SELECT * FROM payment_categories WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCategories(): LiveData<List<PaymentCategory>>

    @Query("SELECT * FROM payment_categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<PaymentCategory>>

    @Query("SELECT * FROM payment_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): PaymentCategory?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: PaymentCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<PaymentCategory>)

    @Update
    suspend fun update(category: PaymentCategory)

    @Delete
    suspend fun delete(category: PaymentCategory)

    @Query("DELETE FROM payment_categories")
    suspend fun deleteAll()
}