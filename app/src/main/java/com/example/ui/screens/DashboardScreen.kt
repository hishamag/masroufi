package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ads.BannerAd
import com.example.data.model.TransactionEntity
import com.example.ui.components.ArabicSummaryCard
import com.example.ui.components.DonutChart
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.components.TransactionItemCard
import com.example.ui.viewmodel.MonthlyFinanceSummary

@Composable
fun DashboardScreen(
    summary: MonthlyFinanceSummary,
    recentTransactions: List<TransactionEntity>,
    currency: String,
    monthName: String,
    year: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddNewTransaction: () -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Logo & Top Branding Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arabic_expense_icon_1793116250583),
                        contentDescription = "شعار تطبيق مصروفي",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مصروفي",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "بدون ربط بنكي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "تتبع مصاريفك الخاصة أوفلاين 100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onTestNotification,
                    modifier = Modifier.testTag("btn_quick_notification")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "تنبيه تسجيل المصاريف",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Month Selector Header
        item {
            MonthSelectorHeader(
                monthName = monthName,
                year = year,
                onPrevMonth = onPrevMonth,
                onNextMonth = onNextMonth
            )
        }

        // Hero Financial Summary Card
        item {
            ArabicSummaryCard(
                summary = summary,
                currency = currency
            )
        }

        // Banner Ad
        item {
            BannerAd(modifier = Modifier.fillMaxWidth())
        }

        // Donut Chart Category Breakdown
        item {
            DonutChart(
                breakdowns = summary.categoryBreakdowns,
                totalExpense = summary.totalExpense,
                currency = currency
            )
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر المعاملات المسجلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentTransactions.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.testTag("btn_view_all_transactions")
                    ) {
                        Text("عرض الكل")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        // Recent Transactions List (first 5)
        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد معاملات مسجلة في هذا الشهر",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddNewTransaction,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_empty_add_transaction")
                        ) {
                            Text("إضافة معاملة الآن")
                        }
                    }
                }
            }
        } else {
            items(
                items = recentTransactions.take(5),
                key = { it.id }
            ) { transaction ->
                TransactionItemCard(
                    transaction = transaction,
                    currency = currency,
                    onClick = { onEditTransaction(transaction) },
                    onDelete = { onDeleteTransaction(transaction) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp)) // padding for FAB and nav bar
        }
    }
}
