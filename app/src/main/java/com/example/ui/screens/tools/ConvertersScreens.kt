package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.calculator.ConversionUnit
import com.example.calculator.UnitCategory
import com.example.calculator.UnitConversionEngine
import com.example.data.HistoryRepository
import com.example.model.CurrencyInfo
import com.example.model.CurrencyRegistry
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var availableUnits by remember(selectedCategory) {
        mutableStateOf(UnitConversionEngine.getUnitsForCategory(selectedCategory))
    }
    var fromUnit by remember(selectedCategory) { mutableStateOf(availableUnits[2]) } // Meter default
    var toUnit by remember(selectedCategory) { mutableStateOf(availableUnits[1]) }   // Centimeter default

    var inputValueStr by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val inputValue = inputValueStr.toDoubleOrNull() ?: 0.0

    val convertedValue = remember(inputValue, fromUnit, toUnit, selectedCategory) {
        UnitConversionEngine.convert(inputValue, fromUnit, toUnit, selectedCategory)
    }

    fun format(v: Double): String {
        return if (v == kotlin.math.floor(v) && kotlin.math.abs(v) < 1e9) {
            v.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "$inputValue ${fromUnit.symbol} to ${toUnit.symbol}"
                            val res = "${format(convertedValue)} ${toUnit.symbol}"
                            historyRepository.insert(expr, res, "Unit Converter")
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
            // Category horizontal scroller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitConversionEngine.CATEGORIES.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            val units = UnitConversionEngine.getUnitsForCategory(category)
                            availableUnits = units
                            fromUnit = units.first()
                            toUnit = if (units.size > 1) units[1] else units[0]
                        },
                        label = { Text(category.displayName) }
                    )
                }
            }

            // Converter Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // From
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputValueStr,
                            onValueChange = { inputValueStr = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                            label = { Text("From Value") },
                            placeholder = { Text("उदा. 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f)
                        )

                        // From unit dropdown
                        var fromExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { fromExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(fromUnit.symbol, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = fromExpanded,
                                onDismissRequest = { fromExpanded = false }
                            ) {
                                availableUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.name} (${u.symbol})") },
                                        onClick = {
                                            fromUnit = u
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Swap button centered
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                val temp = fromUnit
                                fromUnit = toUnit
                                toUnit = temp
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Swap Units", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    // To
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = format(convertedValue),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Converted Value") },
                            modifier = Modifier.weight(1.2f)
                        )

                        // To unit dropdown
                        var toExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { toExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(toUnit.symbol, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                availableUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.name} (${u.symbol})") },
                                        onClick = {
                                            toUnit = u
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Result", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${format(convertedValue)} ${toUnit.symbol}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Unit", format(convertedValue)))
                            snackbarMessage = "Result copied"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Text(
                        text = "1 ${fromUnit.symbol} = ${format(UnitConversionEngine.convert(1.0, fromUnit, toUnit, selectedCategory))} ${toUnit.symbol}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val expr = "$inputValue ${fromUnit.symbol} to ${toUnit.symbol}"
                                val res = "${format(convertedValue)} ${toUnit.symbol}"
                                historyRepository.insert(expr, res, "Unit Converter")
                                snackbarMessage = "Converted & Saved to history!"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Convert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fromCurrency by remember { mutableStateOf(CurrencyRegistry.CURRENCIES[0]) } // USD
    var toCurrency by remember { mutableStateOf(CurrencyRegistry.CURRENCIES[1]) }   // INR
    var amountStr by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val converted = remember(amount, fromCurrency, toCurrency) {
        CurrencyRegistry.convert(amount, fromCurrency.code, toCurrency.code)
    }

    fun format(v: Double) = String.format(Locale.US, "%,.2f", v)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Converter", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "$amount ${fromCurrency.code} to ${toCurrency.code}"
                            val res = "${format(converted)} ${toCurrency.code}"
                            historyRepository.insert(expr, res, "Currency Converter")
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
            // Offline rate notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Text("Rates: ${CurrencyRegistry.LAST_UPDATED}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Input card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // From
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Amount") },
                            placeholder = { Text("उदा. 100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f)
                        )

                        var fromMenuExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { fromMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("${fromCurrency.flagEmoji} ${fromCurrency.code}", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(expanded = fromMenuExpanded, onDismissRequest = { fromMenuExpanded = false }) {
                                CurrencyRegistry.CURRENCIES.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text("${curr.flagEmoji} ${curr.code} - ${curr.name}") },
                                        onClick = {
                                            fromCurrency = curr
                                            fromMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Swap Button
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                val t = fromCurrency
                                fromCurrency = toCurrency
                                toCurrency = t
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    // To
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = format(converted),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Converted") },
                            modifier = Modifier.weight(1.2f)
                        )

                        var toMenuExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { toMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("${toCurrency.flagEmoji} ${toCurrency.code}", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(expanded = toMenuExpanded, onDismissRequest = { toMenuExpanded = false }) {
                                CurrencyRegistry.CURRENCIES.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text("${curr.flagEmoji} ${curr.code} - ${curr.name}") },
                                        onClick = {
                                            toCurrency = curr
                                            toMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Live Conversion", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${toCurrency.symbol} ${format(converted)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Currency", format(converted)))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    val oneUnitRate = CurrencyRegistry.convert(1.0, fromCurrency.code, toCurrency.code)
                    Text("1 ${fromCurrency.code} = ${format(oneUnitRate)} ${toCurrency.code}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val expr = "$amountStr ${fromCurrency.code} to ${toCurrency.code}"
                                val res = "${toCurrency.symbol} ${format(converted)}"
                                historyRepository.insert(expr, res, "Currency Converter")
                                snackbarMessage = "Converted & Saved to history!"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Convert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
