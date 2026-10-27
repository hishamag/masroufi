package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endMillis = calendar.timeInMillis - 1

        return transactionDao.getTransactionsBetween(startMillis, endMillis)
    }

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertAll(transactions: List<TransactionEntity>) {
        transactionDao.insertAll(transactions)
    }

    suspend fun update(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }

    suspend fun getCount(): Int {
        return transactionDao.getCount()
    }

    suspend fun seedSampleDataIfEmpty() {
        if (transactionDao.getCount() > 0) return

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)

        fun createDate(day: Int, hour: Int = 12): Long {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 30)
            }.timeInMillis
        }

        val samples = listOf(
            TransactionEntity(
                title = "راتب الشهر الوظيفي",
                amount = 12500.0,
                type = TransactionType.INCOME.name,
                category = "راتب شهري",
                timestamp = createDate(1, 9),
                note = "إيداع راتب الشركة",
                paymentMethod = "تحويل بنكي"
            ),
            TransactionEntity(
                title = "مشروع تصميم موقع حر",
                amount = 2200.0,
                type = TransactionType.INCOME.name,
                category = "عمل حر وتجارة",
                timestamp = createDate(12, 16),
                note = "دفعة نهائية للعميل",
                paymentMethod = "تحويل بنكي"
            ),
            TransactionEntity(
                title = "إيجار الشقة وفواتير الخدمات",
                amount = 3200.0,
                type = TransactionType.EXPENSE.name,
                category = "فواتير وسكن",
                timestamp = createDate(2, 10),
                note = "سداد عبر سداد",
                paymentMethod = "تحويل بنكي"
            ),
            TransactionEntity(
                title = "تموينات السوبرماركت الشهرية",
                amount = 840.0,
                type = TransactionType.EXPENSE.name,
                category = "بقالة وتموينات",
                timestamp = createDate(4, 18),
                note = "خضار وفواكه ولحوم",
                paymentMethod = "بطاقة مدى / ائتمان"
            ),
            TransactionEntity(
                title = "عشاء عائلي في مطعم",
                amount = 265.0,
                type = TransactionType.EXPENSE.name,
                category = "طعام ومطاعم",
                timestamp = createDate(7, 21),
                note = "جلسة نهاية الأسبوع",
                paymentMethod = "بطاقة مدى / ائتمان"
            ),
            TransactionEntity(
                title = "بنزين السيارة وصيانة دورية",
                amount = 310.0,
                type = TransactionType.EXPENSE.name,
                category = "مواصلات ووقود",
                timestamp = createDate(10, 11),
                note = "تغيير زيت + وقود كامل",
                paymentMethod = "بطاقة مدى / ائتمان"
            ),
            TransactionEntity(
                title = "صيدلية وأدوية فيتامينات",
                amount = 180.0,
                type = TransactionType.EXPENSE.name,
                category = "صحة وعلاج",
                timestamp = createDate(14, 15),
                note = "فيتامينات موسمية ومسكنات",
                paymentMethod = "نقد"
            ),
            TransactionEntity(
                title = "اشتراك دورة تدريبية تقنية",
                amount = 290.0,
                type = TransactionType.EXPENSE.name,
                category = "تعليم وتطوير",
                timestamp = createDate(18, 14),
                note = "تطوير مهارات العمل",
                paymentMethod = "بطاقة مدى / ائتمان"
            ),
            TransactionEntity(
                title = "شراء ملابس وأحذية",
                amount = 450.0,
                type = TransactionType.EXPENSE.name,
                category = "تسوق وملابس",
                timestamp = createDate(22, 19),
                note = "عروض التخفيضات",
                paymentMethod = "بطاقة مدى / ائتمان"
            ),
            TransactionEntity(
                title = "قهوة مع الأصدقاء",
                amount = 45.0,
                type = TransactionType.EXPENSE.name,
                category = "طعام ومطاعم",
                timestamp = createDate(25, 17),
                note = "مقهى المحطة",
                paymentMethod = "نقد"
            )
        )

        transactionDao.insertAll(samples)
    }
}
