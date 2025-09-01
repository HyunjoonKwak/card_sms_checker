package com.example.firstapplication

import android.util.Log
import com.example.firstapplication.db.AppDatabase
import com.example.firstapplication.db.CardPayment
import com.example.firstapplication.db.DefaultCategories
import com.example.firstapplication.db.MonthlySummary
import com.example.firstapplication.db.PaymentCategory
import com.example.firstapplication.db.SmsPattern
import java.text.SimpleDateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.regex.Pattern

class SmsParsingService(private val database: AppDatabase) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    data class ParsedPayment(
        val cardName: String,
        val amount: Double,
        val merchant: String = "",
        val confidence: Float = 0.0f
    )

    fun parsePaymentSms(messageBody: String, onResult: (ParsedPayment?) -> Unit) {
        scope.launch {
            try {
                // 먼저 월별 총합 SMS인지 확인
                if (isMonthlyTotalSms(messageBody)) {
                    parseMonthlySummary(messageBody)
                    onResult(null) // 월별 총합은 개별 결제가 아니므로 null 반환
                    return@launch
                }
                
                val patterns = database.smsPatternDao().getActivePatterns()
                val result = parseWithPatterns(messageBody, patterns)
                
                if (result != null) {
                    // 카테고리 자동 분류
                    val categoryId = categorizePayment(result.merchant)
                    
                    val payment = CardPayment(
                        cardName = result.cardName,
                        amount = result.amount,
                        merchant = result.merchant,
                        paymentDate = Date(),
                        categoryId = categoryId,
                        isValidated = result.confidence > 0.8f
                    )
                    
                    database.paymentDao().insert(payment)
                    Log.d("SmsParsingService", "Payment saved with category: ${result.cardName} - ${result.amount}원 (${result.merchant})")
                }
                
                onResult(result)
            } catch (e: Exception) {
                Log.e("SmsParsingService", "Error parsing SMS", e)
                onResult(null)
            }
        }
    }

    private fun parseWithPatterns(messageBody: String, patterns: List<SmsPattern>): ParsedPayment? {
        var bestResult: ParsedPayment? = null
        var highestConfidence = 0.0f

        for (pattern in patterns) {
            try {
                val cardPattern = Pattern.compile(pattern.cardNamePattern)
                val amountPattern = Pattern.compile(pattern.amountPattern)
                
                val cardMatcher = cardPattern.matcher(messageBody)
                val amountMatcher = amountPattern.matcher(messageBody)
                
                if (cardMatcher.find() && amountMatcher.find()) {
                    val cardName = cardMatcher.group(1)?.trim()
                    val amountString = amountMatcher.group(1)?.replace(",", "")
                    val amount = amountString?.toDoubleOrNull()
                    
                    if (cardName != null && amount != null && amount > 0) {
                        val merchant = extractMerchant(messageBody)
                        val confidence = calculateConfidence(messageBody, cardName, amount, merchant)
                        
                        val result = ParsedPayment(
                            cardName = cardName,
                            amount = amount,
                            merchant = merchant,
                            confidence = confidence
                        )
                        
                        if (confidence > highestConfidence) {
                            bestResult = result
                            highestConfidence = confidence
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("SmsParsingService", "Pattern parsing failed for: ${pattern.name}", e)
            }
        }
        
        return bestResult
    }

    private fun extractMerchant(messageBody: String): String {
        // 일반적인 가맹점명 패턴들
        val merchantPatterns = listOf(
            "([가-힣\\w\\s]+)에서\\s*사용",
            "([가-힣\\w\\s]+)\\s*승인",
            "([가-힣\\w\\s]+)\\s*결제",
            "\\[([가-힣\\w\\s]+)\\]"
        )
        
        for (patternStr in merchantPatterns) {
            try {
                val pattern = Pattern.compile(patternStr)
                val matcher = pattern.matcher(messageBody)
                if (matcher.find()) {
                    return matcher.group(1)?.trim() ?: ""
                }
            } catch (e: Exception) {
                // 패턴 오류 무시하고 다음으로
            }
        }
        
        return ""
    }

    private fun calculateConfidence(
        messageBody: String, 
        cardName: String, 
        amount: Double, 
        merchant: String
    ): Float {
        var confidence = 0.0f
        
        // 기본 신뢰도 (카드명과 금액이 파싱되었으므로)
        confidence += 0.4f
        
        // 결제 관련 키워드 존재
        val paymentKeywords = listOf("결제", "승인", "사용", "출금")
        if (paymentKeywords.any { messageBody.contains(it) }) {
            confidence += 0.2f
        }
        
        // 가맹점명이 파싱됨
        if (merchant.isNotEmpty()) {
            confidence += 0.2f
        }
        
        // 금액이 합리적 범위
        if (amount in 100.0..1000000.0) {
            confidence += 0.1f
        }
        
        // 카드사명이 명확함
        if (cardName.contains("카드") || cardName.contains("Card")) {
            confidence += 0.1f
        }
        
        return confidence.coerceAtMost(1.0f)
    }

    private suspend fun categorizePayment(merchant: String): Int? {
        if (merchant.isEmpty()) return null
        
        val categories = database.paymentCategoryDao().getAllCategories().value ?: return null
        
        for (category in categories) {
            val patterns = category.merchantPatterns.split(",").map { it.trim() }
            for (pattern in patterns) {
                if (pattern.isNotEmpty() && merchant.contains(pattern, ignoreCase = true)) {
                    return category.id
                }
            }
        }
        
        return null
    }

    private fun isMonthlyTotalSms(messageBody: String): Boolean {
        val monthlyKeywords = listOf(
            "월 이용금액", "월 사용금액", "월간 이용액", "이번달 총", "당월 이용액",
            "월별 사용내역", "이용금액 안내", "월 결제금액", "총 이용금액"
        )
        
        return monthlyKeywords.any { messageBody.contains(it) } &&
               (messageBody.contains("원") || messageBody.contains("만원"))
    }

    private suspend fun parseMonthlySummary(messageBody: String) {
        try {
            // 카드사명 추출
            val cardPatterns = listOf(
                "\\[([^\\]]+카드)\\]",
                "([가-힣]+카드)",
                "\\[([^\\]]+)\\].*카드"
            )
            
            var cardName = "알 수 없는 카드"
            for (pattern in cardPatterns) {
                val matcher = Pattern.compile(pattern).matcher(messageBody)
                if (matcher.find()) {
                    cardName = matcher.group(1) ?: cardName
                    break
                }
            }
            
            // 금액 추출 (다양한 패턴 지원)
            val amountPatterns = listOf(
                "([0-9,]+)원",
                "([0-9,]+)만원",
                "총 ([0-9,]+)원",
                "금액 ([0-9,]+)원"
            )
            
            var totalAmount = 0.0
            for (pattern in amountPatterns) {
                val matcher = Pattern.compile(pattern).matcher(messageBody)
                if (matcher.find()) {
                    val amountStr = matcher.group(1)?.replace(",", "") ?: "0"
                    totalAmount = amountStr.toDoubleOrNull() ?: 0.0
                    
                    // "만원" 단위면 10000을 곱함
                    if (pattern.contains("만원")) {
                        totalAmount *= 10000
                    }
                    break
                }
            }
            
            if (totalAmount > 0) {
                val currentDate = Date()
                val monthFormat = SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                val summaryMonth = monthFormat.format(currentDate)
                
                val summary = MonthlySummary(
                    cardName = cardName,
                    totalAmount = totalAmount,
                    summaryMonth = summaryMonth,
                    receivedDate = currentDate,
                    originalMessage = messageBody
                )
                
                database.monthlySummaryDao().insert(summary)
                Log.d("SmsParsingService", "Monthly summary saved: $cardName - ${totalAmount}원 for $summaryMonth")
            }
        } catch (e: Exception) {
            Log.e("SmsParsingService", "Failed to parse monthly summary", e)
        }
    }

    suspend fun initializeDefaultCategories() {
        try {
            val existingCategories = database.paymentCategoryDao().getAllCategories().value
            if (existingCategories.isNullOrEmpty()) {
                database.paymentCategoryDao().insertAll(DefaultCategories.categories)
                Log.d("SmsParsingService", "Default categories initialized")
            }
        } catch (e: Exception) {
            Log.e("SmsParsingService", "Failed to initialize default categories", e)
        }
    }
}