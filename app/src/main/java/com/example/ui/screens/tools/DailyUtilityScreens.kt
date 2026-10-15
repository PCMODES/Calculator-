package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

// 1. Tip & Split Calculator
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipSplitCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var billAmountStr by remember { mutableStateOf("") }
    var tipPercentStr by remember { mutableStateOf("10") }
    var splitPeopleCount by remember { mutableIntStateOf(3) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val bill = billAmountStr.toDoubleOrNull() ?: 0.0
    val tipP = tipPercentStr.toDoubleOrNull() ?: 0.0

    val (tipAmount, totalBill, perPerson) = remember(bill, tipP, splitPeopleCount) {
        val tip = bill * (tipP / 100.0)
        val total = bill + tip
        val perP = if (splitPeopleCount > 0) total / splitPeopleCount else total
        Triple(tip, total, perP)
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tip & Bill Split", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "Bill: ₹${format(bill)} + $tipP% Tip split by $splitPeopleCount"
                            val res = "Per person: ₹${format(perPerson)} | Total: ₹${format(totalBill)}"
                            historyRepository.insert(expr, res, "Tip & Split")
                            snackbarMessage = "Saved to history"
                        }
                    }) { Icon(Icons.Default.BookmarkBorder, contentDescription = "Save") }
                }
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(modifier = Modifier.padding(16.dp), action = { TextButton(onClick = { snackbarMessage = null }) { Text("OK") } }) {
                    Text(snackbarMessage ?: "")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = billAmountStr,
                        onValueChange = { billAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Total Bill Amount (₹)") },
                        placeholder = { Text("उदा. 1500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tip Percentage", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 15, 20).forEach { p ->
                            FilterChip(
                                selected = tipPercentStr == p.toString(),
                                onClick = { tipPercentStr = p.toString() },
                                label = { Text("$p%") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tipPercentStr,
                        onValueChange = { tipPercentStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Custom Tip %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // People Split Counter
                    Text("Split Among People", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$splitPeopleCount Person${if (splitPeopleCount > 1) "s" else ""}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalIconButton(onClick = { if (splitPeopleCount > 1) splitPeopleCount-- }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            FilledTonalIconButton(onClick = { splitPeopleCount++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }

            // Result
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Per Person Share", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "₹ " + format(perPerson),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Share", format(perPerson)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Tip", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(tipAmount), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Bill (with Tip)", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(totalBill), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// 2. Percentage Calculator (X% of Y, What % is X of Y, % Increase/Decrease)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PercentageCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var modeIndex by remember { mutableIntStateOf(0) } // 0: Percentage, 1: Increase, 2: Decrease, 3: Difference

    var val1Str by remember { mutableStateOf("") }
    var val2Str by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val v1 = val1Str.toDoubleOrNull() ?: 0.0
    val v2 = val2Str.toDoubleOrNull() ?: 0.0

    val (resultText, formulaDesc) = remember(modeIndex, v1, v2) {
        when (modeIndex) {
            0 -> {
                // v2 % of v1 (e.g. 20% of 500 = 100)
                val res = (v2 / 100.0) * v1
                Pair(String.format(Locale.US, "%,.2f", res), "$v2% of $v1")
            }
            1 -> {
                // Percentage Increase from v1 to v2
                val diff = v2 - v1
                val res = if (v1 != 0.0) (diff / v1) * 100.0 else 0.0
                Pair(String.format(Locale.US, "+%.2f%%", kotlin.math.max(0.0, res)), "Increase from $v1 to $v2")
            }
            2 -> {
                // Percentage Decrease from v1 to v2
                val diff = v1 - v2
                val res = if (v1 != 0.0) (diff / v1) * 100.0 else 0.0
                Pair(String.format(Locale.US, "-%.2f%%", kotlin.math.max(0.0, res)), "Decrease from $v1 to $v2")
            }
            else -> {
                // Percentage Difference |v1 - v2| / ((v1 + v2)/2) * 100
                val avg = (v1 + v2) / 2.0
                val res = if (avg != 0.0) (kotlin.math.abs(v1 - v2) / avg) * 100.0 else 0.0
                Pair(String.format(Locale.US, "%.2f%%", res), "Difference between $v1 and $v2")
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Percentage Result", text)
        clipboard.setPrimaryClip(clip)
        snackbarMessage = "Copied: $text"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Percentage Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            historyRepository.insert(formulaDesc, resultText, "Percentage")
                            snackbarMessage = "Saved to history"
                        }
                    }) { Icon(Icons.Default.BookmarkBorder, contentDescription = "Save") }
                }
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(modifier = Modifier.padding(16.dp), action = { TextButton(onClick = { snackbarMessage = null }) { Text("OK") } }) {
                    Text(snackbarMessage ?: "")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = modeIndex == 0, onClick = { modeIndex = 0 }, shape = SegmentedButtonDefaults.itemShape(0, 4)) {
                    Text("Percent", fontSize = 12.sp)
                }
                SegmentedButton(selected = modeIndex == 1, onClick = { modeIndex = 1 }, shape = SegmentedButtonDefaults.itemShape(1, 4)) {
                    Text("Increase", fontSize = 12.sp)
                }
                SegmentedButton(selected = modeIndex == 2, onClick = { modeIndex = 2 }, shape = SegmentedButtonDefaults.itemShape(2, 4)) {
                    Text("Decrease", fontSize = 12.sp)
                }
                SegmentedButton(selected = modeIndex == 3, onClick = { modeIndex = 3 }, shape = SegmentedButtonDefaults.itemShape(3, 4)) {
                    Text("Diff", fontSize = 12.sp)
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = val1Str,
                        onValueChange = { val1Str = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                        label = {
                            Text(
                                when (modeIndex) {
                                    0 -> "Base Number"
                                    1 -> "Original Value"
                                    2 -> "Original Value"
                                    else -> "Value 1"
                                }
                            )
                        },
                        placeholder = { Text("उदा. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = val2Str,
                        onValueChange = { val2Str = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                        label = {
                            Text(
                                when (modeIndex) {
                                    0 -> "Percentage (%)"
                                    1 -> "New Higher Value"
                                    2 -> "New Lower Value"
                                    else -> "Value 2"
                                }
                            )
                        },
                        placeholder = { Text("उदा. 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Neumorphic Result Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Result: $formulaDesc", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    historyRepository.insert(formulaDesc, resultText, "Percentage")
                                    snackbarMessage = "Calculated & Saved to history!"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Calculate", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val1Str = ""
                                val2Str = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = { copyToClipboard(resultText) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }
                }
            }
        }
    }
}

data class StatisticsResult(
    val mean: Double,
    val median: Double,
    val mode: String,
    val min: Double,
    val max: Double,
    val sum: Double,
    val count: Int
)

// 3. Statistics & Average Calculator
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawInput by remember { mutableStateOf("12, 45, 67, 23, 89, 45, 90") }

    val numbers = remember(rawInput) {
        rawInput.split(',', ' ', '\n')
            .mapNotNull { it.trim().toDoubleOrNull() }
    }

    val stats = remember(numbers) {
        if (numbers.isNotEmpty()) {
            val c = numbers.size
            val s = numbers.sum()
            val avg = s / c
            val sorted = numbers.sorted()
            val med = if (c % 2 == 1) sorted[c / 2] else (sorted[c / 2 - 1] + sorted[c / 2]) / 2.0
            val mn = sorted.first()
            val mx = sorted.last()

            // Mode
            val freqMap = numbers.groupingBy { it }.eachCount()
            val maxFreq = freqMap.values.maxOrNull() ?: 1
            val modes = freqMap.filter { it.value == maxFreq && maxFreq > 1 }.keys.toList()
            val modeStr = if (modes.isEmpty()) "None" else modes.joinToString(", ") { String.format(Locale.US, "%.1f", it) }

            StatisticsResult(
                mean = avg,
                median = med,
                mode = modeStr,
                min = mn,
                max = mx,
                sum = s,
                count = c
            )
        } else {
            StatisticsResult(
                mean = 0.0,
                median = 0.0,
                mode = "None",
                min = 0.0,
                max = 0.0,
                sum = 0.0,
                count = 0
            )
        }
    }

    fun fmt(v: Double) = String.format(Locale.US, "%,.2f", v)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Average & Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter numbers separated by comma or space", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("Data points") }
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Mean (Average)", style = MaterialTheme.typography.bodyMedium)
                            Text(fmt(stats.mean), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Count", style = MaterialTheme.typography.bodyMedium)
                            Text("${stats.count} items", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Median", style = MaterialTheme.typography.bodyMedium)
                        Text(fmt(stats.median), fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mode", style = MaterialTheme.typography.bodyMedium)
                        Text(stats.mode, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Minimum", style = MaterialTheme.typography.bodyMedium)
                        Text(fmt(stats.min), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Maximum", style = MaterialTheme.typography.bodyMedium)
                        Text(fmt(stats.max), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sum Total", style = MaterialTheme.typography.bodyMedium)
                        Text(fmt(stats.sum), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// 4. Random Generator (Dice, Coin, Min-Max range)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGeneratorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var minStr by remember { mutableStateOf("1") }
    var maxStr by remember { mutableStateOf("100") }
    var randomResult by remember { mutableStateOf<String?>(null) }
    var diceResult by remember { mutableStateOf(6) }
    var coinResult by remember { mutableStateOf("Heads") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Number range
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Random Number Picker", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minStr,
                            onValueChange = { minStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxStr,
                            onValueChange = { maxStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val mn = minStr.toIntOrNull() ?: 1
                            val mx = maxStr.toIntOrNull() ?: 100
                            if (mx >= mn) {
                                randomResult = Random.nextInt(mn, mx + 1).toString()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Random Number")
                    }

                    if (randomResult != null) {
                        Text(
                            text = randomResult ?: "",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Dice Roll and Coin Flip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Dice Roll", fontWeight = FontWeight.SemiBold)
                        Text("🎲 $diceResult", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { diceResult = Random.nextInt(1, 7) }) {
                            Text("Roll")
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Coin Flip", fontWeight = FontWeight.SemiBold)
                        Text(if (coinResult == "Heads") "🪙 Heads" else "🪙 Tails", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { coinResult = if (Random.nextBoolean()) "Heads" else "Tails" }) {
                            Text("Flip")
                        }
                    }
                }
            }
        }
    }
}
