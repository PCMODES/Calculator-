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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val nextBdayDays: Int,
    val nextBdayWeekday: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default DOB: 2000-01-01
    var birthYear by remember { mutableIntStateOf(2000) }
    var birthMonth by remember { mutableIntStateOf(1) }
    var birthDay by remember { mutableIntStateOf(1) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val todayCalendar = remember { Calendar.getInstance() }
    val asOfYear = todayCalendar.get(Calendar.YEAR)
    val asOfMonth = todayCalendar.get(Calendar.MONTH) + 1
    val asOfDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

    val ageResult = remember(birthYear, birthMonth, birthDay) {
        val birthCal = Calendar.getInstance().apply {
            set(birthYear, birthMonth - 1, birthDay, 0, 0, 0)
        }
        val targetCal = Calendar.getInstance().apply {
            set(asOfYear, asOfMonth - 1, asOfDay, 0, 0, 0)
        }

        val diffMillis = targetCal.timeInMillis - birthCal.timeInMillis
        val totalDaysCount = (diffMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

        // Exact Y/M/D
        var y = asOfYear - birthYear
        var m = asOfMonth - birthMonth
        var d = asOfDay - birthDay

        if (d < 0) {
            m--
            val prevMonth = (targetCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            d += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (m < 0) {
            y--
            m += 12
        }

        // Next Birthday calculation
        val nextBdayCal = Calendar.getInstance().apply {
            set(asOfYear, birthMonth - 1, birthDay, 0, 0, 0)
            if (before(targetCal)) {
                add(Calendar.YEAR, 1)
            }
        }
        val daysToNextBday = ((nextBdayCal.timeInMillis - targetCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val weekdayName = SimpleDateFormat("EEEE", Locale.US).format(nextBdayCal.time)

        AgeResult(
            years = y.coerceAtLeast(0),
            months = m.coerceAtLeast(0),
            days = d.coerceAtLeast(0),
            totalDays = totalDaysCount,
            nextBdayDays = daysToNextBday,
            nextBdayWeekday = weekdayName
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val shareText = "🎂 My Age: ${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days (${ageResult.totalDays} total days). Next Birthday in ${ageResult.nextBdayDays} days on a ${ageResult.nextBdayWeekday}!"
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Age"))
                    }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = "DOB: $birthDay/$birthMonth/$birthYear"
                            val res = "${ageResult.years} Yrs, ${ageResult.months} Mos, ${ageResult.days} Days (${ageResult.totalDays} days)"
                            historyRepository.insert(expr, res, "Age Calculator")
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
                    Text("Date of Birth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthDay.toString(),
                            onValueChange = { birthDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                            label = { Text("Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthMonth.toString(),
                            onValueChange = { birthMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                            label = { Text("Month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthYear.toString(),
                            onValueChange = { birthYear = (it.toIntOrNull() ?: 2000).coerceIn(1900, asOfYear) },
                            label = { Text("Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    Text("As of: Today ($asOfDay/$asOfMonth/$asOfYear)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Your Exact Age", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${ageResult.years} Years",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "${ageResult.months} Months, ${ageResult.days} Days",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Age", "${ageResult.years} Yrs ${ageResult.months} Mos ${ageResult.days} Days"))
                            snackbarMessage = "Copied to clipboard"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Days Lived", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(Locale.US, "%,d days", ageResult.totalDays), fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Next Birthday", style = MaterialTheme.typography.bodyMedium)
                        Text("in ${ageResult.nextBdayDays} days (${ageResult.nextBdayWeekday})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(
    historyRepository: HistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isDiffMode by remember { mutableStateOf(true) }

    // Date Diff
    var startDay by remember { mutableIntStateOf(1) }
    var startMonth by remember { mutableIntStateOf(1) }
    var startYear by remember { mutableIntStateOf(2025) }

    var endDay by remember { mutableIntStateOf(15) }
    var endMonth by remember { mutableIntStateOf(8) }
    var endYear by remember { mutableIntStateOf(2026) }

    // Add / Subtract mode
    var addDaysStr by remember { mutableStateOf("30") }
    var isAddOperation by remember { mutableStateOf(true) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val diffResult = remember(startDay, startMonth, startYear, endDay, endMonth, endYear) {
        val c1 = Calendar.getInstance().apply { set(startYear, startMonth - 1, startDay) }
        val c2 = Calendar.getInstance().apply { set(endYear, endMonth - 1, endDay) }
        val diffMs = kotlin.math.abs(c2.timeInMillis - c1.timeInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)
        val weeks = days / 7
        val remDays = days % 7
        Triple(days, weeks, remDays)
    }

    val addResult = remember(startDay, startMonth, startYear, addDaysStr, isAddOperation) {
        val daysToAdd = addDaysStr.toIntOrNull() ?: 0
        val c = Calendar.getInstance().apply {
            set(startYear, startMonth - 1, startDay)
            add(Calendar.DAY_OF_YEAR, if (isAddOperation) daysToAdd else -daysToAdd)
        }
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
        sdf.format(c.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Date Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val expr = if (isDiffMode) "$startDay/$startMonth/$startYear to $endDay/$endMonth/$endYear"
                            else "$startDay/$startMonth/$startYear ${if (isAddOperation) "+" else "-"} $addDaysStr days"
                            val res = if (isDiffMode) "${diffResult.first} days (${diffResult.second} wks ${diffResult.third} d)"
                            else addResult
                            historyRepository.insert(expr, res, "Date Calculator")
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
                SegmentedButton(selected = isDiffMode, onClick = { isDiffMode = true }, shape = SegmentedButtonDefaults.itemShape(0, 2)) {
                    Text("Date Difference")
                }
                SegmentedButton(selected = !isDiffMode, onClick = { isDiffMode = false }, shape = SegmentedButtonDefaults.itemShape(1, 2)) {
                    Text("Add / Subtract Days")
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (isDiffMode) "Start Date" else "Base Date", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDay.toString(),
                            onValueChange = { startDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                            label = { Text("Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startMonth.toString(),
                            onValueChange = { startMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                            label = { Text("Month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startYear.toString(),
                            onValueChange = { startYear = (it.toIntOrNull() ?: 2025).coerceIn(1900, 2100) },
                            label = { Text("Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    if (isDiffMode) {
                        Text("End Date", fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = endDay.toString(),
                                onValueChange = { endDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                                label = { Text("Day") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endMonth.toString(),
                                onValueChange = { endMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                                label = { Text("Month") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endYear.toString(),
                                onValueChange = { endYear = (it.toIntOrNull() ?: 2026).coerceIn(1900, 2100) },
                                label = { Text("Year") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(selected = isAddOperation, onClick = { isAddOperation = true }, label = { Text("+ Add") })
                            FilterChip(selected = !isAddOperation, onClick = { isAddOperation = false }, label = { Text("- Subtract") })
                            OutlinedTextField(
                                value = addDaysStr,
                                onValueChange = { addDaysStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Number of Days") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
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
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (isDiffMode) {
                        Text("Duration Between Dates", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${diffResult.first} Days",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Text(
                            text = "= ${diffResult.second} Weeks and ${diffResult.third} Days",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text("Resulting Date", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = addResult,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val expr = if (isDiffMode) "$startDay/$startMonth/$startYear to $endDay/$endMonth/$endYear"
                                    else "$startDay/$startMonth/$startYear ${if (isAddOperation) "+" else "-"} $addDaysStr days"
                                    val res = if (isDiffMode) "${diffResult.first} days (${diffResult.second} wks ${diffResult.third} d)"
                                    else addResult
                                    historyRepository.insert(expr, res, "Date Calculator")
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
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = if (isDiffMode) "${diffResult.first} Days" else addResult
                                cb.setPrimaryClip(ClipData.newPlainText("Date Result", text))
                                snackbarMessage = "Copied: $text"
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
