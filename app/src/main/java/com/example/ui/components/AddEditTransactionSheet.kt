package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoriesData
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.defaultPaymentMethods
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: TransactionEntity? = null,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        timestamp: Long,
        note: String,
        paymentMethod: String
    ) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedType by remember {
        mutableStateOf(existingTransaction?.transactionType ?: TransactionType.EXPENSE)
    }
    var title by remember {
        mutableStateOf(existingTransaction?.title ?: "")
    }
    var amountText by remember {
        mutableStateOf(existingTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    val categoriesList = if (selectedType == TransactionType.EXPENSE) {
        CategoriesData.expenseCategories
    } else {
        CategoriesData.incomeCategories
    }
    var selectedCategory by remember(selectedType) {
        mutableStateOf(
            existingTransaction?.category?.takeIf { cat ->
                categoriesList.any { it.nameAr == cat }
            } ?: categoriesList.first().nameAr
        )
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(existingTransaction?.paymentMethod ?: defaultPaymentMethods.first())
    }
    var note by remember {
        mutableStateOf(existingTransaction?.note ?: "")
    }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val quickExpenseTitles = listOf("بقالة", "غداء/عشاء", "بنزين", "فاتورة كهرباء", "قهوة", "صيدلية", "تسوق")
    val quickIncomeTitles = listOf("راتب شهري", "عمل إضافي", "مكافأة", "استثمار", "تحويل")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction == null) "إضافة عملية جديدة" else "تعديل العملية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Tab Selector (مصروف vs دخل)
            TabRow(
                selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = {
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = CategoriesData.expenseCategories.first().nameAr
                    },
                    modifier = Modifier.testTag("tab_type_expense"),
                    text = {
                        Text(
                            text = "مصروف (-)",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = {
                        selectedType = TransactionType.INCOME
                        selectedCategory = CategoriesData.incomeCategories.first().nameAr
                    },
                    modifier = Modifier.testTag("tab_type_income"),
                    text = {
                        Text(
                            text = "دخل (+)",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = false
                },
                label = { Text("المبلغ") },
                suffix = {
                    Text(
                        text = currency,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("يرجى إدخال مبلغ صحيح أكبر من الصفر") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transaction_amount"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title Input Field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("الوصف / اسم العملية") },
                placeholder = { Text("مثال: مشتريات بنده، فاتورة الهاتف...") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("يرجى كتابة وصف العملية") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transaction_title"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick suggestion chips
            val currentQuickTitles = if (selectedType == TransactionType.EXPENSE) quickExpenseTitles else quickIncomeTitles
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentQuickTitles.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { title = tag; titleError = false }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection Header
            Text(
                text = "اختر التصنيف",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Grid / Flow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categoriesList.forEach { category ->
                    val isSelected = selectedCategory == category.nameAr
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) category.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) category.color else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = category.nameAr }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(category.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.nameAr,
                                    tint = category.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.nameAr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Payment Method Selector
            Text(
                text = "طريقة الدفع / الاستلام",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                defaultPaymentMethods.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentMethod = method },
                        label = { Text(method, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("ملاحظة إضافية (اختياري)") },
                placeholder = { Text("أضف أي تفاصيل أو أرقام فواتير...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_transaction_note"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        titleError = true
                    }
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = true
                    }
                    if (title.isNotBlank() && parsedAmount != null && parsedAmount > 0.0) {
                        onSave(
                            existingTransaction?.id ?: 0L,
                            title,
                            parsedAmount,
                            selectedType,
                            selectedCategory,
                            existingTransaction?.timestamp ?: System.currentTimeMillis(),
                            note,
                            selectedPaymentMethod
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_transaction"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primary else IncomeGreen
                )
            ) {
                Text(
                    text = if (existingTransaction == null) "حفظ المعاملة" else "تحديث المعاملة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
