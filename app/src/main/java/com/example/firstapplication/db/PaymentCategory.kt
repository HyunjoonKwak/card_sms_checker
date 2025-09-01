package com.example.firstapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_categories")
data class PaymentCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String = "💳", // 이모지 아이콘
    val color: String = "#2196F3", // 카테고리 색상
    val merchantPatterns: String = "", // 쉼표로 구분된 가맹점 패턴들
    val isActive: Boolean = true
)

// 기본 카테고리들
object DefaultCategories {
    val categories = listOf(
        PaymentCategory(name = "식료품", icon = "🛒", color = "#4CAF50", merchantPatterns = "마트,슈퍼,편의점,GS25,CU,세븐일레븐,이마트,롯데마트,홈플러스"),
        PaymentCategory(name = "교통", icon = "🚌", color = "#FF9800", merchantPatterns = "지하철,버스,택시,주유소,SK에너지,GS칼텍스,현대오일뱅크"),
        PaymentCategory(name = "식당", icon = "🍽️", color = "#E91E63", merchantPatterns = "음식점,카페,스타벅스,맥도날드,KFC,버거킹,치킨,피자"),
        PaymentCategory(name = "쇼핑", icon = "🛍️", color = "#9C27B0", merchantPatterns = "백화점,아울렛,온라인쇼핑,11번가,쿠팡,G마켓,옥션"),
        PaymentCategory(name = "의료", icon = "🏥", color = "#009688", merchantPatterns = "병원,의원,약국,치과,한의원"),
        PaymentCategory(name = "교육", icon = "📚", color = "#3F51B5", merchantPatterns = "학원,도서관,서점,교보문고,영풍문고"),
        PaymentCategory(name = "오락", icon = "🎮", color = "#FF5722", merchantPatterns = "영화관,CGV,롯데시네마,메가박스,노래방,PC방,게임"),
        PaymentCategory(name = "기타", icon = "💼", color = "#607D8B", merchantPatterns = "")
    )
}