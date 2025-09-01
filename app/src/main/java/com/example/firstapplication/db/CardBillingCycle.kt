package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_billing_cycles",
    indices = [Index(value = ["cardName"], unique = true)]
)
data class CardBillingCycle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardName: String,
    val bankName: String, // 은행명 (삼성, 현대, KB국민, 신한, 롯데, 우리 등)
    val billingDay: Int, // 결제일 (1-31)
    val cutoffDay: Int = billingDay - 7, // 마감일 (결제일 기준 -7일, 수정 가능)
    val isActive: Boolean = true
)

// 기본 카드사 결제일 설정
object DefaultBillingCycles {
    val cycles = listOf(
        CardBillingCycle(cardName = "삼성카드", bankName = "삼성", billingDay = 1, cutoffDay = 25),
        CardBillingCycle(cardName = "현대카드", bankName = "현대", billingDay = 1, cutoffDay = 25),
        CardBillingCycle(cardName = "KB국민카드", bankName = "KB국민", billingDay = 15, cutoffDay = 8),
        CardBillingCycle(cardName = "신한카드", bankName = "신한", billingDay = 25, cutoffDay = 18),
        CardBillingCycle(cardName = "롯데카드", bankName = "롯데", billingDay = 5, cutoffDay = 28),
        CardBillingCycle(cardName = "우리카드", bankName = "우리", billingDay = 10, cutoffDay = 3),
        CardBillingCycle(cardName = "하나카드", bankName = "하나", billingDay = 20, cutoffDay = 13),
        CardBillingCycle(cardName = "NH농협카드", bankName = "NH농협", billingDay = 12, cutoffDay = 5),
        CardBillingCycle(cardName = "BC카드", bankName = "BC", billingDay = 8, cutoffDay = 1),
        CardBillingCycle(cardName = "IBK기업은행카드", bankName = "IBK기업", billingDay = 28, cutoffDay = 21)
    )
}