package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Neumorphic & Glassmorphic Light Palette
val PrimaryLight = Color(0xFF247BFF)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0EDFF)
val OnPrimaryContainerLight = Color(0xFF1D4ED8)

val SecondaryLight = Color(0xFF7457FF)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFEDE9FE)
val OnSecondaryContainerLight = Color(0xFF5B21B6)

val TertiaryLight = Color(0xFF0BAF8A)
val OnTertiaryLight = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFEAF1FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF5F8FF)
val OnSurfaceLight = Color(0xFF17243A)
val OnSurfaceVariantLight = Color(0xFF65758B)
val OutlineLight = Color(0xFFCBD5E1)

// Premium Neumorphic & Glassmorphic Dark Palette
val PrimaryDark = Color(0xFF3987FF)
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF1E3A8A)
val OnPrimaryContainerDark = Color(0xFFBFDBFE)

val SecondaryDark = Color(0xFF8766FF)
val OnSecondaryDark = Color(0xFFFFFFFF)
val SecondaryContainerDark = Color(0xFF4C1D95)
val OnSecondaryContainerDark = Color(0xFFDDD6FE)

val TertiaryDark = Color(0xFF2BD4B0)
val OnTertiaryDark = Color(0xFF042F2E)

val BackgroundDark = Color(0xFF0B1220)
val SurfaceDark = Color(0xFF132238)
val SurfaceVariantDark = Color(0xFF111C30)
val OnSurfaceDark = Color(0xFFF3F7FF)
val OnSurfaceVariantDark = Color(0xFF9FB0C8)
val OutlineDark = Color(0xFF1E2D4A)

// AMOLED Pitch Black
val BackgroundAmoled = Color(0xFF000000)
val SurfaceAmoled = Color(0xFF080808)
val SurfaceVariantAmoled = Color(0xFF101010)

// Calculator Specialty Accents
val CalcOpAccent = Color(0xFF3987FF)
val CalcEqualsAccent = Color(0xFF0BAF8A)
val CalcClearAccent = Color(0xFFE34F65)
val CalcSciAccent = Color(0xFF8766FF)
val CalcMemoryAccent = Color(0xFFEC4899)
val CalcAmberAccent = Color(0xFFF59E0B)

// Radiant Gradients for striking attractive UI
val EqualsGradient = Brush.horizontalGradient(
    listOf(Color(0xFF247BFF), Color(0xFF7457FF))
)

val PrimaryActionGradient = Brush.linearGradient(
    listOf(Color(0xFF247BFF), Color(0xFF7457FF))
)

val ClearActionGradient = Brush.horizontalGradient(
    listOf(Color(0xFFE34F65), Color(0xFFDC2626))
)

val SciActionGradient = Brush.horizontalGradient(
    listOf(Color(0xFF8766FF), Color(0xFF7C3AED))
)
