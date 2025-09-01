package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "monthly_summaries",
    indices = [Index(value = ["cardName", "summaryMonth"], unique = true)]
)
data class MonthlySummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardName: String,
    val totalAmount: Double,
    val summaryMonth: String, // "2024-01" 형식
    val receivedDate: Date,
    val originalMessage: String
)