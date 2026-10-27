package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CategoriesData
import com.example.data.model.CategoryDefinition
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.preferences.UserPreferences
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CategoryBreakdown(
    val category: CategoryDefinition,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class MonthlyFinanceSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val savingsRate: Double = 0.0,
    val budgetLimit: Double = 6000.0,
    val budgetSpentPercentage: Double = 0.0,
    val budgetRemaining: Double = 6000.0,
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val dailyExpenses: Map<Int, Double> = emptyMap(),
    val daysInMonth: Int = 30
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val preferences = UserPreferences(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TransactionRepository(db.transactionDao())

        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    val currency: StateFlow<String> = preferences.currency
    val budgetLimit: StateFlow<Double> = preferences.budgetLimit
    val reminderEnabled: StateFlow<Boolean> = preferences.reminderEnabled

    // Current selected Year & Month (0-based for Calendar.MONTH)
    private val initialCal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(initialCal.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(initialCal.get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter: StateFlow<TransactionType?> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    // Monthly raw transactions
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = combine(
        _selectedYear,
        _selectedMonth
    ) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getTransactionsForMonth(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered transactions for the list screen
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        monthlyTransactions,
        _searchQuery,
        _typeFilter,
        _categoryFilter
    ) { transactions, query, type, category ->
        transactions.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.note.contains(query, ignoreCase = true)
            val matchesType = type == null || item.transactionType == type
            val matchesCategory = category == null || item.category == category
            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Monthly financial summary & charts data
    val monthlySummary: StateFlow<MonthlyFinanceSummary> = combine(
        monthlyTransactions,
        preferences.budgetLimit,
        _selectedYear,
        _selectedMonth
    ) { transactions, budget, year, month ->
        var income = 0.0
        var expense = 0.0
        val expenseCategoryMap = mutableMapOf<String, Pair<Double, Int>>()
        val dailyMap = mutableMapOf<Int, Double>()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (item in transactions) {
            if (item.transactionType == TransactionType.INCOME) {
                income += item.amount
            } else {
                expense += item.amount
                val current = expenseCategoryMap[item.category] ?: Pair(0.0, 0)
                expenseCategoryMap[item.category] = Pair(current.first + item.amount, current.second + 1)

                val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                val day = itemCal.get(Calendar.DAY_OF_MONTH)
                dailyMap[day] = (dailyMap[day] ?: 0.0) + item.amount
            }
        }

        val balance = income - expense
        val savingsRate = if (income > 0) ((income - expense) / income * 100.0).coerceAtLeast(0.0) else 0.0
        val budgetSpentPct = if (budget > 0) ((expense / budget) * 100.0) else 0.0
        val budgetRemaining = budget - expense

        val breakdowns = expenseCategoryMap.map { (catName, pair) ->
            val def = CategoriesData.getCategoryByName(catName, TransactionType.EXPENSE)
            val pct = if (expense > 0) (pair.first / expense * 100).toFloat() else 0f
            CategoryBreakdown(
                category = def,
                totalAmount = pair.first,
                percentage = pct,
                count = pair.second
            )
        }.sortedByDescending { it.totalAmount }

        MonthlyFinanceSummary(
            totalIncome = income,
            totalExpense = expense,
            netBalance = balance,
            savingsRate = savingsRate,
            budgetLimit = budget,
            budgetSpentPercentage = budgetSpentPct,
            budgetRemaining = budgetRemaining,
            categoryBreakdowns = breakdowns,
            dailyExpenses = dailyMap,
            daysInMonth = daysInMonth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyFinanceSummary()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun prevMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun setMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun saveTransaction(
        id: Long = 0,
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        timestamp: Long,
        note: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                title = title.trim(),
                amount = amount,
                type = type.name,
                category = category,
                timestamp = timestamp,
                note = note.trim(),
                paymentMethod = paymentMethod
            )
            if (id == 0L) {
                repository.insert(entity)
            } else {
                repository.update(entity)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun setCurrency(newCurrency: String) {
        preferences.setCurrency(newCurrency)
    }

    fun setBudgetLimit(newLimit: Double) {
        preferences.setBudgetLimit(newLimit)
    }

    fun setReminderEnabled(enabled: Boolean) {
        preferences.setReminderEnabled(enabled)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun getMonthNameAr(month: Int): String {
        return when (month) {
            0 -> "يناير"
            1 -> "فبراير"
            2 -> "مارس"
            3 -> "أبريل"
            4 -> "مايو"
            5 -> "يونيو"
            6 -> "يوليو"
            7 -> "أغسطس"
            8 -> "سبتمبر"
            9 -> "أكتوبر"
            10 -> "نوفمبر"
            11 -> "ديسمبر"
            else -> ""
        }
    }

    fun generateReportText(): String {
        val summary = monthlySummary.value
        val items = monthlyTransactions.value
        val cur = currency.value
        val monthName = getMonthNameAr(_selectedMonth.value)
        val year = _selectedYear.value
        val numberFormat = NumberFormat.getNumberInstance(Locale("ar")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

        val sb = StringBuilder()
        sb.append("📊 تقرير مصروفي المالي - $monthName $year\n")
        sb.append("====================================\n\n")
        sb.append("💰 الملخص الشهري:\n")
        sb.append("• إجمالي الدخل: ${numberFormat.format(summary.totalIncome)} $cur\n")
        sb.append("• إجمالي المصروفات: ${numberFormat.format(summary.totalExpense)} $cur\n")
        sb.append("• صافي التوفير / الرصيد: ${numberFormat.format(summary.netBalance)} $cur\n")
        sb.append("• نسبة التوفير: ${numberFormat.format(summary.savingsRate)}%\n")
        sb.append("• ميزانية الشهر: ${numberFormat.format(summary.budgetLimit)} $cur\n")
        sb.append("• استهلاك الميزانية: ${numberFormat.format(summary.budgetSpentPercentage)}%\n\n")

        sb.append("🏷️ توزيع المصروفات حسب التصنيف:\n")
        if (summary.categoryBreakdowns.isEmpty()) {
            sb.append("لا توجد مصروفات مسجلة لهذا الشهر.\n")
        } else {
            summary.categoryBreakdowns.forEach { cat ->
                sb.append("• ${cat.category.nameAr}: ${numberFormat.format(cat.totalAmount)} $cur (${String.format(Locale.US, "%.1f", cat.percentage)}% - ${cat.count} عمليات)\n")
            }
        }
        sb.append("\n📝 سجل العمليات بالتفصيل (${items.size} عملية):\n")
        items.forEachIndexed { index, tx ->
            val sign = if (tx.transactionType == TransactionType.INCOME) "(+) دخل" else "(-) مصروف"
            val dateStr = dateFormat.format(tx.timestamp)
            sb.append("${index + 1}. [${dateStr}] ${tx.title} - ${numberFormat.format(tx.amount)} $cur [${tx.category}] - ${tx.paymentMethod} $sign")
            if (tx.note.isNotBlank()) {
                sb.append(" (ملاحظة: ${tx.note})")
            }
            sb.append("\n")
        }

        sb.append("\n---\nتم التصدير عبر تطبيق مصروفي (محلي وأوفلاين 100%)")
        return sb.toString()
    }

    fun generateReportCsv(): String {
        val items = monthlyTransactions.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("المعرف,التاريخ,العنوان,المبلغ,النوع,التصنيف,طريقة الدفع,ملاحظات\n")
        items.forEach { tx ->
            val date = dateFormat.format(tx.timestamp)
            val noteSafe = tx.note.replace(",", " ").replace("\n", " ")
            val titleSafe = tx.title.replace(",", " ")
            sb.append("${tx.id},\"$date\",\"$titleSafe\",${tx.amount},\"${tx.transactionType.titleAr}\",\"${tx.category}\",\"${tx.paymentMethod}\",\"$noteSafe\"\n")
        }
        return sb.toString()
    }
}
