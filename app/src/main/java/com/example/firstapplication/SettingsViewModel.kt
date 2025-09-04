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
import org.json.JSONArray
import org.json.JSONObject

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

    fun resetPatternsToOptimizedSet() = viewModelScope.launch {
        // 모든 기존 패턴 삭제
        smsPatternDao.deleteAll()
        
        // 최적화된 필수 패턴들만 추가
        val optimizedPatterns = listOf(
            // 1. 기본 한국 카드사 패턴
            SmsPattern(
                name = "한국카드 기본",
                cardNamePattern = "(\\S+카드)",
                amountPattern = "([0-9,]+)원",
                description = "신한카드, 롯데카드 등 기본 형식"
            ),
            
            // 2. 대괄호 형식 패턴
            SmsPattern(
                name = "대괄호 카드사",
                cardNamePattern = "\\[([^\\]]+카드)\\]",
                amountPattern = "([0-9,]+)원",
                description = "[삼성카드], [현대카드], [우리카드] 형식"
            ),
            
            // 3. KB국민카드 특수 형식
            SmsPattern(
                name = "KB국민카드",
                cardNamePattern = "(KB국민카드)",
                amountPattern = "([0-9,]+)원",
                description = "KB국민카드 전용 패턴"
            ),
            
            // 4. 카드사명 없이 브랜드명만 있는 경우
            SmsPattern(
                name = "브랜드명 패턴",
                cardNamePattern = "\\[([^\\]]+)\\].*카드",
                amountPattern = "([0-9,]+)원", 
                description = "[현대], [삼성] 등 브랜드명만 있는 경우"
            ),
            
            // 5. 신용/체크카드 구분 패턴
            SmsPattern(
                name = "신용체크카드",
                cardNamePattern = "(\\S+)(신용카드|체크카드)",
                amountPattern = "([0-9,]+)원",
                description = "신용카드/체크카드 구분이 있는 경우"
            )
        )
        
        optimizedPatterns.forEach { pattern ->
            smsPatternDao.insert(pattern)
        }
    }

    fun cleanupDuplicatePatterns() = viewModelScope.launch {
        val allPatterns = smsPatternDao.getAllPatterns().value ?: return@launch
        
        // 중복 패턴 그룹화 (cardNamePattern과 amountPattern이 같은 것들)
        val duplicateGroups = allPatterns.groupBy { "${it.cardNamePattern}|${it.amountPattern}" }
        
        duplicateGroups.forEach { (_, patterns) ->
            if (patterns.size > 1) {
                // 첫 번째 패턴은 유지하고 나머지는 삭제
                patterns.drop(1).forEach { pattern ->
                    smsPatternDao.delete(pattern)
                }
            }
        }
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
                    name = "대괄호 카드명",
                    cardNamePattern = "\\[([^\\]]+카드)\\]",
                    amountPattern = "([0-9,]+)원",
                    description = "[삼성카드], [현대카드] 형식의 카드명 패턴"
                ),
                SmsPattern(
                    name = "카드사명만",
                    cardNamePattern = "(\\S+)카드",
                    amountPattern = "([0-9,]+)원",
                    description = "신한카드, KB국민카드 등 카드사명만 있는 패턴"
                )
            )
            
            defaultPatterns.forEach { pattern ->
                smsPatternDao.insert(pattern)
            }
        }
    }

    fun exportToFile(uri: Uri, contentResolver: ContentResolver, format: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val content = when (format) {
                        "CSV" -> generateCsvContent()
                        "JSON" -> generateJsonContent()
                        "TXT" -> generateTxtContent()
                        "MD" -> generateMarkdownContent()
                        "EXCEL" -> generateCsvContent() // Excel은 CSV 형식으로 저장
                        "PDF" -> generateTxtContent() // PDF는 텍스트로 저장
                        else -> generateCsvContent()
                    }
                    
                    outputStream.write(content.toByteArray())
                    callback(true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }
    
    private suspend fun generateCsvContent(): String {
        val csvBuilder = StringBuilder()
        
        // CSV 헤더
        csvBuilder.appendLine("구분,카드명,금액,날짜,발신자,메시지 내용,상점명")
        
        // 결제 데이터 추가
        paymentRepository.allPayments.value?.forEach { payment ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(payment.paymentDate)
            
            csvBuilder.appendLine(
                "결제,\"${payment.cardName}\",${payment.amount},\"$formattedDate\",\"\",\"\",\"${payment.merchant}\""
            )
        }
        
        // SMS 데이터 추가
        smsRepository.allSmsMessages.value?.forEach { sms ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(sms.receivedDate)
            val cleanMessage = sms.messageBody.replace("\"", "\"\"") // CSV escape
            
            csvBuilder.appendLine(
                "SMS,\"\",\"\",\"$formattedDate\",\"${sms.sender}\",\"$cleanMessage\",\"\""
            )
        }
        
        return csvBuilder.toString()
    }
    
    private suspend fun generateJsonContent(): String {
        val rootObject = JSONObject()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        // 결제 데이터
        val paymentsArray = JSONArray()
        paymentRepository.allPayments.value?.forEach { payment ->
            val paymentObject = JSONObject().apply {
                put("type", "payment")
                put("cardName", payment.cardName)
                put("amount", payment.amount)
                put("date", dateFormat.format(payment.paymentDate))
                put("merchant", payment.merchant)
            }
            paymentsArray.put(paymentObject)
        }
        
        // SMS 데이터
        val smsArray = JSONArray()
        smsRepository.allSmsMessages.value?.forEach { sms ->
            val smsObject = JSONObject().apply {
                put("type", "sms")
                put("sender", sms.sender)
                put("message", sms.messageBody)
                put("date", dateFormat.format(sms.receivedDate))
                put("processed", sms.isProcessed)
            }
            smsArray.put(smsObject)
        }
        
        rootObject.put("payments", paymentsArray)
        rootObject.put("sms_messages", smsArray)
        rootObject.put("export_date", dateFormat.format(java.util.Date()))
        rootObject.put("total_payments", paymentsArray.length())
        rootObject.put("total_sms", smsArray.length())
        
        return rootObject.toString(4) // Pretty print with 4 spaces
    }
    
    private suspend fun generateTxtContent(): String {
        val txtBuilder = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        txtBuilder.appendLine("=== Smart SMS 분류기 데이터 내보내기 ===")
        txtBuilder.appendLine("내보낸 날짜: ${dateFormat.format(java.util.Date())}")
        txtBuilder.appendLine()
        
        // 결제 데이터
        txtBuilder.appendLine("=== 결제 내역 ===")
        paymentRepository.allPayments.value?.forEach { payment ->
            txtBuilder.appendLine("카드명: ${payment.cardName}")
            txtBuilder.appendLine("금액: ${payment.amount}원")
            txtBuilder.appendLine("상점: ${payment.merchant}")
            txtBuilder.appendLine("날짜: ${dateFormat.format(payment.paymentDate)}")
            txtBuilder.appendLine("---")
        }
        
        txtBuilder.appendLine()
        txtBuilder.appendLine("=== SMS 내역 ===")
        smsRepository.allSmsMessages.value?.forEach { sms ->
            txtBuilder.appendLine("발신자: ${sms.sender}")
            txtBuilder.appendLine("날짜: ${dateFormat.format(sms.receivedDate)}")
            txtBuilder.appendLine("내용: ${sms.messageBody}")
            txtBuilder.appendLine("처리됨: ${if (sms.isProcessed) "예" else "아니오"}")
            txtBuilder.appendLine("---")
        }
        
        return txtBuilder.toString()
    }
    
    private suspend fun generateMarkdownContent(): String {
        val mdBuilder = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        mdBuilder.appendLine("# Smart SMS 분류기 데이터 내보내기")
        mdBuilder.appendLine()
        mdBuilder.appendLine("**내보낸 날짜:** ${dateFormat.format(java.util.Date())}")
        mdBuilder.appendLine()
        
        // 요약 정보
        val paymentsCount = paymentRepository.allPayments.value?.size ?: 0
        val smsCount = smsRepository.allSmsMessages.value?.size ?: 0
        val totalAmount = paymentRepository.allPayments.value?.sumOf { it.amount } ?: 0
        
        mdBuilder.appendLine("## 📊 요약")
        mdBuilder.appendLine("- **총 결제 건수:** $paymentsCount 건")
        mdBuilder.appendLine("- **총 결제 금액:** ${String.format("%,d", totalAmount)}원")
        mdBuilder.appendLine("- **총 SMS 건수:** $smsCount 건")
        mdBuilder.appendLine()
        
        // 결제 데이터
        mdBuilder.appendLine("## 💳 결제 내역")
        mdBuilder.appendLine()
        
        if (paymentsCount > 0) {
            mdBuilder.appendLine("| 카드명 | 금액 | 상점 | 날짜 |")
            mdBuilder.appendLine("|--------|------|------|------|")
            
            paymentRepository.allPayments.value?.forEach { payment ->
                val formattedDate = dateFormat.format(payment.paymentDate)
                val formattedAmount = String.format("%,d", payment.amount)
                mdBuilder.appendLine("| ${payment.cardName} | ${formattedAmount}원 | ${payment.merchant} | $formattedDate |")
            }
        } else {
            mdBuilder.appendLine("*결제 내역이 없습니다.*")
        }
        
        mdBuilder.appendLine()
        
        // 카드별 결제 요약
        val paymentsByCard = paymentRepository.allPayments.value?.groupBy { it.cardName }
        if (!paymentsByCard.isNullOrEmpty()) {
            mdBuilder.appendLine("## 📈 카드별 결제 요약")
            mdBuilder.appendLine()
            mdBuilder.appendLine("| 카드명 | 건수 | 총 금액 |")
            mdBuilder.appendLine("|--------|------|---------|")
            
            paymentsByCard.forEach { (cardName, payments) ->
                val count = payments.size
                val total = payments.sumOf { it.amount }
                val formattedTotal = String.format("%,d", total)
                mdBuilder.appendLine("| $cardName | ${count}건 | ${formattedTotal}원 |")
            }
            mdBuilder.appendLine()
        }
        
        // SMS 데이터
        mdBuilder.appendLine("## 📱 SMS 내역")
        mdBuilder.appendLine()
        
        if (smsCount > 0) {
            mdBuilder.appendLine("| 발신자 | 날짜 | 처리 상태 | 내용 (요약) |")
            mdBuilder.appendLine("|--------|------|-----------|------------|")
            
            smsRepository.allSmsMessages.value?.forEach { sms ->
                val formattedDate = dateFormat.format(sms.receivedDate)
                val processedStatus = if (sms.isProcessed) "✅ 처리됨" else "⭕ 미처리"
                val contentSummary = sms.messageBody.take(50) + if (sms.messageBody.length > 50) "..." else ""
                val cleanContent = contentSummary.replace("|", "\\|").replace("\n", " ")
                mdBuilder.appendLine("| ${sms.sender} | $formattedDate | $processedStatus | $cleanContent |")
            }
        } else {
            mdBuilder.appendLine("*SMS 내역이 없습니다.*")
        }
        
        mdBuilder.appendLine()
        mdBuilder.appendLine("---")
        mdBuilder.appendLine("*Smart SMS 분류기에서 생성됨*")
        
        return mdBuilder.toString()
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