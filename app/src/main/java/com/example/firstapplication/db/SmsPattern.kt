package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_patterns")
data class SmsPattern(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val cardNamePattern: String,
    val amountPattern: String,
    val description: String = "",
    val isActive: Boolean = true
)