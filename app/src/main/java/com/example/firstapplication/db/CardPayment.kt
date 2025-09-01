package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "card_payments",
    indices = [
        Index(value = ["paymentDate"]),
        Index(value = ["cardName"]),
        Index(value = ["categoryId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PaymentCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class CardPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardName: String,
    val amount: Double,
    val paymentDate: Date,
    val merchant: String = "", // 가맹점 이름
    val categoryId: Int? = null, // 카테고리 외래 키
    val memo: String = "", // 사용자 메모
    val isValidated: Boolean = false // 사용자가 확인한 결제인지
)
