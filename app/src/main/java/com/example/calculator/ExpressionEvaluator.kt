package com.example.calculator

import com.example.data.AngleMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class ExpressionEvaluator {

    sealed class Result {
        data class Success(val value: Double, val formatted: String) : Result()
        data class Error(val message: String) : Result()
    }

    fun evaluate(
        rawExpression: String,
        angleMode: AngleMode = AngleMode.DEG,
        precision: Int = 4,
        useThousandsSeparator: Boolean = true
    ): Result {
        if (rawExpression.isBlank()) {
            return Result.Success(0.0, "0")
        }

        return try {
            val sanitized = sanitize(rawExpression)
            val tokens = tokenize(sanitized)
            val postfix = infixToPostfix(tokens)
            val value = evaluatePostfix(postfix, angleMode)

            if (value.isNaN()) {
                return Result.Error("Calculation Error")
            }
            if (value.isInfinite()) {
                return Result.Error(if (value > 0) "Infinity" else "-Infinity")
            }

            val formatted = formatResult(value, precision, useThousandsSeparator)
            Result.Success(value, formatted)
        } catch (e: ArithmeticException) {
            Result.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            Result.Error(e.message ?: "Invalid syntax")
        } catch (e: Exception) {
            Result.Error("Syntax error")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "(${Math.PI})")
            .replace("e", "(${Math.E})")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            // Number (digits and decimal point)
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            // Word / function names
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < len && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            // Unary minus detection: e.g. "-5" at start, or after "(", "*", "/", "+", "-"
            if (c == '-') {
                val isUnary = tokens.isEmpty() || tokens.last() in listOf("(", "+", "-", "*", "/", "^", "%")
                if (isUnary) {
                    // We can represent unary minus as "neg" function or attach to the next number
                    tokens.add("neg")
                    i++
                    continue
                }
            }

            // Operators and brackets
            if (c in "+-*/^%()!") {
                tokens.add(c.toString())
                i++
                continue
            }

            // Skip unknown char
            i++
        }

        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/", "%" -> 2
            "^" -> 3
            "neg" -> 4
            "!" -> 5
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "log", "ln", "sqrt", "cbrt", "abs" -> 6
            else -> 0
        }
    }

    private fun isFunction(token: String): Boolean {
        return token in listOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "log", "ln", "sqrt", "cbrt", "abs", "neg"
        )
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                output.add(token)
            } else if (isFunction(token)) {
                stack.addLast(token)
            } else if (token == "(") {
                stack.addLast(token)
            } else if (token == ")") {
                while (stack.isNotEmpty() && stack.last() != "(") {
                    output.add(stack.removeLast())
                }
                if (stack.isNotEmpty() && stack.last() == "(") {
                    stack.removeLast()
                }
                if (stack.isNotEmpty() && isFunction(stack.last())) {
                    output.add(stack.removeLast())
                }
            } else if (token == "!") {
                // Postfix unary operator
                output.add(token)
            } else {
                // Standard binary operators (+, -, *, /, ^, %)
                while (stack.isNotEmpty() && stack.last() != "(" &&
                    (precedence(stack.last()) > precedence(token) ||
                            (precedence(stack.last()) == precedence(token) && token != "^"))
                ) {
                    output.add(stack.removeLast())
                }
                stack.addLast(token)
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top != "(") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<String>, angleMode: AngleMode): Double {
        val stack = ArrayDeque<Double>()

        fun toAngle(v: Double): Double = when (angleMode) {
            AngleMode.DEG -> Math.toRadians(v)
            AngleMode.RAD -> v
            AngleMode.GRAD -> v * (Math.PI / 200.0)
        }

        fun fromAngle(rad: Double): Double = when (angleMode) {
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.RAD -> rad
            AngleMode.GRAD -> rad * (200.0 / Math.PI)
        }

        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(num)
                continue
            }

            when (token) {
                "+" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a + b)
                }
                "-" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a - b)
                }
                "*" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a * b)
                }
                "/" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (b == 0.0) throw ArithmeticException("Cannot divide by 0")
                    stack.addLast(a / b)
                }
                "%" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a * (b / 100.0))
                }
                "^" -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(a.pow(b))
                }
                "neg" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(-a)
                }
                "!" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a < 0 || a != floor(a)) throw IllegalArgumentException("Factorial of integer required")
                    if (a > 170) throw ArithmeticException("Overflow")
                    var fact = 1.0
                    for (k in 2..a.toInt()) {
                        fact *= k
                    }
                    stack.addLast(fact)
                }
                "sin" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(sin(toAngle(a)))
                }
                "cos" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cos(toAngle(a)))
                }
                "tan" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    val rad = toAngle(a)
                    if (abs(cos(rad)) < 1e-12) throw ArithmeticException("Undefined tan")
                    stack.addLast(tan(rad))
                }
                "asin" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a < -1.0 || a > 1.0) throw IllegalArgumentException("asin domain [-1, 1]")
                    stack.addLast(fromAngle(asin(a)))
                }
                "acos" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a < -1.0 || a > 1.0) throw IllegalArgumentException("acos domain [-1, 1]")
                    stack.addLast(fromAngle(acos(a)))
                }
                "atan" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(fromAngle(atan(a)))
                }
                "sinh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(sinh(a))
                }
                "cosh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cosh(a))
                }
                "tanh" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(tanh(a))
                }
                "log" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a <= 0.0) throw IllegalArgumentException("log requires x > 0")
                    stack.addLast(log10(a))
                }
                "ln" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a <= 0.0) throw IllegalArgumentException("ln requires x > 0")
                    stack.addLast(ln(a))
                }
                "sqrt" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    if (a < 0.0) throw IllegalArgumentException("sqrt requires x ≥ 0")
                    stack.addLast(sqrt(a))
                }
                "cbrt" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(cbrt(a))
                }
                "abs" -> {
                    val a = stack.removeLastOrNull() ?: 0.0
                    stack.addLast(abs(a))
                }
                else -> throw IllegalArgumentException("Unknown operator: $token")
            }
        }

        return stack.lastOrNull() ?: 0.0
    }

    fun formatResult(value: Double, precision: Int = 4, useThousandsSeparator: Boolean = true): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // For whole numbers within reasonable magnitude
        if (value == floor(value) && abs(value) < 1e12) {
            val longVal = value.toLong()
            return if (useThousandsSeparator) {
                String.format(Locale.US, "%,d", longVal)
            } else {
                longVal.toString()
            }
        }

        // Decimal formatting
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = if (useThousandsSeparator) ',' else '\u0000'
        }

        val pattern = if (useThousandsSeparator) "#,##0." + "#".repeat(precision) else "0." + "#".repeat(precision)
        val df = DecimalFormat(pattern, symbols)
        df.roundingMode = RoundingMode.HALF_UP

        val res = df.format(BigDecimal.valueOf(value))
        return if (res.endsWith(".")) res.substring(0, res.length - 1) else res
    }
}
