package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ThemeMode
import com.example.ui.theme.CalcOpAccent

/**
 * Top Segmented Navigation Header replacing bottom navigation bar.
 * Clean, modern, loved by users, and removes clutter/unwanted text.
 */
@Composable
fun MainTopNavHeader(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    userName: String = "User",
    modifier: Modifier = Modifier
) {
    var showThemeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(bottom = 6.dp)
    ) {
        // Row 1: Header Row with minimal branding and quick actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean Brand Title (No "Smart..." or cluttered subtitles)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    CalcOpAccent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentRoute) {
                            "taraju" -> Icons.Default.Scale
                            "tools" -> Icons.Default.Widgets
                            "history" -> Icons.Default.History
                            else -> Icons.Default.Calculate
                        },
                        contentDescription = "App Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = when (currentRoute) {
                        "calculator" -> "Calculator"
                        "taraju" -> "Taraju"
                        "tools" -> "All Tools"
                        "history" -> "History"
                        else -> "Calculator"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            // Quick Actions: Theme, Settings, Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Theme Mode Switcher
                Box {
                    IconButton(
                        onClick = { showThemeMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("top_bar_theme_button")
                    ) {
                        val themeIcon = when (currentTheme) {
                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.DARK -> Icons.Default.DarkMode
                        }
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = "Theme Switcher",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("System Default", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) },
                            trailingIcon = {
                                if (currentTheme == ThemeMode.SYSTEM) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onThemeChange(ThemeMode.SYSTEM)
                                showThemeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Light Mode", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                            trailingIcon = {
                                if (currentTheme == ThemeMode.LIGHT) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onThemeChange(ThemeMode.LIGHT)
                                showThemeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark Mode", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                            trailingIcon = {
                                if (currentTheme == ThemeMode.DARK) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                onThemeChange(ThemeMode.DARK)
                                showThemeMenu = false
                            }
                        )
                    }
                }

                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Profile Avatar Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_profile_button")
                ) {
                    if (!avatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Row 2: Gorgeous Modern Segmented Pill Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .height(44.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopSegmentItem(
                    label = "Calc",
                    selectedIcon = Icons.Filled.Calculate,
                    unselectedIcon = Icons.Outlined.Calculate,
                    isSelected = currentRoute == "calculator",
                    modifier = Modifier.weight(1f),
                    testTag = "nav_top_calculator",
                    onClick = { onNavigate("calculator") }
                )

                TopSegmentItem(
                    label = "Taraju",
                    selectedIcon = Icons.Filled.Scale,
                    unselectedIcon = Icons.Outlined.Scale,
                    isSelected = currentRoute == "taraju",
                    modifier = Modifier.weight(1f),
                    testTag = "nav_top_taraju",
                    onClick = { onNavigate("taraju") }
                )

                TopSegmentItem(
                    label = "Tools",
                    selectedIcon = Icons.Filled.Widgets,
                    unselectedIcon = Icons.Outlined.Widgets,
                    isSelected = currentRoute == "tools",
                    modifier = Modifier.weight(1f),
                    testTag = "nav_top_tools",
                    onClick = { onNavigate("tools") }
                )

                TopSegmentItem(
                    label = "History",
                    selectedIcon = Icons.Filled.History,
                    unselectedIcon = Icons.Outlined.History,
                    isSelected = currentRoute == "history",
                    modifier = Modifier.weight(1f),
                    testTag = "nav_top_history",
                    onClick = { onNavigate("history") }
                )
            }
        }
    }
}

/**
 * Individual Segmented Pill Button inside TopNavHeader
 */
@Composable
private fun TopSegmentItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "segmentContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 180),
        label = "segmentContent"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.1.sp
                ),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * AppTopBar for sub-screens (e.g. detailed tools, settings, profile)
 * Clean, standard, and free of clutter or "Smart" marketing text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "Calculator",
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    avatarUri: String? = null,
    userName: String = "User",
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    currentTheme: ThemeMode = ThemeMode.SYSTEM,
    onThemeChange: (ThemeMode) -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    var showThemeMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                ),
                maxLines = 1
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            actions()

            // Quick Theme Switcher Button
            Box {
                IconButton(
                    onClick = { showThemeMenu = true },
                    modifier = Modifier.testTag("top_bar_theme_button")
                ) {
                    val themeIcon = when (currentTheme) {
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                    }
                    Icon(
                        imageVector = themeIcon,
                        contentDescription = "Theme Switcher",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("System Default", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) },
                        trailingIcon = {
                            if (currentTheme == ThemeMode.SYSTEM) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            onThemeChange(ThemeMode.SYSTEM)
                            showThemeMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Light Mode", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                        trailingIcon = {
                            if (currentTheme == ThemeMode.LIGHT) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            onThemeChange(ThemeMode.LIGHT)
                            showThemeMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Dark Mode", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        trailingIcon = {
                            if (currentTheme == ThemeMode.DARK) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            onThemeChange(ThemeMode.DARK)
                            showThemeMenu = false
                        }
                    )
                }
            }

            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("top_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open Settings"
                )
            }

            // Profile button
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("top_bar_profile_button")
            ) {
                if (!avatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
