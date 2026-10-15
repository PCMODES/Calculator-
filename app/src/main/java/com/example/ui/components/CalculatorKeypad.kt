package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class CalcButtonType {
    NUMBER,
    OPERATOR,
    ACTION,      // AC, DEL
    EQUALS,
    SCIENTIFIC,
    MEMORY
}

@Composable
fun CalcButton(
    text: String,
    type: CalcButtonType = CalcButtonType.NUMBER,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = false,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Fast GPU spring scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "btnSpringScale"
    )

    val isLight = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    val shape = RoundedCornerShape(18.dp)

    val baseBackgroundColor = when (type) {
        CalcButtonType.NUMBER -> if (isLight) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.surface
        CalcButtonType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isLight) 0.95f else 0.85f)
        CalcButtonType.ACTION -> MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isLight) 0.95f else 0.85f)
        CalcButtonType.EQUALS -> Color.Transparent
        CalcButtonType.SCIENTIFIC -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLight) 0.85f else 0.7f)
        CalcButtonType.MEMORY -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLight) 0.75f else 0.5f)
    }

    val contentColor = when (type) {
        CalcButtonType.NUMBER -> MaterialTheme.colorScheme.onSurface
        CalcButtonType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        CalcButtonType.ACTION -> MaterialTheme.colorScheme.onErrorContainer
        CalcButtonType.EQUALS -> Color.White
        CalcButtonType.SCIENTIFIC -> MaterialTheme.colorScheme.primary
        CalcButtonType.MEMORY -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Crisp borders calibrated for high visibility and clear separation
    val borderStroke = when (type) {
        CalcButtonType.EQUALS -> null
        CalcButtonType.NUMBER -> {
            if (isLight) {
                BorderStroke(1.2.dp, Color(0xFFD1D5DB))
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            }
        }
        CalcButtonType.OPERATOR -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isLight) 0.45f else 0.25f))
        CalcButtonType.ACTION -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = if (isLight) 0.45f else 0.25f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isLight) 0.35f else 0.15f))
    }

    // Clean, lightweight elevation that looks great without GPU overdraw
    val shadowElevation = when {
        isPressed -> 0.5.dp
        type == CalcButtonType.EQUALS -> 4.dp
        type == CalcButtonType.NUMBER -> if (isLight) 3.dp else 1.5.dp
        type == CalcButtonType.OPERATOR || type == CalcButtonType.ACTION -> if (isLight) 2.5.dp else 1.5.dp
        else -> 1.dp
    }

    val pressedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "pressedOverlay"
    )

    Surface(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true)
            ) {
                FeedbackHelper.performClickFeedback(view, context, hapticEnabled, soundEnabled)
                onClick()
            }
            .testTag("calc_btn_$text"),
        shape = shape,
        color = if (type == CalcButtonType.EQUALS) Color.Transparent else baseBackgroundColor,
        border = borderStroke,
        shadowElevation = shadowElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (type == CalcButtonType.EQUALS) {
                        Modifier.background(EqualsGradient)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = when {
                    text.length > 3 -> 13.sp
                    type == CalcButtonType.NUMBER -> 24.sp
                    type == CalcButtonType.EQUALS -> 26.sp
                    else -> 22.sp
                },
                fontWeight = when (type) {
                    CalcButtonType.EQUALS -> FontWeight.ExtraBold
                    CalcButtonType.ACTION, CalcButtonType.OPERATOR -> FontWeight.Bold
                    else -> FontWeight.SemiBold
                }
            )

            // Smooth tactile press sheen
            if (pressedAlpha > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (type == CalcButtonType.EQUALS) Color.White.copy(alpha = pressedAlpha * 1.5f)
                            else if (isLight) Color.Black.copy(alpha = pressedAlpha)
                            else Color.White.copy(alpha = pressedAlpha)
                        )
                )
            }
        }
    }
}
