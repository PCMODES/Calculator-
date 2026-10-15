package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.HistoryRepository
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalPriceStr by remember { mutableStateOf("") }
    var discountPercentStr by remember { mutableStateOf("") }
    var additionalTaxStr by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
    val discountPercent = discountPercentStr.toDoubleOrNull() ?: 0.0
    val additionalTax = additionalTaxStr.toDoubleOrNull() ?: 0.0

    val (discountAmount, priceAfterDiscount, taxAmount, finalPrice, totalSavings) = remember(originalPrice, discountPercent, additionalTax) {
        val discount = originalPrice * (discountPercent / 100.0)
        val discounted = (originalPrice - discount).coerceAtLeast(0.0)
        val tax = discounted * (additionalTax / 100.0)
        val finalP = discounted + tax
        val savings = (originalPrice - finalP).coerceAtLeast(0.0)
        listOf(discount, discounted, tax, finalP, savings)
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    fun share() {
        val text = """
            🏷️ Discount Breakdown
            Original Price: ₹ ${format(originalPrice)}
            Discount: $discountPercent% (-₹ ${format(discountAmount)})
            Additional Tax: $additionalTax% (+₹ ${format(taxAmount)})
            Final Price: ₹ ${format(finalPrice)}
            You Save: ₹ ${format(totalSavings)}
            Calculated via Smart Calculator
        """.trimIndent()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Discount"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discount Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = ::share) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "₹${format(originalPrice)} with $discountPercent% off"
                            val res = "Final: ₹${format(finalPrice)} (Saved ₹${format(totalSavings)})"
                            historyRepository.insert(expr, res, "Discount")
                            snackbarMessage = "Saved to history"
                        }
                    }) { Icon(Icons.Default.BookmarkBorder, contentDescription = "Save") }
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = originalPriceStr,
                        onValueChange = { originalPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Original Price (₹)") },
                        placeholder = { Text("उदा. 1200") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discountPercentStr,
                        onValueChange = { discountPercentStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Discount (%)") },
                        placeholder = { Text("उदा. 25") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick discount chips
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10, 20, 30, 50).forEach { disc ->
                            FilterChip(
                                selected = discountPercentStr == disc.toString(),
                                onClick = { discountPercentStr = disc.toString() },
                                label = { Text("$disc%") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = additionalTaxStr,
                        onValueChange = { additionalTaxStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Additional Tax / GST % (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Final Price", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(finalPrice),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Final Price", format(finalPrice)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("You Save", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("₹ " + format(totalSavings), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount Amount", style = MaterialTheme.typography.bodySmall)
                        Text("- ₹ " + format(discountAmount), style = MaterialTheme.typography.bodySmall)
                    }

                    if (additionalTax > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax Added ($additionalTax%)", style = MaterialTheme.typography.bodySmall)
                            Text("+ ₹ " + format(taxAmount), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val expr = "₹${format(originalPrice)} with $discountPercent% off"
                                    val res = "Final: ₹${format(finalPrice)} (Saved ₹${format(totalSavings)})"
                                    historyRepository.insert(expr, res, "Discount")
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
                                originalPriceStr = ""
                                discountPercentStr = ""
                                additionalTaxStr = "0"
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Final Price", format(finalPrice)))
                                snackbarMessage = "Copied: ₹${format(finalPrice)}"
                            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var costPriceStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val costPrice = costPriceStr.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0

    val diff = sellingPrice - costPrice
    val isProfit = diff >= 0
    val percent = if (costPrice > 0) (kotlin.math.abs(diff) / costPrice) * 100.0 else 0.0
    val margin = if (sellingPrice > 0) (diff / sellingPrice) * 100.0 else 0.0

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    fun share() {
        val text = """
            📈 Profit & Loss Report
            Cost Price: ₹ ${format(costPrice)}
            Selling Price: ₹ ${format(sellingPrice)}
            Result: ${if (isProfit) "Profit" else "Loss"} of ₹ ${format(kotlin.math.abs(diff))}
            Margin: ${String.format(Locale.US, "%.2f%%", margin)}
            Percentage: ${String.format(Locale.US, "%.2f%%", percent)}
            Calculated via Smart Calculator
        """.trimIndent()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Profit & Loss"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit & Loss", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = ::share) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "Cost: ₹${format(costPrice)}, Sell: ₹${format(sellingPrice)}"
                            val res = "${if (isProfit) "Profit" else "Loss"}: ₹${format(kotlin.math.abs(diff))} (${String.format(Locale.US, "%.2f%%", percent)})"
                            historyRepository.insert(expr, res, "Profit & Loss")
                            snackbarMessage = "Saved to history"
                        }
                    }) { Icon(Icons.Default.BookmarkBorder, contentDescription = "Save") }
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cost Price (CP)") },
                        placeholder = { Text("उदा. 1000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Selling Price (SP)") },
                        placeholder = { Text("उदा. 1250") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfit) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(if (isProfit) "Net Profit" else "Net Loss", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹ " + format(kotlin.math.abs(diff)),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Profit/Loss", format(diff)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${if (isProfit) "Profit" else "Loss"} Percentage", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "%.2f%%", percent),
                            fontWeight = FontWeight.Bold,
                            color = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Profit Margin", style = MaterialTheme.typography.bodySmall)
                        Text(String.format(Locale.US, "%.2f%%", margin), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val expr = "Cost: ₹${format(costPrice)}, Sell: ₹${format(sellingPrice)}"
                                    val res = "${if (isProfit) "Profit" else "Loss"}: ₹${format(kotlin.math.abs(diff))} (${String.format(Locale.US, "%.2f%%", percent)})"
                                    historyRepository.insert(expr, res, "Profit & Loss")
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
                                costPriceStr = ""
                                sellingPriceStr = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }

                        OutlinedButton(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Profit/Loss", format(diff)))
                                snackbarMessage = "Copied: ₹${format(diff)}"
                            },
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
