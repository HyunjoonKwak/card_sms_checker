package com.example.firstapplication.db

import androidx.lifecycle.LiveData

class SmsRepository(private val smsDao: SmsDao) {

    val allSmsMessages: LiveData<List<SmsMessage>> = smsDao.getAllSmsMessages()

    suspend fun insert(smsMessage: SmsMessage) {
        smsDao.insert(smsMessage)
    }

    suspend fun deleteAll() {
        smsDao.deleteAll()
    }
}