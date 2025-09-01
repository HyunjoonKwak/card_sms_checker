package com.example.firstapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstapplication.db.CardPayment
import com.example.firstapplication.db.CardBillingSummary
import com.example.firstapplication.db.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: PaymentRepository) : ViewModel() {

    val allPayments: LiveData<List<CardPayment>> = repository.allPayments

    fun insert(payment: CardPayment) = viewModelScope.launch {
        repository.insert(payment)
    }

    suspend fun getCurrentBillingCycleSummary(): List<CardBillingSummary> {
        val billingCycles = repository.allBillingCycles.value ?: emptyList()
        val summaries = mutableListOf<CardBillingSummary>()
        
        for (cycle in billingCycles.filter { it.isActive }) {
            val period = BillingCycleCalculator.getCurrentBillingPeriod(cycle)
            val cycleSummaries = repository.getCardSummaryByBillingCycle(
                period.startDate.time,
                period.endDate.time
            )
            summaries.addAll(cycleSummaries)
        }
        
        return summaries.sortedByDescending { it.totalAmount }
    }

    suspend fun getBillingCycleSummaryForPeriod(startDate: Long, endDate: Long): List<CardBillingSummary> {
        return repository.getCardSummaryByBillingCycle(startDate, endDate)
    }
}

class PaymentViewModelFactory(private val repository: PaymentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
