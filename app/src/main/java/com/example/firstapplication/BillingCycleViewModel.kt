package com.example.firstapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firstapplication.db.CardBillingCycle
import com.example.firstapplication.db.DefaultBillingCycles
import com.example.firstapplication.db.PaymentRepository

class BillingCycleViewModel(private val repository: PaymentRepository) : ViewModel() {

    val allBillingCycles: LiveData<List<CardBillingCycle>> = repository.allBillingCycles

    suspend fun insert(billingCycle: CardBillingCycle) {
        repository.insert(billingCycle)
    }

    suspend fun update(billingCycle: CardBillingCycle) {
        repository.update(billingCycle)
    }

    suspend fun delete(billingCycle: CardBillingCycle) {
        repository.delete(billingCycle)
    }

    suspend fun getBillingCycleByCardName(cardName: String): CardBillingCycle? {
        return repository.getBillingCycleByCardName(cardName)
    }

    suspend fun insertDefaultBillingCycles() {
        repository.insertDefaultBillingCycles()
    }
}

class BillingCycleViewModelFactory(private val repository: PaymentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingCycleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingCycleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}