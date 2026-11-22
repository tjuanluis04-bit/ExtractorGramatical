package com.extractor.gramatica.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extractor.gramatica.nlp.Category

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B3FD8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E5FF),
    onPrimaryContainer = Color(0xFF0F1170),
    secondary = Color(0xFF5B5FF0),
    background = Color(0xFFF6F6FC),
    onBackground = Color(0xFF1A1B2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1B2E),
    surfaceVariant = Color(0xFFEDEDF7),
    onSurfaceVariant = Color(0xFF5A5C78),
    outline = Color(0xFFC9CADB),
    outlineVariant = Color(0xFFE3E4F0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB6B8FF),
    onPrimary = Color(0xFF1A1C7A),
    primaryContainer = Color(0xFF2B2E9C),
    onPrimaryContainer = Color(0xFFE4E5FF),
    secondary = Color(0xFF9DA0FF),
    background = Color(0xFF101120),
    onBackground = Color(0xFFE6E7F5),
    surface = Color(0xFF181A2E),
    onSurface = Color(0xFFE6E7F5),
    surfaceVariant = Color(0xFF23253D),
    onSurfaceVariant = Color(0xFFA8AAC6),
    outline = Color(0xFF3A3D5C),
    outlineVariant = Color(0xFF2B2E48)
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val LocalDarkTheme = staticCompositionLocalOf { false }

fun categoryColor(category: Category, dark: Boolean): Color = if (dark) {
    when (category) {
        Category.NOUN -> Color(0xFF8FA9FF)
        Category.ADJ -> Color(0xFF4FD6AE)
        Category.VERB -> Color(0xFFFF9A7A)
        Category.ADV -> Color(0xFFF5C95A)
        Category.TERM -> Color(0xFFBE9CFF)
    }
} else {
    when (category) {
        Category.NOUN -> Color(0xFF3D6CF5)
        Category.ADJ -> Color(0xFF14A37F)
        Category.VERB -> Color(0xFFE8603C)
        Category.ADV -> Color(0xFFD99A0B)
        Category.TERM -> Color(0xFF8E5CF0)
    }
}

@Composable
fun ExtractorTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
