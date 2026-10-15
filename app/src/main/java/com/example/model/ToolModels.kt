package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val displayName: String) {
    FINANCE("Financial"),
    MATH("Mathematics"),
    UTILITY("Conversions"),
    DAILY_LIFE("Daily Life")
}

data class ToolInfo(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val color: Color
)

object ToolRegistry {
    val ALL_TOOLS = listOf(
        ToolInfo(
            id = "taraju",
            title = "Taraju (Rate & Weight)",
            description = "Retail rate to weight & weight to price calculator (कीमत और वजन तराजू)",
            category = ToolCategory.DAILY_LIFE,
            icon = Icons.Default.Scale,
            color = Color(0xFF0D9488)
        ),
        ToolInfo(
            id = "emi_calculator",
            title = "EMI Calculator",
            description = "Calculate loan EMI, interest, & monthly amortization breakdown",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF2563EB)
        ),
        ToolInfo(
            id = "gst_calculator",
            title = "GST Calculator",
            description = "Add or remove GST with standard tax slabs (3%, 5%, 12%, 18%, 28%)",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.ReceiptLong,
            color = Color(0xFF0D9488)
        ),
        ToolInfo(
            id = "sip_calculator",
            title = "SIP Calculator",
            description = "Project mutual funds and SIP wealth creation returns",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.Timeline,
            color = Color(0xFF059669)
        ),
        ToolInfo(
            id = "discount_calculator",
            title = "Discount Calculator",
            description = "Find final discounted price, total savings, and optional tax",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.Discount,
            color = Color(0xFFEA580C)
        ),
        ToolInfo(
            id = "profit_loss",
            title = "Profit & Loss",
            description = "Evaluate net profit/loss, margin %, and cost comparisons",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF16A34A)
        ),
        ToolInfo(
            id = "fd_rd_calculator",
            title = "FD & RD Calculator",
            description = "Fixed & recurring deposits maturity with compound intervals",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.Savings,
            color = Color(0xFF7C3AED)
        ),
        ToolInfo(
            id = "interest_calculator",
            title = "Interest Calculator",
            description = "Calculate simple and compound interest over custom periods",
            category = ToolCategory.FINANCE,
            icon = Icons.Default.Percent,
            color = Color(0xFF4F46E5)
        ),
        ToolInfo(
            id = "unit_converter",
            title = "Unit Converter",
            description = "Convert Length, Mass, Temp, Area, Volume, Speed, Time, Data",
            category = ToolCategory.UTILITY,
            icon = Icons.Default.Scale,
            color = Color(0xFF0284C7)
        ),
        ToolInfo(
            id = "currency_converter",
            title = "Currency Converter",
            description = "Real-time & offline exchange rate converter with global currencies",
            category = ToolCategory.UTILITY,
            icon = Icons.Default.CurrencyExchange,
            color = Color(0xFF0D9488)
        ),
        ToolInfo(
            id = "age_calculator",
            title = "Age Calculator",
            description = "Precise age in years, months, days & next birthday countdown",
            category = ToolCategory.DAILY_LIFE,
            icon = Icons.Default.Cake,
            color = Color(0xFFDB2777)
        ),
        ToolInfo(
            id = "date_calculator",
            title = "Date Calculator",
            description = "Days difference between dates & add/subtract calendar days",
            category = ToolCategory.DAILY_LIFE,
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFF9333EA)
        ),
        ToolInfo(
            id = "tip_split",
            title = "Tip & Bill Split",
            description = "Split restaurant bills, customize tips, and share per-person cost",
            category = ToolCategory.DAILY_LIFE,
            icon = Icons.Default.CallSplit,
            color = Color(0xFFD97706)
        ),
        ToolInfo(
            id = "percentage",
            title = "Percentage Calculator",
            description = "X% of Y, what % is X of Y, and percentage increase/decrease",
            category = ToolCategory.MATH,
            icon = Icons.Default.Calculate,
            color = Color(0xFF2563EB)
        ),
        ToolInfo(
            id = "statistics",
            title = "Average & Statistics",
            description = "Calculate Mean, Median, Mode, Min, Max, and Sum of values",
            category = ToolCategory.MATH,
            icon = Icons.Default.ShowChart,
            color = Color(0xFF0891B2)
        ),
        ToolInfo(
            id = "random_gen",
            title = "Random Generator",
            description = "Generate random number ranges, roll dice, or flip a coin",
            category = ToolCategory.DAILY_LIFE,
            icon = Icons.Default.Casino,
            color = Color(0xFFE11D48)
        )
    )

    fun getToolById(id: String): ToolInfo? = ALL_TOOLS.find { it.id == id }
}
