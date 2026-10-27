package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoriesData
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun TransactionsListScreen(
    transactions: List<TransactionEntity>,
    currency: String,
    monthName: String,
    year: Int,
    searchQuery: String,
    typeFilter: TransactionType?,
    categoryFilter: String?,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val categoryOptions = if (typeFilter == TransactionType.INCOME) {
        CategoriesData.incomeCategories
    } else {
        CategoriesData.expenseCategories
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_list_screen")
    ) {
        // Sticky Header Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonthSelectorHeader(
                monthName = monthName,
                year = year,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("بحث في المعاملات أو الملاحظات...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح البحث"
                            )
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_transactions"),
                shape = RoundedCornerShape(14.dp)
            )

            // Type Tabs: الكل / مصاريف / مداخيل
            val selectedTabIndex = when (typeFilter) {
                null -> 0
                TransactionType.EXPENSE -> 1
                TransactionType.INCOME -> 2
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { onTypeFilterChange(null) },
                    modifier = Modifier.testTag("tab_filter_all"),
                    text = { Text("الكل", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { onTypeFilterChange(TransactionType.EXPENSE) },
                    modifier = Modifier.testTag("tab_filter_expenses"),
                    text = {
                        Text(
                            "المصاريف",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { onTypeFilterChange(TransactionType.INCOME) },
                    modifier = Modifier.testTag("tab_filter_incomes"),
                    text = {
                        Text(
                            "الدخل",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 2) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Categories horizontal filter chip row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = categoryFilter == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("جميع التصنيفات") },
                    shape = RoundedCornerShape(10.dp)
                )

                categoryOptions.forEach { cat ->
                    val isSelected = categoryFilter == cat.nameAr
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) onCategoryFilterChange(null) else onCategoryFilterChange(cat.nameAr)
                        },
                        label = { Text(cat.nameAr) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color.copy(alpha = 0.2f),
                            selectedLabelColor = cat.color
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "لا توجد معاملات مطابقة للبحث أو التصفية",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotEmpty() || typeFilter != null || categoryFilter != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            onSearchQueryChange("")
                            onTypeFilterChange(null)
                            onCategoryFilterChange(null)
                        }) {
                            Text("إلغاء جميع الفلاتر")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItemCard(
                        transaction = transaction,
                        currency = currency,
                        onClick = { onEditTransaction(transaction) },
                        onDelete = { transactionToDelete = transaction }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "حذف المعاملة؟",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف العملية \"${tx.title}\" بمبلغ ${tx.amount} $currency؟ لا يمكن التراجع عن هذا الإجراء.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { transactionToDelete = null },
                    modifier = Modifier.testTag("btn_cancel_delete")
                ) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
