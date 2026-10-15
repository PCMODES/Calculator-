package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.pow

data class AmortizationItem(
    val month: Int,
    val principalPaid: Double,
    val interestPaid: Double,
    val totalPaid: Double,
    val balance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loanAmountStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("") }
    var tenureStr by remember { mutableStateOf("") }
    var isTenureInYears by remember { mutableStateOf(true) }

    var showAmortization by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val loanAmount = loanAmountStr.toDoubleOrNull() ?: 0.0
    val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0
    val tenureVal = tenureStr.toDoubleOrNull() ?: 0.0

    val tenureMonths = if (isTenureInYears) (tenureVal * 12).toInt() else tenureVal.toInt()

    val (monthlyEmi, totalInterest, totalPayment, schedule) = remember(loanAmount, interestRate, tenureMonths) {
        if (loanAmount > 0 && interestRate == 0.0 && tenureMonths > 0) {
            val emi = loanAmount / tenureMonths
            listOf(emi, 0.0, loanAmount, emptyList<AmortizationItem>())
        } else if (loanAmount > 0 && interestRate > 0 && tenureMonths > 0) {
            val monthlyRate = (interestRate / 12.0) / 100.0
            val emi = loanAmount * monthlyRate * (1 + monthlyRate).pow(tenureMonths.toDouble()) /
                    ((1 + monthlyRate).pow(tenureMonths.toDouble()) - 1)

            val totalPay = emi * tenureMonths
            val totalInt = totalPay - loanAmount

            // Generate Amortization schedule
            val list = mutableListOf<AmortizationItem>()
            var balance = loanAmount
            for (m in 1..tenureMonths) {
                val interestPart = balance * monthlyRate
                val principalPart = emi - interestPart
                balance -= principalPart
                if (balance < 0) balance = 0.0
                list.add(
                    AmortizationItem(
                        month = m,
                        principalPaid = principalPart,
                        interestPaid = interestPart,
                        totalPaid = emi,
                        balance = balance
                    )
                )
            }
            listOf(emi, totalInt, totalPay, list)
        } else {
            listOf(0.0, 0.0, 0.0, emptyList<AmortizationItem>())
        }
    }

    val emiValue = monthlyEmi as Double
    val totalIntValue = totalInterest as Double
    val totalPayValue = totalPayment as Double
    @Suppress("UNCHECKED_CAST")
    val scheduleList = schedule as List<AmortizationItem>

    fun formatCurr(v: Double): String {
        return String.format(Locale.US, "%,.2f", v)
    }

    fun shareCalculation() {
        val shareText = """
            📊 EMI Calculation Summary
            --------------------------
            Loan Amount: ₹ ${formatCurr(loanAmount)}
            Interest Rate: $interestRate%
            Tenure: $tenureVal ${if (isTenureInYears) "Years" else "Months"}
            
            Monthly EMI: ₹ ${formatCurr(emiValue)}
            Total Interest: ₹ ${formatCurr(totalIntValue)}
            Total Payment: ₹ ${formatCurr(totalPayValue)}
            --------------------------
            Calculated via Smart Calculator
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share EMI Details"))
    }

    fun copyEmi() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("EMI", formatCurr(emiValue))
        clipboard.setPrimaryClip(clip)
        snackbarMessage = "Monthly EMI copied"
    }

    fun saveCalculation() {
        coroutineScope.launch {
            val expr = "Loan: ₹${formatCurr(loanAmount)} @ $interestRate% for $tenureVal ${if (isTenureInYears) "Yr" else "Mo"}"
            val res = "EMI: ₹${formatCurr(emiValue)} | Total: ₹${formatCurr(totalPayValue)}"
            historyRepository.insert(expr, res, "EMI Calculator")
            snackbarMessage = "Saved to calculation history"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = ::shareCalculation) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = ::saveCalculation) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save to History")
                    }
                }
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) { Text("OK") }
                    }
                ) {
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
            // INPUT CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Loan Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    // Loan Amount
                    OutlinedTextField(
                        value = loanAmountStr,
                        onValueChange = { loanAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Loan Amount (₹)") },
                        placeholder = { Text("उदा. 5,00,000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) }
                    )

                    // Interest Rate
                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Interest Rate (% per annum)") },
                        placeholder = { Text("उदा. 8.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) }
                    )

                    // Tenure + Units
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tenureStr,
                            onValueChange = { tenureStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Tenure") },
                            placeholder = { Text("उदा. 5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Toggle Months / Years
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                        ) {
                            FilterChip(
                                selected = isTenureInYears,
                                onClick = { isTenureInYears = true },
                                label = { Text("Yr") }
                            )
                            Spacer(Modifier.width(4.dp))
                            FilterChip(
                                selected = !isTenureInYears,
                                onClick = { isTenureInYears = false },
                                label = { Text("Mo") }
                            )
                        }
                    }

                    // Reset & Calculate actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                loanAmountStr = ""
                                interestRateStr = ""
                                tenureStr = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset")
                        }
                        Button(
                            onClick = ::saveCalculation,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                }
            }

            // RESULTS CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monthly EMI", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "₹ " + formatCurr(emiValue),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        IconButton(onClick = ::copyEmi) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy EMI")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Summary breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Principal Amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ " + formatCurr(loanAmount), fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Interest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ " + formatCurr(totalIntValue), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Payment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ " + formatCurr(totalPayValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val interestShare = if (totalPayValue > 0) (totalIntValue / totalPayValue) * 100 else 0.0
                            Text("Interest Ratio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.US, "%.1f%%", interestShare), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Visual Progress indicator (Principal vs Interest)
                    if (totalPayValue > 0) {
                        val principalRatio = (loanAmount / totalPayValue).toFloat().coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { principalRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.tertiary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("■ Principal (${String.format(Locale.US, "%.0f%%", principalRatio * 100)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text("■ Interest (${String.format(Locale.US, "%.0f%%", (1 - principalRatio) * 100)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }

            // AMORTIZATION SCHEDULE TOGGLE
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                onClick = { showAmortization = !showAmortization },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Amortization Schedule", fontWeight = FontWeight.SemiBold)
                            Text("${scheduleList.size} Monthly breakdowns", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(
                        imageVector = if (showAmortization) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Amortization Table List
            AnimatedVisibility(visible = showAmortization) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mo", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Principal", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                            Text("Interest", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                            Text("Balance", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                        }

                        Spacer(Modifier.height(4.dp))

                        // Show first 24 months if list is large or preview
                        val previewItems = scheduleList.take(36)
                        previewItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${item.month}", modifier = Modifier.weight(0.7f), fontSize = 12.sp)
                                Text(formatCurr(item.principalPaid), modifier = Modifier.weight(1.3f), fontSize = 12.sp, textAlign = TextAlign.End)
                                Text(formatCurr(item.interestPaid), modifier = Modifier.weight(1.3f), fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.tertiary)
                                Text(formatCurr(item.balance), modifier = Modifier.weight(1.5f), fontSize = 12.sp, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }

                        if (scheduleList.size > 36) {
                            Text(
                                text = "... and ${scheduleList.size - 36} more months",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
