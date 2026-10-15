package com.example.model

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String,
    val rateAgainstUsd: Double // 1 USD = rateAgainstUsd Currency
)

object CurrencyRegistry {
    const val LAST_UPDATED = "Today, 12:00 PM (Offline-ready)"

    val CURRENCIES = listOf(
        CurrencyInfo("USD", "United States Dollar", "$", "🇺🇸", 1.0),
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳", 83.45),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺", 0.92),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧", 0.79),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵", 154.60),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺", 1.52),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦", 1.36),
        CurrencyInfo("AED", "UAE Dirham", "AED", "🇦🇪", 3.67),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬", 1.35),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭", 0.90),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳", 7.23),
        CurrencyInfo("SAR", "Saudi Riyal", "SAR", "🇸🇦", 3.75),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", 1.66),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷", 5.15),
        CurrencyInfo("RUB", "Russian Ruble", "₽", "🇷🇺", 91.50),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷", 1375.0),
        CurrencyInfo("THB", "Thai Baht", "฿", "🇹🇭", 36.80),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦", 18.35)
    )

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        val from = CURRENCIES.find { it.code == fromCode } ?: return amount
        val to = CURRENCIES.find { it.code == toCode } ?: return amount
        // amount in USD = amount / from.rateAgainstUsd
        val inUsd = amount / from.rateAgainstUsd
        return inUsd * to.rateAgainstUsd
    }
}
