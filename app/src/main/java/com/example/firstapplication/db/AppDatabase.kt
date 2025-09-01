package com.example.firstapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CardPayment::class, SmsMessage::class, SmsPattern::class, PaymentCategory::class, MonthlySummary::class, CardBillingCycle::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao
    abstract fun smsDao(): SmsDao
    abstract fun smsPatternDao(): SmsPatternDao
    abstract fun paymentCategoryDao(): PaymentCategoryDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao
    abstract fun cardBillingCycleDao(): CardBillingCycleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payment_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
