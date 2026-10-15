package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRepository
import com.example.data.PreferencesManager
import com.example.ui.components.FeedbackHelper
import com.example.ui.components.bouncyClick
import kotlinx.coroutines.launch
import java.util.Locale

enum class TarajuTab {
    RATE,   // Weight to Price
    WEIGHT  // Money to Weight
}

enum class WeightUnit(val label: String, val multiplierToKg: Double) {
    KG("kg", 1.0),
    G("g", 0.001),
    MG("mg", 0.000001)
}

enum class OutputWeightUnit(val label: String, val fromKgMultiplier: Double) {
    G("g", 1000.0),
    KG("kg", 1.0),
    MG("mg", 1000000.0)
}

data class QuickWeightChip(
    val label: String,
    val value: String,
    val unit: WeightUnit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarajuScreen(
    historyRepository: HistoryRepository,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
    initialTab: TarajuTab = TarajuTab.RATE
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val hapticEnabled by preferencesManager.hapticFeedback.collectAsState()
    val soundEnabled by preferencesManager.soundFeedback.collectAsState()
    val isLight = MaterialTheme.colorScheme.background.luminance() >= 0.5f

    var activeTab by remember { mutableStateOf(initialTab) }

    // Tab 1: Rate (Weight -> Price) - Starts EMPTY as requested so user types directly!
    var ratePerKgRateTab by remember { mutableStateOf("") }
    var itemWeightStr by remember { mutableStateOf("") }
    var selectedWeightUnit by remember { mutableStateOf(WeightUnit.G) }

    // Tab 2: Weight (Money -> Weight) - Starts EMPTY as requested so user types directly!
    var ratePerKgWeightTab by remember { mutableStateOf("") }
    var purchaseAmountStr by remember { mutableStateOf("") }
    var selectedOutputUnit by remember { mutableStateOf(OutputWeightUnit.G) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Helper functions
    fun triggerFeedback() {
        FeedbackHelper.performClickFeedback(view, context, hapticEnabled, soundEnabled)
    }

    fun copyText(text: String, label: String = "Taraju Result") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        triggerFeedback()
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
        snackbarMessage = "Copied to clipboard: $text"
    }

    // Calculations for Tab 1: Rate (Weight -> Price)
    val rateVal = ratePerKgRateTab.toDoubleOrNull() ?: 0.0
    val weightInputVal = itemWeightStr.toDoubleOrNull() ?: 0.0

    val (weightInKg, totalPrice, isValidRateCalc) = remember(rateVal, weightInputVal, selectedWeightUnit) {
        if (rateVal > 0.0 && weightInputVal > 0.0) {
            val wKg = weightInputVal * selectedWeightUnit.multiplierToKg
            val total = rateVal * wKg
            Triple(wKg, total, true)
        } else {
            Triple(0.0, 0.0, false)
        }
    }

    // Calculations for Tab 2: Weight (Money -> Weight)
    val rateWeightVal = ratePerKgWeightTab.toDoubleOrNull() ?: 0.0
    val purchaseVal = purchaseAmountStr.toDoubleOrNull() ?: 0.0

    val (customerWeightKg, outputWeightVal, isValidWeightCalc) = remember(rateWeightVal, purchaseVal, selectedOutputUnit) {
        if (rateWeightVal > 0.0 && purchaseVal > 0.0) {
            val wKg = purchaseVal / rateWeightVal
            val outputVal = wKg * selectedOutputUnit.fromKgMultiplier
            Triple(wKg, outputVal, true)
        } else {
            Triple(0.0, 0.0, false)
        }
    }

    val quickWeightChips = remember {
        listOf(
            QuickWeightChip("50 g", "50", WeightUnit.G),
            QuickWeightChip("100 g", "100", WeightUnit.G),
            QuickWeightChip("250 g", "250", WeightUnit.G),
            QuickWeightChip("500 g", "500", WeightUnit.G),
            QuickWeightChip("1 kg", "1", WeightUnit.KG),
            QuickWeightChip("2 kg", "2", WeightUnit.KG),
            QuickWeightChip("5 kg", "5", WeightUnit.KG)
        )
    }

    val quickMoneyChips = remember {
        listOf("10", "20", "50", "100", "200", "500")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Selector Row (Neumorphic Segmented Control with bouncy click)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (isLight) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(
                1.dp,
                if (isLight) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            shadowElevation = if (isLight) 2.dp else 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Rate Tab Button (Smooth Bouncy Tab)
                val rateTabBgColor by animateColorAsState(
                    targetValue = if (activeTab == TarajuTab.RATE) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "rateTabBg"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .bouncyClick(
                            hapticFeedback = hapticEnabled,
                            soundFeedback = soundEnabled
                        ) {
                            activeTab = TarajuTab.RATE
                        }
                        .testTag("taraju_tab_rate"),
                    shape = RoundedCornerShape(12.dp),
                    color = rateTabBgColor,
                    shadowElevation = if (activeTab == TarajuTab.RATE && isLight) 3.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = if (activeTab == TarajuTab.RATE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rate / कीमत",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (activeTab == TarajuTab.RATE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Weight Tab Button (Smooth Bouncy Tab)
                val weightTabBgColor by animateColorAsState(
                    targetValue = if (activeTab == TarajuTab.WEIGHT) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "weightTabBg"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .bouncyClick(
                            hapticFeedback = hapticEnabled,
                            soundFeedback = soundEnabled
                        ) {
                            activeTab = TarajuTab.WEIGHT
                        }
                        .testTag("taraju_tab_weight"),
                    shape = RoundedCornerShape(12.dp),
                    color = weightTabBgColor,
                    shadowElevation = if (activeTab == TarajuTab.WEIGHT && isLight) 3.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = if (activeTab == TarajuTab.WEIGHT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Weight / वजन",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (activeTab == TarajuTab.WEIGHT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: RATE (WEIGHT -> PRICE)
        // =========================================================================
        if (activeTab == TarajuTab.RATE) {
            // Input Fields Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.2.dp,
                    if (isLight) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                shadowElevation = if (isLight) 3.5.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Field 1: Rate Per KG (Empty box initially)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rate Per KG / प्रति किलो दाम",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) Color(0xFF334155) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (ratePerKgRateTab.isNotEmpty()) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .bouncyClick(hapticFeedback = hapticEnabled) {
                                            ratePerKgRateTab = ""
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = ratePerKgRateTab,
                            onValueChange = { ratePerKgRateTab = it.filter { c -> c.isDigit() || c == '.' } },
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                if (ratePerKgRateTab.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            triggerFeedback()
                                            ratePerKgRateTab = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "/kg",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                            },
                            placeholder = { Text("उदा. 800 (दाम दर्ज करें)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("taraju_input_rate_per_kg")
                        )
                    }

                    // Field 2: Item Weight (Empty box initially)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saman Ka Vajan / सामान का वजन",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) Color(0xFF334155) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (itemWeightStr.isNotEmpty()) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .bouncyClick(hapticFeedback = hapticEnabled) {
                                            itemWeightStr = ""
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = itemWeightStr,
                            onValueChange = { itemWeightStr = it.filter { c -> c.isDigit() || c == '.' } },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (itemWeightStr.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            triggerFeedback()
                                            itemWeightStr = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            placeholder = { Text("उदा. 250 (वजन दर्ज करें)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("taraju_input_item_weight")
                        )
                    }

                    // Quick Weight Buttons (1-Tap shortcuts for shopkeepers & everyday use)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Weight / तुरंत वजन चुनें:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickWeightChips) { chip ->
                                val isChipSelected = itemWeightStr == chip.value && selectedWeightUnit == chip.unit
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .bouncyClick(
                                            scaleDown = 0.91f,
                                            hapticFeedback = hapticEnabled,
                                            soundFeedback = soundEnabled
                                        ) {
                                            itemWeightStr = chip.value
                                            selectedWeightUnit = chip.unit
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChipSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        if (isLight) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChipSelected) MaterialTheme.colorScheme.primary else if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        text = chip.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChipSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Field 3: Weight Unit Selector Pills (Animated selection)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Weight Unit / वजन इकाई",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeightUnit.values().forEach { unit ->
                                val isSelected = selectedWeightUnit == unit
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .bouncyClick(
                                            scaleDown = 0.92f,
                                            hapticFeedback = hapticEnabled,
                                            soundFeedback = soundEnabled
                                        ) {
                                            selectedWeightUnit = unit
                                        }
                                        .testTag("taraju_unit_${unit.label}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Glassmorphic Result Card (Rate -> Price)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("taraju_rate_result_card"),
                shape = RoundedCornerShape(24.dp),
                color = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.4.dp,
                    if (isLight) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.4f)
                ),
                shadowElevation = if (isLight) 6.dp else 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (isLight) Color(0xFFE0F2FE).copy(alpha = 0.6f) else Color(0xFF0F2B48).copy(alpha = 0.5f),
                                    if (isLight) Color(0xFFF0FDF4).copy(alpha = 0.6f) else Color(0xFF0A2E28).copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0284C7), Color(0xFF0D9488))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "TOTAL PRICE / कुल दाम",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = if (isLight) Color(0xFF0369A1) else Color(0xFF38BDF8)
                                        )
                                    )
                                    Text(
                                        text = if (isValidRateCalc) {
                                            String.format(Locale.US, "₹%,.2f", totalPrice)
                                        } else "₹0.00",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                                        )
                                    )
                                }
                            }

                            // Normalized weight chip
                            if (isValidRateCalc) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isLight) Color(0xFFE0F2FE) else Color(0xFF0284C7).copy(alpha = 0.25f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLight) Color(0xFF7DD3FC) else Color(0xFF0284C7).copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "(%.3f kg)", weightInKg),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Breakdown line / Guidance hint
                        if (isValidRateCalc) {
                            Text(
                                text = "$itemWeightStr ${selectedWeightUnit.label} @ ₹$ratePerKgRateTab/kg",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC)
                                )
                            )
                        } else {
                            Text(
                                text = "ऊपर खाली बॉक्स में दाम और वजन भरें, लाइव हिसाब तुरंत दिखेगा।",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            )
                        }

                        HorizontalDivider(
                            color = if (isLight) Color(0xFFCBD5E1).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f)
                        )

                        // Action Buttons: Calculate, Reset, Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Calculate Button
                            Button(
                                onClick = {
                                    triggerFeedback()
                                    if (isValidRateCalc) {
                                        coroutineScope.launch {
                                            val expr = "$itemWeightStr ${selectedWeightUnit.label} @ ₹$ratePerKgRateTab/kg"
                                            val res = String.format(Locale.US, "₹%,.2f (%.3f kg)", totalPrice, weightInKg)
                                            historyRepository.insert(expr, res, "Taraju (Rate)")
                                            Toast.makeText(context, "Calculated & Saved!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "कृपया दाम और वजन दर्ज करें", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_calculate_rate")
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Reset Button (Clears both fields back to empty)
                            OutlinedButton(
                                onClick = {
                                    triggerFeedback()
                                    ratePerKgRateTab = ""
                                    itemWeightStr = ""
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_reset_rate")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Reset", fontSize = 13.sp)
                            }

                            // Copy Button
                            OutlinedButton(
                                onClick = {
                                    if (isValidRateCalc) {
                                        val formatted = String.format(Locale.US, "₹%,.2f", totalPrice)
                                        copyText(formatted)
                                    } else {
                                        Toast.makeText(context, "No result to copy", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_copy_rate")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Copy", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: WEIGHT (MONEY -> WEIGHT)
        // =========================================================================
        if (activeTab == TarajuTab.WEIGHT) {
            // Input Fields Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.2.dp,
                    if (isLight) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                shadowElevation = if (isLight) 3.5.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Field 1: Rate Per KG (Empty box initially)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rate Per KG / प्रति किलो दाम",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) Color(0xFF334155) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (ratePerKgWeightTab.isNotEmpty()) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .bouncyClick(hapticFeedback = hapticEnabled) {
                                            ratePerKgWeightTab = ""
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = ratePerKgWeightTab,
                            onValueChange = { ratePerKgWeightTab = it.filter { c -> c.isDigit() || c == '.' } },
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                if (ratePerKgWeightTab.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            triggerFeedback()
                                            ratePerKgWeightTab = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "/kg",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                            },
                            placeholder = { Text("उदा. 40 (दाम दर्ज करें)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("taraju_input_rate_weight_tab")
                        )
                    }

                    // Field 2: Purchase Amount (Khareed Ki Rakam) (Empty box initially)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Khareed Ki Rakam / खरीद की रकम",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) Color(0xFF334155) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (purchaseAmountStr.isNotEmpty()) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .bouncyClick(hapticFeedback = hapticEnabled) {
                                            purchaseAmountStr = ""
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = purchaseAmountStr,
                            onValueChange = { purchaseAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                if (purchaseAmountStr.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            triggerFeedback()
                                            purchaseAmountStr = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            placeholder = { Text("उदा. 15 (रकम दर्ज करें)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("taraju_input_purchase_amount")
                        )
                    }

                    // Quick Money Shortcuts (1-Tap amount selection)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Amount / तुरंत रकम चुनें:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickMoneyChips) { amt ->
                                val isSelected = purchaseAmountStr == amt
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .bouncyClick(
                                            scaleDown = 0.91f,
                                            hapticFeedback = hapticEnabled,
                                            soundFeedback = soundEnabled
                                        ) {
                                            purchaseAmountStr = amt
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        if (isLight) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        text = "₹$amt",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Field 3: Output Weight Unit Selector (Animated selection pills)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Output Unit / परिणाम इकाई",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutputWeightUnit.values().forEach { unit ->
                                val isSelected = selectedOutputUnit == unit
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .bouncyClick(
                                            scaleDown = 0.92f,
                                            hapticFeedback = hapticEnabled,
                                            soundFeedback = soundEnabled
                                        ) {
                                            selectedOutputUnit = unit
                                        }
                                        .testTag("taraju_output_unit_${unit.label}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Glassmorphic Result Card (Money -> Weight)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("taraju_weight_result_card"),
                shape = RoundedCornerShape(24.dp),
                color = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.4.dp,
                    if (isLight) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.4f)
                ),
                shadowElevation = if (isLight) 6.dp else 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (isLight) Color(0xFFE0F2FE).copy(alpha = 0.6f) else Color(0xFF0F2B48).copy(alpha = 0.5f),
                                    if (isLight) Color(0xFFEDE9FE).copy(alpha = 0.6f) else Color(0xFF2E1065).copy(alpha = 0.35f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "CUSTOMER WEIGHT / वजन",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = if (isLight) Color(0xFF2563EB) else Color(0xFF60A5FA)
                                        )
                                    )
                                    Text(
                                        text = if (isValidWeightCalc) {
                                            String.format(Locale.US, "%.2f %s", outputWeightVal, selectedOutputUnit.label)
                                        } else "0 ${selectedOutputUnit.label}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                                        )
                                    )
                                }
                            }

                            // Equivalent in kg chip
                            if (isValidWeightCalc) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isLight) Color(0xFFDBEAFE) else Color(0xFF1E3A8A).copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLight) Color(0xFF93C5FD) else Color(0xFF3B82F6).copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "(%.3f kg)", customerWeightKg),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFF1D4ED8) else Color(0xFF93C5FD)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Breakdown line / Guidance hint
                        if (isValidWeightCalc) {
                            Text(
                                text = "₹$purchaseAmountStr @ ₹$ratePerKgWeightTab/kg",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLight) Color(0xFF2563EB) else Color(0xFF93C5FD)
                                )
                            )
                        } else {
                            Text(
                                text = "ऊपर खाली बॉक्स में दाम और रकम भरें, मिलने वाला वजन तुरंत दिखेगा।",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            )
                        }

                        HorizontalDivider(
                            color = if (isLight) Color(0xFFCBD5E1).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f)
                        )

                        // Action Buttons: Calculate, Reset, Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Calculate Button
                            Button(
                                onClick = {
                                    triggerFeedback()
                                    if (isValidWeightCalc) {
                                        coroutineScope.launch {
                                            val expr = "₹$purchaseAmountStr @ ₹$ratePerKgWeightTab/kg"
                                            val res = String.format(Locale.US, "%.2f %s (%.3f kg)", outputWeightVal, selectedOutputUnit.label, customerWeightKg)
                                            historyRepository.insert(expr, res, "Taraju (Weight)")
                                            Toast.makeText(context, "Calculated & Saved!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "कृपया दाम और रकम दर्ज करें", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_calculate_weight")
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Reset Button (Clears back to empty)
                            OutlinedButton(
                                onClick = {
                                    triggerFeedback()
                                    ratePerKgWeightTab = ""
                                    purchaseAmountStr = ""
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_reset_weight")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Reset", fontSize = 13.sp)
                            }

                            // Copy Button
                            OutlinedButton(
                                onClick = {
                                    if (isValidWeightCalc) {
                                        val formatted = String.format(Locale.US, "%.2f %s", outputWeightVal, selectedOutputUnit.label)
                                        copyText(formatted)
                                    } else {
                                        Toast.makeText(context, "No result to copy", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("taraju_btn_copy_weight")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Copy", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
