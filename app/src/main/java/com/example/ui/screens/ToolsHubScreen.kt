package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.ToolCategory
import com.example.model.ToolInfo
import com.example.model.ToolRegistry
import com.example.ui.components.FeedbackHelper
import kotlinx.coroutines.launch

@Composable
fun ToolsHubScreen(
    preferencesManager: PreferencesManager,
    onToolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isProSupporter by preferencesManager.proSupporter.collectAsState()
    val favorites by preferencesManager.favoriteTools.collectAsState()
    val recents by preferencesManager.recentTools.collectAsState()
    val hapticEnabled by preferencesManager.hapticFeedback.collectAsState()
    val soundEnabled by preferencesManager.soundFeedback.collectAsState()

    val isLight = MaterialTheme.colorScheme.background.luminance() >= 0.5f

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val filteredTools = remember(searchQuery, selectedCategory, favorites) {
        ToolRegistry.ALL_TOOLS.filter { tool ->
            val matchesQuery = tool.title.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == null || tool.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    val recentToolsList = remember(recents) {
        recents.mapNotNull { id -> ToolRegistry.getToolById(id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Modern Elevated Search Bar
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
                1.dp,
                if (isLight) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            shadowElevation = if (isLight) 2.dp else 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search calculators & tools...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tools_search_field")
            )
        }

        // Custom Tactile Category Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolCategoryChip(
                text = "All Tools",
                icon = Icons.Default.Dashboard,
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                isLight = isLight
            )

            ToolCategory.values().forEach { cat ->
                val icon = when (cat) {
                    ToolCategory.FINANCE -> Icons.Default.AccountBalance
                    ToolCategory.MATH -> Icons.Default.Functions
                    ToolCategory.UTILITY -> Icons.Default.SwapHoriz
                    ToolCategory.DAILY_LIFE -> Icons.Default.Schedule
                }
                ToolCategoryChip(
                    text = cat.displayName,
                    icon = icon,
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    isLight = isLight
                )
            }
        }

        // Tools Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Recents Section (if not searching)
            if (searchQuery.isBlank() && selectedCategory == null && recentToolsList.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Recently Used",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${recentToolsList.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                items(
                    recentToolsList.take(4),
                    key = { "recent_${it.id}" },
                    contentType = { "tool" }
                ) { tool ->
                    ToolCard(
                        tool = tool,
                        isFavorite = favorites.contains(tool.id),
                        onToggleFavorite = {
                            coroutineScope.launch { preferencesManager.toggleFavorite(tool.id) }
                        },
                        onClick = {
                            coroutineScope.launch { preferencesManager.addRecentTool(tool.id) }
                            onToolSelected(tool.id)
                        },
                        hapticEnabled = hapticEnabled,
                        soundEnabled = soundEnabled,
                        isLight = isLight,
                        isRecent = true
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "All Tools",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${filteredTools.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // First Batch of Tools
            items(
                filteredTools.take(4),
                key = { it.id },
                contentType = { "tool" }
            ) { tool ->
                ToolCard(
                    tool = tool,
                    isFavorite = favorites.contains(tool.id),
                    onToggleFavorite = {
                        coroutineScope.launch { preferencesManager.toggleFavorite(tool.id) }
                    },
                    onClick = {
                        coroutineScope.launch { preferencesManager.addRecentTool(tool.id) }
                        onToolSelected(tool.id)
                    },
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    isLight = isLight
                )
            }


            // Remaining Tools
            items(
                filteredTools.drop(4),
                key = { it.id },
                contentType = { "tool" }
            ) { tool ->
                ToolCard(
                    tool = tool,
                    isFavorite = favorites.contains(tool.id),
                    onToggleFavorite = {
                        coroutineScope.launch { preferencesManager.toggleFavorite(tool.id) }
                    },
                    onClick = {
                        coroutineScope.launch { preferencesManager.addRecentTool(tool.id) }
                        onToolSelected(tool.id)
                    },
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    isLight = isLight
                )
            }
        }
    }
}

@Composable
fun ToolCategoryChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    isLight: Boolean
) {
    val view = LocalView.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )

    Surface(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true),
                onClick = {
                    FeedbackHelper.performClickFeedback(view, context, hapticEnabled, soundEnabled)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            1.2.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        shadowElevation = if (selected) 4.dp else if (isLight) 2.5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimary
                       else if (isLight) Color(0xFF334155) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else if (isLight) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolInfo,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    isLight: Boolean,
    modifier: Modifier = Modifier,
    isRecent: Boolean = false
) {
    val view = LocalView.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toolCardScale"
    )

    val borderColor = if (isPressed) {
        tool.color
    } else {
        if (isLight) tool.color.copy(alpha = 0.38f) else tool.color.copy(alpha = 0.45f)
    }

    val shadowElevation = when {
        isPressed -> 1.5.dp
        isLight -> 5.dp
        else -> 3.5.dp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true),
                onClick = {
                    FeedbackHelper.performClickFeedback(view, context, hapticEnabled, soundEnabled)
                    onClick()
                }
            )
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isPressed) 1.8.dp else 1.2.dp, borderColor),
        shadowElevation = shadowElevation,
        tonalElevation = if (isLight) 2.dp else 1.dp,
        color = if (isLight) {
            if (isPressed) tool.color.copy(alpha = 0.05f) else Color.White
        } else {
            Color.Transparent
        }
    ) {
        val boxModifier = if (!isLight) {
            Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        tool.color.copy(alpha = 0.18f),
                        Color(0xFF191E28),
                        Color(0xFF141822)
                    )
                )
            )
        } else {
            Modifier
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            // Vivid Top Color Accent Stripe for unmistakable tool identity in both modes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                tool.color,
                                tool.color.copy(alpha = if (isLight) 0.65f else 0.85f)
                            )
                        )
                    )
            )

            Box(
                modifier = boxModifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top row: Distinct Icon Container on left, Badges & Favorite on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(tool.color.copy(alpha = if (isLight) 0.15f else 0.25f))
                                .border(
                                    1.2.dp,
                                    tool.color.copy(alpha = if (isLight) 0.45f else 0.60f),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = tool.color,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isRecent) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLight) tool.color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLight) tool.color.copy(alpha = 0.30f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        text = "RECENT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) tool.color else MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Favorite Star Button
                            IconButton(
                                onClick = {
                                    FeedbackHelper.performClickFeedback(view, context, hapticEnabled, soundEnabled)
                                    onToggleFavorite()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color(0xFFF59E0B)
                                           else if (isLight) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }

                    // Title, Category & Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tool.color.copy(alpha = if (isLight) 0.12f else 0.18f),
                            border = BorderStroke(
                                0.8.dp,
                                tool.color.copy(alpha = if (isLight) 0.35f else 0.30f)
                            )
                        ) {
                            Text(
                                text = tool.category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tool.color
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = tool.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Interactive Themed Arrow Cue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tool.color.copy(alpha = if (isLight) 0.12f else 0.20f))
                                .border(
                                    0.8.dp,
                                    tool.color.copy(alpha = if (isLight) 0.30f else 0.40f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = tool.color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

