package com.example.firstapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlySummaryDao {
    @Query("SELECT * FROM monthly_summaries ORDER BY summaryMonth DESC, cardName ASC")
    fun getAllSummaries(): LiveData<List<MonthlySummary>>

    @Query("SELECT * FROM monthly_summaries WHERE summaryMonth = :month ORDER BY cardName ASC")
    fun getSummariesByMonth(month: String): LiveData<List<MonthlySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: MonthlySummary)

    @Query("DELETE FROM monthly_summaries")
    suspend fun deleteAll()
}