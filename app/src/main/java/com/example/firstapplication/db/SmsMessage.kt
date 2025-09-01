package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sender: String,
    val messageBody: String,
    val receivedDate: Date,
    val isProcessed: Boolean = false
)