package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("masroufi_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_BUDGET_LIMIT = "key_budget_limit"

        private const val KEY_REMINDER_ENABLED = "key_reminder_enabled"

        val AVAILABLE_CURRENCIES = listOf(
            "ر.س" to "ريال سعودي (SAR)",
            "د.إ" to "درهم إماراتي (AED)",
            "ج.م" to "جنيه مصري (EGP)",
            "د.ك" to "دينار كويتي (KWD)",
            "ر.ق" to "ريال قطري (QAR)",
            "ر.ع" to "ريال عماني (OMR)",
            "د.ب" to "دينار بحريني (BHD)",
            "د.أ" to "دينار أردني (JOD)",
            "د.م" to "درهم مغربي (MAD)",
            "د.ت" to "دينار تونسي (TND)",
            "د.ج" to "دينار جزائري (DZD)",
            "$" to "دولار أمريكي (USD)",
            "€" to "يورو (EUR)"
        )
    }

    private val _currency = MutableStateFlow(prefs.getString(KEY_CURRENCY, "ر.س") ?: "ر.س")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _budgetLimit = MutableStateFlow(prefs.getFloat(KEY_BUDGET_LIMIT, 6000f).toDouble())
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDER_ENABLED, true))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    fun setCurrency(newCurrency: String) {
        prefs.edit().putString(KEY_CURRENCY, newCurrency).apply()
        _currency.value = newCurrency
    }

    fun setBudgetLimit(newLimit: Double) {
        prefs.edit().putFloat(KEY_BUDGET_LIMIT, newLimit.toFloat()).apply()
        _budgetLimit.value = newLimit
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        _reminderEnabled.value = enabled
    }
}
