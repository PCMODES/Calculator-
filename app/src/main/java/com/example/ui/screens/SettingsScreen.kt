package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AngleMode
import com.example.data.PreferencesManager
import com.example.data.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val currentTheme by preferencesManager.themeMode.collectAsState()
    val amoledMode by preferencesManager.amoledBlack.collectAsState()
    val haptics by preferencesManager.hapticFeedback.collectAsState()
    val sound by preferencesManager.soundFeedback.collectAsState()
    val precision by preferencesManager.decimalPrecision.collectAsState()
    val useThousands by preferencesManager.thousandsSeparator.collectAsState()
    val autoSave by preferencesManager.autoSaveCalculations.collectAsState()
    val angleMode by preferencesManager.defaultAngleMode.collectAsState()
    val appLanguage by preferencesManager.appLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // THEME & APPEARANCE
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                val isSystemDark = isSystemInDarkTheme()

                // Theme Mode Segmented Button
                Text("Theme Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = currentTheme == ThemeMode.SYSTEM,
                        onClick = { coroutineScope.launch { preferencesManager.setThemeMode(ThemeMode.SYSTEM) } },
                        shape = SegmentedButtonDefaults.itemShape(0, 3)
                    ) { Text("System (Auto)") }

                    SegmentedButton(
                        selected = currentTheme == ThemeMode.LIGHT,
                        onClick = { coroutineScope.launch { preferencesManager.setThemeMode(ThemeMode.LIGHT) } },
                        shape = SegmentedButtonDefaults.itemShape(1, 3)
                    ) { Text("Light") }

                    SegmentedButton(
                        selected = currentTheme == ThemeMode.DARK,
                        onClick = { coroutineScope.launch { preferencesManager.setThemeMode(ThemeMode.DARK) } },
                        shape = SegmentedButtonDefaults.itemShape(2, 3)
                    ) { Text("Dark") }
                }

                // Information banner showing how system theme mode operates
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentTheme) {
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = when (currentTheme) {
                                ThemeMode.SYSTEM -> "System (Auto): Changes automatically according to your device's Light or Dark mode. (Device is currently ${if (isSystemDark) "Dark Mode" else "Light Mode"})."
                                ThemeMode.LIGHT -> "Light Mode: Always keep the app in bright theme."
                                ThemeMode.DARK -> "Dark Mode: Always keep the app in dark theme."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AMOLED True Black Toggle
                SettingsSwitchRow(
                    title = "AMOLED True Black",
                    subtitle = "Deep pitch black dark mode for OLED screens",
                    checked = amoledMode,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.setAmoledBlack(it) } }
                )
            }

            // LANGUAGE
            SettingsSection(title = "Language / भाषा", icon = Icons.Default.Language) {
                Text("App Language", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = appLanguage == "en",
                        onClick = { preferencesManager.setAppLanguage("en") },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("English") }

                    SegmentedButton(
                        selected = appLanguage == "hi",
                        onClick = { preferencesManager.setAppLanguage("hi") },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("हिन्दी (Hindi)") }
                }
            }

            // CALCULATION & FORMATTING
            SettingsSection(title = "Calculation & Formatting", icon = Icons.Default.Calculate) {
                // Decimal Precision
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Decimal Precision", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$precision Decimals", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 4, 6, 8).forEach { p ->
                            FilterChip(
                                selected = precision == p,
                                onClick = { coroutineScope.launch { preferencesManager.setDecimalPrecision(p) } },
                                label = { Text("$p digits") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Thousands Separator
                SettingsSwitchRow(
                    title = "Thousands Separator",
                    subtitle = "Format numbers as 1,234,567.89",
                    checked = useThousands,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.setThousandsSeparator(it) } }
                )

                // Default Angle Mode
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Default Trigonometry Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AngleMode.values().forEach { mode ->
                            FilterChip(
                                selected = angleMode == mode,
                                onClick = { coroutineScope.launch { preferencesManager.setDefaultAngleMode(mode) } },
                                label = { Text(mode.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Auto-save Calculations
                SettingsSwitchRow(
                    title = "Auto-save Calculations",
                    subtitle = "Automatically store completed equations in history",
                    checked = autoSave,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.setAutoSaveCalculations(it) } }
                )
            }

            // FEEDBACK & VIBRATION
            SettingsSection(title = "Haptics & Audio", icon = Icons.Default.Vibration) {
                SettingsSwitchRow(
                    title = "Haptic Vibration",
                    subtitle = "Tactile feedback when clicking calculator keys",
                    checked = haptics,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.setHapticFeedback(it) } }
                )

                SettingsSwitchRow(
                    title = "Key Click Sounds",
                    subtitle = "Play subtle system click sounds when typing",
                    checked = sound,
                    onCheckedChange = { coroutineScope.launch { preferencesManager.setSoundFeedback(it) } }
                )
            }

            // ABOUT
            SettingsSection(title = "About Smart Calculator", icon = Icons.Default.Info) {
                // App Logo and Brand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Smart Calculator Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Smart Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Pro Edition • 20+ Smart Tools",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Version", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("1.0.0 (Production Release)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Offline Ready",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("100% Offline & Private", fontWeight = FontWeight.SemiBold)
                        Text(
                            "All calculations and data are processed and stored exclusively on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
