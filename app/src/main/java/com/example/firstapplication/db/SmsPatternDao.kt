package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SmsPatternDao {
    @Query("SELECT * FROM sms_patterns ORDER BY name ASC")
    fun getAllPatterns(): LiveData<List<SmsPattern>>

    @Query("SELECT * FROM sms_patterns WHERE isActive = 1")
    fun getActivePatterns(): List<SmsPattern>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pattern: SmsPattern)

    @Update
    suspend fun update(pattern: SmsPattern)

    @Delete
    suspend fun delete(pattern: SmsPattern)

    @Query("DELETE FROM sms_patterns")
    suspend fun deleteAll()
}