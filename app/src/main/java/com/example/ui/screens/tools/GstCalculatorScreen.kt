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

enum class GstMode {
    ADD, REMOVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var amountStr by remember { mutableStateOf("") }
    var gstRateStr by remember { mutableStateOf("18") }
    var gstMode by remember { mutableStateOf(GstMode.ADD) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val presets = listOf(0.0, 5.0, 12.0, 18.0, 28.0)

    val inputAmount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = gstRateStr.toDoubleOrNull() ?: 0.0

    val (netAmount, gstAmount, totalAmount) = remember(inputAmount, rate, gstMode) {
        if (inputAmount > 0 && rate >= 0) {
            when (gstMode) {
                GstMode.ADD -> {
                    val gst = inputAmount * (rate / 100.0)
                    val total = inputAmount + gst
                    Triple(inputAmount, gst, total)
                }
                GstMode.REMOVE -> {
                    // total = net + net * rate/100 = net * (1 + rate/100)
                    val net = inputAmount / (1 + (rate / 100.0))
                    val gst = inputAmount - net
                    Triple(net, gst, inputAmount)
                }
            }
        } else {
            Triple(0.0, 0.0, 0.0)
        }
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    fun shareCalculation() {
        val text = """
            🧾 GST Calculation
            Mode: ${if (gstMode == GstMode.ADD) "GST Added" else "GST Removed"}
            Base Amount: ₹ ${format(netAmount)}
            GST Rate: $rate%
            CGST (Rate/2): ₹ ${format(gstAmount / 2.0)}
            SGST (Rate/2): ₹ ${format(gstAmount / 2.0)}
            Total GST: ₹ ${format(gstAmount)}
            Total Amount: ₹ ${format(totalAmount)}
            Calculated via Smart Calculator
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share GST Details"))
    }

    fun copyResult() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("GST Total", format(totalAmount))
        clipboard.setPrimaryClip(clip)
        snackbarMessage = "Total amount copied"
    }

    fun saveCalculation() {
        coroutineScope.launch {
            val expr = "${if (gstMode == GstMode.ADD) "Add" else "Remove"} $rate% GST on ₹${format(inputAmount)}"
            val res = "GST: ₹${format(gstAmount)} | Total: ₹${format(totalAmount)}"
            historyRepository.insert(expr, res, "GST Calculator")
            snackbarMessage = "Saved to calculation history"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GST Calculator", fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                    }
                }
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { snackbarMessage = null }) { Text("OK") } }
                ) { Text(snackbarMessage ?: "") }
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
            // MODE SELECTOR (GST ADD / GST REMOVE)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = gstMode == GstMode.ADD,
                    onClick = { gstMode = GstMode.ADD },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("GST Add (+)")
                }
                SegmentedButton(
                    selected = gstMode == GstMode.REMOVE,
                    onClick = { gstMode = GstMode.REMOVE },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("GST Remove (-)")
                }
            }

            // INPUT CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(if (gstMode == GstMode.ADD) "Initial Amount (Excl. GST)" else "Total Amount (Incl. GST)") },
                        placeholder = { Text("उदा. 1000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) }
                    )

                    Text("GST Rate Preset", style = MaterialTheme.typography.labelMedium)

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { p ->
                            val isSelected = rate == p
                            FilterChip(
                                selected = isSelected,
                                onClick = { gstRateStr = p.toInt().toString() },
                                label = { Text("${p.toInt()}%") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Custom GST Rate
                    OutlinedTextField(
                        value = gstRateStr,
                        onValueChange = { gstRateStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Custom GST Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) }
                    )
                }
            }

            // RESULTS CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Final Total Amount", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(totalAmount),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        IconButton(onClick = ::copyResult) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net / Original Amount", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(netAmount), fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total GST ($rate%)", style = MaterialTheme.typography.bodyMedium)
                        Text("₹ " + format(gstAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CGST (${rate / 2.0}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹ " + format(gstAmount / 2.0), style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SGST (${rate / 2.0}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹ " + format(gstAmount / 2.0), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val expr = "₹${format(inputAmount)} at $rate% GST (${if (gstMode == GstMode.ADD) "Added" else "Removed"})"
                                    val res = "Total: ₹${format(totalAmount)}, GST: ₹${format(gstAmount)}"
                                    historyRepository.insert(expr, res, "GST")
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
                                amountStr = ""
                                gstRateStr = "18"
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = ::copyResult,
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
