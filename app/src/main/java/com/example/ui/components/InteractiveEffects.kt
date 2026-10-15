package com.example.ui.components

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance modifier extension providing a GPU hardware-accelerated spring press
 * animation and tactile feedback for any clickable element.
 *
 * Uses `graphicsLayer` to avoid expensive layout re-measurements on low-end devices.
 */
fun Modifier.bouncyClick(
    scaleDown: Float = 0.94f,
    hapticFeedback: Boolean = true,
    soundFeedback: Boolean = false,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bouncyScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true)
        ) {
            if (hapticFeedback) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            if (soundFeedback) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
            onClick()
        }
}

/**
 * Reusable animated bounce button component optimized for 60fps rendering on budget phones.
 */
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = Color.Transparent,
    contentColor: Color = Color.Unspecified,
    defaultElevation: Dp = 2.dp,
    pressedElevation: Dp = 0.5.dp,
    scaleDown: Float = 0.94f,
    minHeight: Dp = 48.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bouncyButtonScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) pressedElevation else defaultElevation,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "bouncyButtonElevation"
    )

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = elevation
    ) {
        Box(
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}
