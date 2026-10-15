package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ExpressionEvaluator
import com.example.data.AngleMode
import com.example.data.HistoryRepository
import com.example.data.PreferencesManager
import com.example.ui.components.CalcButton
import com.example.ui.components.CalcButtonType
import kotlinx.coroutines.launch

@Composable
fun MainCalculatorScreen(
    historyRepository: HistoryRepository,
    preferencesManager: PreferencesManager,
    onNavigateToHistory: () -> Unit,
    onNavigateToTaraju: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialExpression: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hapticEnabled by preferencesManager.hapticFeedback.collectAsState()
    val soundEnabled by preferencesManager.soundFeedback.collectAsState()
    val precision by preferencesManager.decimalPrecision.collectAsState()
    val useThousands by preferencesManager.thousandsSeparator.collectAsState()
    val autoSave by preferencesManager.autoSaveCalculations.collectAsState()
    val defaultAngleMode by preferencesManager.defaultAngleMode.collectAsState()

    var expression by remember { mutableStateOf(initialExpression ?: "") }
    var previousCalculation by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var isScientificExpanded by remember { mutableStateOf(false) }
    var activeAngleMode by remember { mutableStateOf(defaultAngleMode) }
    var memoryValue by remember { mutableDoubleStateOf(0.0) }
    var copyMessage by remember { mutableStateOf<String?>(null) }
    var isInverseTrig by remember { mutableStateOf(false) }

    val evaluator = remember { ExpressionEvaluator() }
    val exprScrollState = rememberScrollState()

    // Auto-scroll expression display when updated
    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
        if (expression.isNotBlank()) {
            val eval = evaluator.evaluate(expression, activeAngleMode, precision, useThousands)
            if (eval is ExpressionEvaluator.Result.Success) {
                resultText = eval.formatted
            }
        } else {
            resultText = "0"
        }
    }

    // Dismiss copy message after delay
    LaunchedEffect(copyMessage) {
        if (copyMessage != null) {
            kotlinx.coroutines.delay(1800)
            copyMessage = null
        }
    }

    fun onAppend(char: String) {
        expression += char
    }

    fun onDelete() {
        if (expression.isNotEmpty()) {
            // Check if deleting function like 'sin(', 'cos(', 'sqrt(' etc.
            val funcs = listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(", "log(", "ln(", "sqrt(", "cbrt(", "abs(")
            val matchedFunc = funcs.find { expression.endsWith(it) }
            expression = if (matchedFunc != null) {
                expression.dropLast(matchedFunc.length)
            } else {
                expression.dropLast(1)
            }
        }
    }

    fun onClearAll() {
        if (expression.isNotEmpty()) {
            previousCalculation = expression + " = " + resultText
        }
        expression = ""
        resultText = "0"
    }

    fun onCalculateEquals() {
        if (expression.isBlank()) return
        val eval = evaluator.evaluate(expression, activeAngleMode, precision, useThousands)
        when (eval) {
            is ExpressionEvaluator.Result.Success -> {
                val formatted = eval.formatted
                previousCalculation = "$expression = $formatted"
                if (autoSave) {
                    coroutineScope.launch {
                        historyRepository.insert(expression, formatted, "Calculator")
                    }
                }
                expression = formatted.replace(",", "")
                resultText = formatted
            }
            is ExpressionEvaluator.Result.Error -> {
                resultText = eval.message
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", text)
        clipboard.setPrimaryClip(clip)
        copyMessage = "Copied to clipboard"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // DISPLAY SECTION (Balanced, sleek, normal calculator proportions)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isScientificExpanded) 130.dp else 160.dp)
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row of display: Angle Mode chip, Memory indicator, Copied toast & Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Angle Mode pill (DEG / RAD / GRAD)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    activeAngleMode = when (activeAngleMode) {
                                        AngleMode.DEG -> AngleMode.RAD
                                        AngleMode.RAD -> AngleMode.GRAD
                                        AngleMode.GRAD -> AngleMode.DEG
                                    }
                                }
                                .testTag("btn_angle_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = activeAngleMode.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        // Memory indicator
                        if (memoryValue != 0.0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    expression += evaluator.formatResult(memoryValue, precision, false)
                                }
                            ) {
                                Text(
                                    text = "M: ${evaluator.formatResult(memoryValue, 2, false)}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                )
                            }
                        }

                        // Copied badge notification
                        AnimatedVisibility(
                            visible = copyMessage != null,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(200))
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = copyMessage ?: "",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Display quick tools: Copy result & Quick Backspace
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(
                                onClick = { copyToClipboard(resultText) },
                                modifier = Modifier.fillMaxSize().testTag("btn_copy_result")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Result",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (expression.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                IconButton(
                                    onClick = { onDelete() },
                                    modifier = Modifier.fillMaxSize().testTag("btn_display_backspace")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Calculation content: Running Expression & Evaluated Result grouped at bottom-right
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Previous calculation note
                    if (previousCalculation.isNotBlank()) {
                        Text(
                            text = previousCalculation,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Current expression input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(exprScrollState),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.3.sp,
                                color = if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.End
                        )
                    }

                    // Evaluated result with "=" badge (Normal, clean, high-clarity sizing)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "= ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                            )
                        )
                        Text(
                            text = resultText,
                            style = when {
                                resultText.length > 14 -> MaterialTheme.typography.titleMedium
                                resultText.length > 9 -> MaterialTheme.typography.headlineSmall
                                else -> MaterialTheme.typography.headlineMedium
                            }.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = (-0.3).sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // MEMORY & SCIENTIFIC BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val memBtnModifier = Modifier.weight(1f)
            CalcButton(
                text = "MC",
                type = CalcButtonType.MEMORY,
                height = 38.dp,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                modifier = memBtnModifier
            ) {
                memoryValue = 0.0
            }
            CalcButton(
                text = "MR",
                type = CalcButtonType.MEMORY,
                height = 38.dp,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                modifier = memBtnModifier
            ) {
                expression += evaluator.formatResult(memoryValue, precision, false)
            }
            CalcButton(
                text = "M+",
                type = CalcButtonType.MEMORY,
                height = 38.dp,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                modifier = memBtnModifier
            ) {
                val eval = evaluator.evaluate(if (expression.isNotBlank()) expression else resultText, activeAngleMode)
                if (eval is ExpressionEvaluator.Result.Success) memoryValue += eval.value
            }
            CalcButton(
                text = "M-",
                type = CalcButtonType.MEMORY,
                height = 38.dp,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                modifier = memBtnModifier
            ) {
                val eval = evaluator.evaluate(if (expression.isNotBlank()) expression else resultText, activeAngleMode)
                if (eval is ExpressionEvaluator.Result.Success) memoryValue -= eval.value
            }
            // Scientific Toggle Pill
            Surface(
                modifier = Modifier
                    .weight(1.35f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (isScientificExpanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                color = if (isScientificExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                onClick = { isScientificExpanded = !isScientificExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Scientific Mode",
                        tint = if (isScientificExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isScientificExpanded) "Basic" else "fx Sci",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isScientificExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // SCIENTIFIC PANEL (Expandable)
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: 2nd / Inv, sin, cos, tan, ln, log
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sciModifier = Modifier.weight(1f)
                    CalcButton(
                        text = if (isInverseTrig) "1st" else "2nd",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { isInverseTrig = !isInverseTrig }

                    CalcButton(
                        text = if (isInverseTrig) "asin" else "sin",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { onAppend(if (isInverseTrig) "asin(" else "sin(") }

                    CalcButton(
                        text = if (isInverseTrig) "acos" else "cos",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { onAppend(if (isInverseTrig) "acos(" else "cos(") }

                    CalcButton(
                        text = if (isInverseTrig) "atan" else "tan",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { onAppend(if (isInverseTrig) "atan(" else "tan(") }

                    CalcButton(
                        text = "ln",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { onAppend("ln(") }

                    CalcButton(
                        text = "log",
                        type = CalcButtonType.SCIENTIFIC,
                        height = 42.dp,
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        modifier = sciModifier
                    ) { onAppend("log(") }
                }

                // Row 2: sinh, cosh, tanh, √, ∛, xʸ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sciModifier = Modifier.weight(1f)
                    CalcButton(text = "sinh", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("sinh(") }
                    CalcButton(text = "cosh", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("cosh(") }
                    CalcButton(text = "tanh", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("tanh(") }
                    CalcButton(text = "√", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("√(") }
                    CalcButton(text = "(", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("(") }
                    CalcButton(text = ")", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend(")") }
                }

                // Row 3: x², x³, 1/x, x!, π, e
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sciModifier = Modifier.weight(1f)
                    CalcButton(text = "x²", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("^2") }
                    CalcButton(text = "x³", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("^3") }
                    CalcButton(text = "1/x", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("^(-1)") }
                    CalcButton(text = "x!", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("!") }
                    CalcButton(text = "π", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("π") }
                    CalcButton(text = "e", type = CalcButtonType.SCIENTIFIC, height = 42.dp, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = sciModifier) { onAppend("e") }
                }
            }
        }

        // MAIN KEYPAD SECTION (5 Rows)
        val keyHeight = if (isScientificExpanded) 50.dp else 64.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, DEL, ( ), ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "AC", type = CalcButtonType.ACTION, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onClearAll() }
                CalcButton(text = "DEL", type = CalcButtonType.ACTION, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onDelete() }
                CalcButton(text = "%", type = CalcButtonType.OPERATOR, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("%") }
                CalcButton(text = "÷", type = CalcButtonType.OPERATOR, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("÷") }
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "7", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("7") }
                CalcButton(text = "8", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("8") }
                CalcButton(text = "9", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("9") }
                CalcButton(text = "×", type = CalcButtonType.OPERATOR, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("×") }
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "4", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("4") }
                CalcButton(text = "5", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("5") }
                CalcButton(text = "6", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("6") }
                CalcButton(text = "−", type = CalcButtonType.OPERATOR, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("−") }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "1", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("1") }
                CalcButton(text = "2", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("2") }
                CalcButton(text = "3", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("3") }
                CalcButton(text = "+", type = CalcButtonType.OPERATOR, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("+") }
            }

            // Row 5: 00, 0, ., = (Balanced 4-column grid aligned with all keypad rows)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(text = "00", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("00") }
                CalcButton(text = "0", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend("0") }
                CalcButton(text = ".", height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onAppend(".") }
                CalcButton(text = "=", type = CalcButtonType.EQUALS, height = keyHeight, hapticEnabled = hapticEnabled, soundEnabled = soundEnabled, modifier = Modifier.weight(1f)) { onCalculateEquals() }
            }
        }
    }
}
