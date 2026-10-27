package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val titleAr: String) {
    EXPENSE("مصروف"),
    INCOME("دخل")
}

data class CategoryDefinition(
    val id: String,
    val nameAr: String,
    val type: TransactionType,
    val icon: ImageVector,
    val color: Color
)

object CategoriesData {
    val expenseCategories = listOf(
        CategoryDefinition("food", "طعام ومطاعم", TransactionType.EXPENSE, Icons.Default.Restaurant, Color(0xFFF97316)),
        CategoryDefinition("grocery", "بقالة وتموينات", TransactionType.EXPENSE, Icons.Default.LocalGroceryStore, Color(0xFFEAB308)),
        CategoryDefinition("housing", "فواتير وسكن", TransactionType.EXPENSE, Icons.Default.Home, Color(0xFF3B82F6)),
        CategoryDefinition("transport", "مواصلات ووقود", TransactionType.EXPENSE, Icons.Default.DirectionsCar, Color(0xFF6366F1)),
        CategoryDefinition("health", "صحة وعلاج", TransactionType.EXPENSE, Icons.Default.MedicalServices, Color(0xFFEF4444)),
        CategoryDefinition("shopping", "تسوق وملابس", TransactionType.EXPENSE, Icons.Default.ShoppingBag, Color(0xFFEC4899)),
        CategoryDefinition("education", "تعليم وتطوير", TransactionType.EXPENSE, Icons.Default.School, Color(0xFF8B5CF6)),
        CategoryDefinition("other_exp", "مصاريف أخرى", TransactionType.EXPENSE, Icons.Default.MoreHoriz, Color(0xFF64748B))
    )

    val incomeCategories = listOf(
        CategoryDefinition("salary", "راتب شهري", TransactionType.INCOME, Icons.Default.Payments, Color(0xFF10B981)),
        CategoryDefinition("business", "عمل حر وتجارة", TransactionType.INCOME, Icons.Default.Work, Color(0xFF06B6D4)),
        CategoryDefinition("investments", "أرباح واستثمار", TransactionType.INCOME, Icons.Default.TrendingUp, Color(0xFF84CC16)),
        CategoryDefinition("gift", "مكافأة أو هدية", TransactionType.INCOME, Icons.Default.CardGiftcard, Color(0xFFF59E0B)),
        CategoryDefinition("other_inc", "دخل آخر", TransactionType.INCOME, Icons.Default.AttachMoney, Color(0xFF14B8A6))
    )

    val allCategories = expenseCategories + incomeCategories

    fun getCategoryByName(name: String, fallbackType: TransactionType = TransactionType.EXPENSE): CategoryDefinition {
        return allCategories.find { it.nameAr == name }
            ?: (if (fallbackType == TransactionType.EXPENSE) expenseCategories.last() else incomeCategories.last())
    }
}

val defaultPaymentMethods = listOf("نقد", "بطاقة مدى / ائتمان", "تحويل بنكي", "محفظة رقمية")

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val paymentMethod: String = "نقد"
) {
    val transactionType: TransactionType
        get() = if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
}
