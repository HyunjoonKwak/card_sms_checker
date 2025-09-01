package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY receivedDate DESC")
    fun getAllSmsMessages(): LiveData<List<SmsMessage>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(smsMessage: SmsMessage)

    @Query("DELETE FROM sms_messages")
    suspend fun deleteAll()
}