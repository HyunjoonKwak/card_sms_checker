package com.example.firstapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstapplication.db.SmsMessage
import com.example.firstapplication.db.SmsRepository
import kotlinx.coroutines.launch

class SmsViewModel(private val repository: SmsRepository) : ViewModel() {

    val allSmsMessages: LiveData<List<SmsMessage>> = repository.allSmsMessages

    fun insert(smsMessage: SmsMessage) = viewModelScope.launch {
        repository.insert(smsMessage)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class SmsViewModelFactory(private val repository: SmsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SmsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}