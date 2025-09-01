package com.example.firstapplication

import android.app.Application
import com.example.firstapplication.db.AppDatabase
import com.example.firstapplication.db.PaymentRepository
import com.example.firstapplication.db.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentsApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { PaymentRepository(database.paymentDao(), database.cardBillingCycleDao()) }
    val smsRepository by lazy { SmsRepository(database.smsDao()) }
    
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        initializeData()
    }
    
    private fun initializeData() {
        applicationScope.launch {
            val parsingService = SmsParsingService(database)
            parsingService.initializeDefaultCategories()
        }
    }
}
