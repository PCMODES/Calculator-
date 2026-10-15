package com.example

import com.example.calculator.ConversionUnit
import com.example.calculator.ExpressionEvaluator
import com.example.calculator.UnitCategory
import com.example.calculator.UnitConversionEngine
import com.example.data.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    private val evaluator = ExpressionEvaluator()

    @Test
    fun standardCalculator_operatorPrecedence() {
        // 125 + 75 × 2 = 275
        val result1 = evaluator.evaluate("125 + 75 × 2")
        assertTrue(result1 is ExpressionEvaluator.Result.Success)
        assertEquals(275.0, (result1 as ExpressionEvaluator.Result.Success).value, 0.001)

        // 100 ÷ 4 = 25
        val result2 = evaluator.evaluate("100 ÷ 4")
        assertTrue(result2 is ExpressionEvaluator.Result.Success)
        assertEquals(25.0, (result2 as ExpressionEvaluator.Result.Success).value, 0.001)
    }

    @Test
    fun scientificCalculator_functions() {
        // sqrt(144) = 12
        val sqrtRes = evaluator.evaluate("√(144)")
        assertTrue(sqrtRes is ExpressionEvaluator.Result.Success)
        assertEquals(12.0, (sqrtRes as ExpressionEvaluator.Result.Success).value, 0.001)

        // sin(30) in DEG mode = 0.5
        val sinRes = evaluator.evaluate("sin(30)", angleMode = AngleMode.DEG)
        assertTrue(sinRes is ExpressionEvaluator.Result.Success)
        assertEquals(0.5, (sinRes as ExpressionEvaluator.Result.Success).value, 0.001)
    }

    @Test
    fun taraju_weightToPriceCalculation() {
        // Rate = ₹800/kg, Weight = 250 g -> Price = ₹200.00
        val ratePerKg = 800.0
        val weightGrams = 250.0
        val weightKg = weightGrams / 1000.0
        val totalPrice = ratePerKg * weightKg
        assertEquals(0.250, weightKg, 0.0001)
        assertEquals(200.0, totalPrice, 0.01)
    }

    @Test
    fun taraju_moneyToWeightCalculation() {
        // Rate = ₹40/kg, Amount = ₹15 -> Weight = 375 g (0.375 kg)
        val ratePerKg = 40.0
        val purchaseAmount = 15.0
        val weightKg = purchaseAmount / ratePerKg
        val weightGrams = weightKg * 1000.0
        assertEquals(0.375, weightKg, 0.0001)
        assertEquals(375.0, weightGrams, 0.01)
    }

    @Test
    fun discountCalculation() {
        // Price = 1000, Discount = 20% -> Discount Amount = 200, Final Price = 800
        val price = 1000.0
        val discountPercent = 20.0
        val discountAmount = price * (discountPercent / 100.0)
        val finalPrice = price - discountAmount
        assertEquals(200.0, discountAmount, 0.01)
        assertEquals(800.0, finalPrice, 0.01)
    }

    @Test
    fun gstCalculation() {
        // Amount = 1000, GST = 18% -> GST = 180, Total = 1180
        val amount = 1000.0
        val rate = 18.0
        val gstAmount = amount * (rate / 100.0)
        val totalAmount = amount + gstAmount
        assertEquals(180.0, gstAmount, 0.01)
        assertEquals(1180.0, totalAmount, 0.01)
    }

    @Test
    fun profitAndLossCalculation() {
        // CP = 800, SP = 1000 -> Profit = 200, Profit % = 25%
        val cp = 800.0
        val sp = 1000.0
        val profit = sp - cp
        val profitPercent = (profit / cp) * 100.0
        assertEquals(200.0, profit, 0.01)
        assertEquals(25.0, profitPercent, 0.01)
    }

    @Test
    fun unitConversions() {
        val meter = ConversionUnit("Meter", "m", 1.0)
        val cm = ConversionUnit("Centimeter", "cm", 0.01)
        val converted = UnitConversionEngine.convert(1.0, meter, cm, UnitCategory.LENGTH)
        assertEquals(100.0, converted, 0.01)

        val celsius = ConversionUnit("Celsius", "°C", 1.0)
        val fahrenheit = ConversionUnit("Fahrenheit", "°F", 1.0)
        val tempConverted = UnitConversionEngine.convert(0.0, celsius, fahrenheit, UnitCategory.TEMPERATURE)
        assertEquals(32.0, tempConverted, 0.01)
    }
}
