package com.example.calculator

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume"),
    SPEED("Speed"),
    TIME("Time"),
    DATA("Data Storage"),
    PRESSURE("Pressure"),
    ENERGY("Energy")
}

data class ConversionUnit(
    val name: String,
    val symbol: String,
    // Factor relative to base unit (e.g. meter for length, gram for weight)
    val toBaseFactor: Double
)

object UnitConversionEngine {

    val CATEGORIES = UnitCategory.values().toList()

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> listOf(
                ConversionUnit("Millimeter", "mm", 0.001),
                ConversionUnit("Centimeter", "cm", 0.01),
                ConversionUnit("Meter", "m", 1.0),
                ConversionUnit("Kilometer", "km", 1000.0),
                ConversionUnit("Inch", "in", 0.0254),
                ConversionUnit("Foot", "ft", 0.3048),
                ConversionUnit("Yard", "yd", 0.9144),
                ConversionUnit("Mile", "mi", 1609.344),
                ConversionUnit("Nautical Mile", "NM", 1852.0)
            )

            UnitCategory.WEIGHT -> listOf(
                ConversionUnit("Milligram", "mg", 0.001),
                ConversionUnit("Gram", "g", 1.0),
                ConversionUnit("Kilogram", "kg", 1000.0),
                ConversionUnit("Metric Ton", "t", 1000000.0),
                ConversionUnit("Ounce", "oz", 28.349523),
                ConversionUnit("Pound", "lb", 453.59237)
            )

            UnitCategory.TEMPERATURE -> listOf(
                ConversionUnit("Celsius", "°C", 1.0),
                ConversionUnit("Fahrenheit", "°F", 1.0),
                ConversionUnit("Kelvin", "K", 1.0)
            )

            UnitCategory.AREA -> listOf(
                ConversionUnit("Sq Millimeter", "mm²", 0.000001),
                ConversionUnit("Sq Centimeter", "cm²", 0.0001),
                ConversionUnit("Sq Meter", "m²", 1.0),
                ConversionUnit("Sq Kilometer", "km²", 1000000.0),
                ConversionUnit("Sq Inch", "in²", 0.00064516),
                ConversionUnit("Sq Foot", "ft²", 0.092903),
                ConversionUnit("Acre", "ac", 4046.86),
                ConversionUnit("Hectare", "ha", 10000.0)
            )

            UnitCategory.VOLUME -> listOf(
                ConversionUnit("Milliliter", "ml", 0.001),
                ConversionUnit("Liter", "L", 1.0),
                ConversionUnit("Cubic Meter", "m³", 1000.0),
                ConversionUnit("Teaspoon (US)", "tsp", 0.00492892),
                ConversionUnit("Tablespoon (US)", "tbsp", 0.0147868),
                ConversionUnit("Fluid Ounce (US)", "fl oz", 0.0295735),
                ConversionUnit("Cup (US)", "cup", 0.236588),
                ConversionUnit("Pint (US)", "pt", 0.473176),
                ConversionUnit("Gallon (US)", "gal", 3.78541)
            )

            UnitCategory.SPEED -> listOf(
                ConversionUnit("Meter / sec", "m/s", 1.0),
                ConversionUnit("Kilometer / hour", "km/h", 0.277778),
                ConversionUnit("Miles / hour", "mph", 0.44704),
                ConversionUnit("Knot", "kn", 0.514444),
                ConversionUnit("Foot / sec", "ft/s", 0.3048)
            )

            UnitCategory.TIME -> listOf(
                ConversionUnit("Millisecond", "ms", 0.001),
                ConversionUnit("Second", "s", 1.0),
                ConversionUnit("Minute", "min", 60.0),
                ConversionUnit("Hour", "h", 3600.0),
                ConversionUnit("Day", "d", 86400.0),
                ConversionUnit("Week", "wk", 604800.0),
                ConversionUnit("Month (30d)", "mo", 2592000.0),
                ConversionUnit("Year (365d)", "yr", 31536000.0)
            )

            UnitCategory.DATA -> listOf(
                ConversionUnit("Bit", "b", 0.125),
                ConversionUnit("Byte", "B", 1.0),
                ConversionUnit("Kilobyte (KB)", "KB", 1024.0),
                ConversionUnit("Megabyte (MB)", "MB", 1024.0 * 1024.0),
                ConversionUnit("Gigabyte (GB)", "GB", 1024.0 * 1024.0 * 1024.0),
                ConversionUnit("Terabyte (TB)", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
            )

            UnitCategory.PRESSURE -> listOf(
                ConversionUnit("Pascal", "Pa", 1.0),
                ConversionUnit("Kilopascal", "kPa", 1000.0),
                ConversionUnit("Bar", "bar", 100000.0),
                ConversionUnit("PSI", "psi", 6894.76),
                ConversionUnit("Atmosphere", "atm", 101325.0)
            )

            UnitCategory.ENERGY -> listOf(
                ConversionUnit("Joule", "J", 1.0),
                ConversionUnit("Kilojoule", "kJ", 1000.0),
                ConversionUnit("Calorie", "cal", 4.184),
                ConversionUnit("Kilocalorie", "kcal", 4184.0),
                ConversionUnit("Watt-hour", "Wh", 3600.0),
                ConversionUnit("Kilowatt-hour", "kWh", 3600000.0)
            )
        }
    }

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit, category: UnitCategory): Double {
        if (from.symbol == to.symbol) return value

        if (category == UnitCategory.TEMPERATURE) {
            // Temperature conversion formulas
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * (5.0 / 9.0)
                "K" -> value - 273.15
                else -> value
            }

            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> (celsius * 9.0 / 5.0) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        // Standard multiplicative factor conversion
        val baseValue = value * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }
}
