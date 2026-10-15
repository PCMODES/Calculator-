package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.HistoryRepository
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SipCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var monthlyInvestmentStr by remember { mutableStateOf("") }
    var expectedReturnStr by remember { mutableStateOf("") }
    var durationYearsStr by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val monthlyInvestment = monthlyInvestmentStr.toDoubleOrNull() ?: 0.0
    val expectedReturn = expectedReturnStr.toDoubleOrNull() ?: 0.0
    val durationYears = durationYearsStr.toDoubleOrNull() ?: 0.0

    val (investedAmount, estimatedReturns, totalValue) = remember(monthlyInvestment, expectedReturn, durationYears) {
        if (monthlyInvestment > 0 && expectedReturn > 0 && durationYears > 0) {
            val n = durationYears * 12
            val i = (expectedReturn / 12.0) / 100.0
            // M = P * ({[1 + i]^n - 1} / i) * (1 + i)
            val total = monthlyInvestment * (((1 + i).pow(n) - 1) / i) * (1 + i)
            val invested = monthlyInvestment * n
            val returns = total - invested
            Triple(invested, returns, total)
        } else {
            Triple(0.0, 0.0, 0.0)
        }
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    fun share() {
        val text = """
            📊 SIP Investment Projection
            Monthly Deposit: ₹ ${format(monthlyInvestment)}
            Expected Return: $expectedReturn%
            Duration: $durationYears Years
            
            Invested Amount: ₹ ${format(investedAmount)}
            Est. Wealth Gain: ₹ ${format(estimatedReturns)}
            Total Future Value: ₹ ${format(totalValue)}
            Calculated via Smart Calculator
        """.trimIndent()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share SIP Projection"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SIP Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = ::share) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "SIP: ₹${format(monthlyInvestment)}/mo @ $expectedReturn% for $durationYears Yrs"
                            val res = "Value: ₹${format(totalValue)} (Gain: ₹${format(estimatedReturns)})"
                            historyRepository.insert(expr, res, "SIP Calculator")
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
                        value = monthlyInvestmentStr,
                        onValueChange = { monthlyInvestmentStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Monthly Investment (₹)") },
                        placeholder = { Text("उदा. 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = expectedReturnStr,
                        onValueChange = { expectedReturnStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Expected Annual Return Rate (%)") },
                        placeholder = { Text("उदा. 12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = durationYearsStr,
                        onValueChange = { durationYearsStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Time Period (Years)") },
                        placeholder = { Text("उदा. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Future Value", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(totalValue),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("SIP Total", format(totalValue)))
                            snackbarMessage = "Total copied"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Invested Amount", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(investedAmount), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Returns", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(estimatedReturns), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }

                    if (totalValue > 0) {
                        val ratio = (investedAmount / totalValue).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.secondary
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("■ Invested (${String.format(Locale.US, "%.0f%%", ratio * 100)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text("■ Wealth Gain (${String.format(Locale.US, "%.0f%%", (1 - ratio) * 100)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdRdCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isFdMode by remember { mutableStateOf(true) }
    var principalStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("") }
    var tenureYearsStr by remember { mutableStateOf("") }
    var compoundingFreq by remember { mutableIntStateOf(4) } // 4 = Quarterly, 12 = Monthly, 1 = Annually
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val p = principalStr.toDoubleOrNull() ?: 0.0
    val r = (interestRateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val t = tenureYearsStr.toDoubleOrNull() ?: 0.0

    val (totalInvested, interestEarned, maturityAmount) = remember(isFdMode, p, r, t, compoundingFreq) {
        if (p > 0 && r > 0 && t > 0) {
            if (isFdMode) {
                // FD: A = P * (1 + r/n)^(n*t)
                val n = compoundingFreq.toDouble()
                val maturity = p * (1 + r / n).pow(n * t)
                val interest = maturity - p
                Triple(p, interest, maturity)
            } else {
                // RD: Monthly deposit P. Total months = t * 12
                val months = (t * 12).toInt()
                val i = r / 12.0
                // M = P * (( (1+i)^months - 1 ) / i) * (1 + i)
                val maturity = p * (((1 + i).pow(months.toDouble()) - 1) / i) * (1 + i)
                val invested = p * months
                val interest = maturity - invested
                Triple(invested, interest, maturity)
            }
        } else {
            Triple(0.0, 0.0, 0.0)
        }
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isFdMode) "FD Calculator" else "RD Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "${if (isFdMode) "FD" else "RD"}: ₹${format(p)} @ ${interestRateStr}% for $t Yrs"
                            val res = "Maturity: ₹${format(maturityAmount)} (Interest: ₹${format(interestEarned)})"
                            historyRepository.insert(expr, res, if (isFdMode) "FD Calculator" else "RD Calculator")
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
                SegmentedButton(selected = isFdMode, onClick = { isFdMode = true }, shape = SegmentedButtonDefaults.itemShape(0, 2)) {
                    Text("Fixed Deposit (FD)")
                }
                SegmentedButton(selected = !isFdMode, onClick = { isFdMode = false }, shape = SegmentedButtonDefaults.itemShape(1, 2)) {
                    Text("Recurring Deposit (RD)")
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(if (isFdMode) "Total Deposit Amount (₹)" else "Monthly Deposit (₹)") },
                        placeholder = { Text("उदा. 1,00,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Interest Rate (% per annum)") },
                        placeholder = { Text("उदा. 7.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tenureYearsStr,
                        onValueChange = { tenureYearsStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Tenure (Years)") },
                        placeholder = { Text("उदा. 3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isFdMode) {
                        Text("Compounding Frequency", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = compoundingFreq == 4, onClick = { compoundingFreq = 4 }, label = { Text("Quarterly") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = compoundingFreq == 12, onClick = { compoundingFreq = 12 }, label = { Text("Monthly") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = compoundingFreq == 1, onClick = { compoundingFreq = 1 }, label = { Text("Yearly") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Maturity Amount", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(maturityAmount),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Maturity", format(maturityAmount)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Deposit", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(totalInvested), fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Interest Earned", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(interestEarned), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isCompoundMode by remember { mutableStateOf(false) }
    var principalStr by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("") }
    var timeYearsStr by remember { mutableStateOf("") }
    var compoundFreq by remember { mutableIntStateOf(1) } // 1=Annual, 2=Semi, 4=Quarterly, 12=Monthly
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val p = principalStr.toDoubleOrNull() ?: 0.0
    val r = (rateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val t = timeYearsStr.toDoubleOrNull() ?: 0.0

    val (interestAmount, finalAmount) = remember(isCompoundMode, p, r, t, compoundFreq) {
        if (p > 0 && r > 0 && t > 0) {
            if (!isCompoundMode) {
                // Simple Interest: I = P * r * t
                val interest = p * r * t
                Pair(interest, p + interest)
            } else {
                // Compound Interest: A = P * (1 + r/n)^(n*t)
                val n = compoundFreq.toDouble()
                val total = p * (1 + r / n).pow(n * t)
                Pair(total - p, total)
            }
        } else {
            Pair(0.0, 0.0)
        }
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCompoundMode) "Compound Interest" else "Simple Interest", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "${if (isCompoundMode) "CI" else "SI"}: ₹${format(p)} @ ${rateStr}% for $t Yrs"
                            val res = "Interest: ₹${format(interestAmount)} | Total: ₹${format(finalAmount)}"
                            historyRepository.insert(expr, res, "Interest Calculator")
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
                SegmentedButton(selected = !isCompoundMode, onClick = { isCompoundMode = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) {
                    Text("Simple Interest")
                }
                SegmentedButton(selected = isCompoundMode, onClick = { isCompoundMode = true }, shape = SegmentedButtonDefaults.itemShape(1, 2)) {
                    Text("Compound Interest")
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Principal Amount (₹)") },
                        placeholder = { Text("उदा. 50,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Annual Interest Rate (%)") },
                        placeholder = { Text("उदा. 6.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = timeYearsStr,
                        onValueChange = { timeYearsStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Time Period (Years)") },
                        placeholder = { Text("उदा. 2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isCompoundMode) {
                        Text("Compounding Interval", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = compoundFreq == 1, onClick = { compoundFreq = 1 }, label = { Text("Annually") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = compoundFreq == 4, onClick = { compoundFreq = 4 }, label = { Text("Quarterly") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = compoundFreq == 12, onClick = { compoundFreq = 12 }, label = { Text("Monthly") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Amount Payable", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(finalAmount),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Total Amount", format(finalAmount)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Interest Earned", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(interestAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Principal Borrowed/Invested", style = MaterialTheme.typography.bodySmall)
                        Text("₹ " + format(p), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
