package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.BannerAd
import com.example.ads.InterstitialAdManager
import com.example.data.model.TransactionEntity
import com.example.notifications.ExpenseNotificationHelper
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.ExportReportDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.google.android.gms.ads.MobileAds

enum class AppScreen(
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("الرئيسية", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TRANSACTIONS("المعاملات", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ANALYTICS("الإحصائيات", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    SETTINGS("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    private lateinit var interstitialAdManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AdMob SDK
        MobileAds.initialize(this) {}

        // Initialize notification channel
        ExpenseNotificationHelper.createNotificationChannel(this)

        interstitialAdManager = InterstitialAdManager(this)
        interstitialAdManager.loadAd()

        setContent {
            MyApplicationTheme {
                // Authentic Arabic Right-To-Left layout direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ExpenseTrackerApp(interstitialAdManager = interstitialAdManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    interstitialAdManager: InterstitialAdManager,
    viewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // State from ViewModel
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val budgetLimit by viewModel.budgetLimit.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val monthName = viewModel.getMonthNameAr(selectedMonth)

    // Add / Edit sheet state
    var showAddEditSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    // Export Dialog state
    var showExportDialog by remember { mutableStateOf(false) }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ExpenseNotificationHelper.showReminderNotification(context)
            Toast.makeText(context, "تم إرسال إشعار التذكير بنجاح 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "يرجى منح إذن الإشعارات من إعدادات الهاتف", Toast.LENGTH_LONG).show()
        }
    }

    fun triggerTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                ExpenseNotificationHelper.showReminderNotification(context)
                Toast.makeText(context, "تم إرسال إشعار التذكير بنجاح 🔔", Toast.LENGTH_SHORT).show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ExpenseNotificationHelper.showReminderNotification(context)
            Toast.makeText(context, "تم إرسال إشعار التذكير بنجاح 🔔", Toast.LENGTH_SHORT).show()
        }
    }

    // Schedule or cancel daily reminders based on state
    LaunchedEffect(reminderEnabled) {
        if (reminderEnabled) {
            ExpenseNotificationHelper.scheduleDailyReminder(context, hour = 20, minute = 0)
        } else {
            ExpenseNotificationHelper.cancelDailyReminder(context)
        }
    }

    // Back handling: If on secondary tab, return to Dashboard
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    AppScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.titleAr,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.titleAr,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentScreen != AppScreen.SETTINGS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        transactionToEdit = null
                        showAddEditSheet = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة عملية جديدة"
                        )
                    },
                    text = {
                        Text(
                            text = "إضافة عملية",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_transaction")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        summary = monthlySummary,
                        recentTransactions = monthlyTransactions,
                        currency = currency,
                        monthName = monthName,
                        year = selectedYear,
                        onPrevMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onNavigateToTransactions = { currentScreen = AppScreen.TRANSACTIONS },
                        onEditTransaction = { tx ->
                            transactionToEdit = tx
                            showAddEditSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        },
                        onAddNewTransaction = {
                            transactionToEdit = null
                            showAddEditSheet = true
                        },
                        onTestNotification = { triggerTestNotification() }
                    )
                }

                AppScreen.TRANSACTIONS -> {
                    TransactionsListScreen(
                        transactions = filteredTransactions,
                        currency = currency,
                        monthName = monthName,
                        year = selectedYear,
                        searchQuery = searchQuery,
                        typeFilter = typeFilter,
                        categoryFilter = categoryFilter,
                        onPrevMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                        onEditTransaction = { tx ->
                            transactionToEdit = tx
                            showAddEditSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        }
                    )
                }

                AppScreen.ANALYTICS -> {
                    AnalyticsScreen(
                        summary = monthlySummary,
                        currency = currency,
                        monthName = monthName,
                        year = selectedYear,
                        onPrevMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onOpenExportDialog = {
                            if (activity != null) {
                                interstitialAdManager.showAd(activity) {
                                    showExportDialog = true
                                }
                            } else {
                                showExportDialog = true
                            }
                        }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentCurrency = currency,
                        currentBudgetLimit = budgetLimit,
                        reminderEnabled = reminderEnabled,
                        onCurrencyChange = { viewModel.setCurrency(it) },
                        onBudgetLimitChange = { viewModel.setBudgetLimit(it) },
                        onToggleReminder = { enabled ->
                            viewModel.setReminderEnabled(enabled)
                            if (enabled) {
                                triggerTestNotification()
                            }
                        },
                        onSendTestNotification = { triggerTestNotification() },
                        onShowInterstitialAd = {
                            if (activity != null) {
                                interstitialAdManager.showAd(activity) {
                                    Toast.makeText(context, "تم إغلاق الإعلان البيني", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSeedDemoData = { viewModel.seedDemoData() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit Transaction
    if (showAddEditSheet) {
        AddEditTransactionSheet(
            existingTransaction = transactionToEdit,
            currency = currency,
            onDismiss = {
                showAddEditSheet = false
                transactionToEdit = null
            },
            onSave = { id, title, amount, type, category, timestamp, note, paymentMethod ->
                viewModel.saveTransaction(
                    id = id,
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    timestamp = timestamp,
                    note = note,
                    paymentMethod = paymentMethod
                )
            }
        )
    }

    // Export Report Dialog
    if (showExportDialog) {
        ExportReportDialog(
            reportText = viewModel.generateReportText(),
            reportCsv = viewModel.generateReportCsv(),
            onDismiss = { showExportDialog = false }
        )
    }
}
