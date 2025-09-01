package com.example.firstapplication

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstapplication.db.PaymentRepository
import com.example.firstapplication.db.SmsMessage
import com.example.firstapplication.db.SmsPattern
import com.example.firstapplication.db.SmsPatternDao
import com.example.firstapplication.db.SmsRepository
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsViewModel(
    private val paymentRepository: PaymentRepository,
    private val smsRepository: SmsRepository,
    private val smsPatternDao: SmsPatternDao
) : ViewModel() {

    val allPatterns: LiveData<List<SmsPattern>> = smsPatternDao.getAllPatterns()

    fun insert(pattern: SmsPattern) = viewModelScope.launch {
        smsPatternDao.insert(pattern)
    }

    fun update(pattern: SmsPattern) = viewModelScope.launch {
        smsPatternDao.update(pattern)
    }

    fun delete(pattern: SmsPattern) = viewModelScope.launch {
        smsPatternDao.delete(pattern)
    }

    fun addDefaultPatternsIfEmpty() = viewModelScope.launch {
        val patterns = smsPatternDao.getAllPatterns().value
        if (patterns == null || patterns.isEmpty()) {
            val defaultPatterns = listOf(
                SmsPattern(
                    name = "한국 카드 기본",
                    cardNamePattern = "(\\S+카드)",
                    amountPattern = "([0-9,]+)원",
                    description = "한국 카드사의 기본 SMS 패턴"
                ),
                SmsPattern(
                    name = "결제 승인",
                    cardNamePattern = "(\\S+카드)",
                    amountPattern = "([0-9,]+)원",
                    description = "카드 결제 승인 SMS 패턴"
                )
            )
            
            defaultPatterns.forEach { pattern ->
                smsPatternDao.insert(pattern)
            }
        }
    }

    fun exportToCSV(uri: Uri, contentResolver: ContentResolver, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val csvBuilder = StringBuilder()
                    
                    // CSV 헤더
                    csvBuilder.appendLine("구분,카드명,금액,날짜,발신자,메시지 내용")
                    
                    // 결제 데이터 추가
                    paymentRepository.allPayments.value?.forEach { payment ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val formattedDate = dateFormat.format(payment.paymentDate)
                        
                        csvBuilder.appendLine(
                            "결제,\"${payment.cardName}\",${payment.amount},\"$formattedDate\",\"\",\"\""
                        )
                    }
                    
                    // SMS 데이터 추가
                    smsRepository.allSmsMessages.value?.forEach { sms ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val formattedDate = dateFormat.format(sms.receivedDate)
                        val cleanMessage = sms.messageBody.replace("\"", "\"\"") // CSV escape
                        
                        csvBuilder.appendLine(
                            "SMS,\"\",\"\",\"$formattedDate\",\"${sms.sender}\",\"$cleanMessage\""
                        )
                    }
                    
                    outputStream.write(csvBuilder.toString().toByteArray())
                    callback(true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }
}

class SettingsViewModelFactory(
    private val paymentRepository: PaymentRepository,
    private val smsRepository: SmsRepository,
    private val smsPatternDao: SmsPatternDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(paymentRepository, smsRepository, smsPatternDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}