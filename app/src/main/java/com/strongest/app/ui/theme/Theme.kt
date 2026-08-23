package com.strongest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Palette derived from the app logo (orange / black / gray).
// The deep accent has two variants: #8B2500 for light mode, and a lifted
// rust tone for dark mode, where #8B2500 itself is unreadable on charcoal.
private val BrandOrange = Color(0xFFFF8C00)
private val BrandOrangeContainerDark = Color(0xFF6B3E00)
private val BrandOrangeOnContainerDark = Color(0xFFFFDCC2)
private val BrandOrangeContainerLight = Color(0xFFFFE3C6)
private val BrandOrangeOnContainerLight = Color(0xFF4A2E00)

private val CrimsonAmberLight = Color(0xFF8B2500)
private val CrimsonAmberDark = Color(0xFFE5764A)
private val CrimsonAmberContainerDark = Color(0xFF5C2317)
private val CrimsonAmberOnContainerDark = Color(0xFFFFDBCF)
private val CrimsonAmberContainerLight = Color(0xFFFFDBD1)
private val CrimsonAmberOnContainerLight = Color(0xFF5C1600)

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    onPrimary = Color(0xFF000000),
    primaryContainer = BrandOrangeContainerDark,
    onPrimaryContainer = BrandOrangeOnContainerDark,
    secondary = Color(0xFFC3C6CD),
    onSecondary = Color(0xFF23252A),
    secondaryContainer = Color(0xFF3F4247),
    onSecondaryContainer = Color(0xFFDFE2E8),
    tertiary = CrimsonAmberDark,
    onTertiary = Color(0xFF2B1207),
    tertiaryContainer = CrimsonAmberContainerDark,
    onTertiaryContainer = CrimsonAmberOnContainerDark,
    error = Color(0xFFEF5350),
    onError = Color(0xFF000000),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF212121),
    onSurfaceVariant = Color(0xFFBDBDBD),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    surfaceContainer = Color(0xFF202020),
    outline = Color(0xFF4A4A4A),
    outlineVariant = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = Color(0xFF000000),
    primaryContainer = BrandOrangeContainerLight,
    onPrimaryContainer = BrandOrangeOnContainerLight,
    secondary = Color(0xFF56585E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCDEE3),
    onSecondaryContainer = Color(0xFF2A2C31),
    tertiary = CrimsonAmberLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = CrimsonAmberContainerLight,
    onTertiaryContainer = CrimsonAmberOnContainerLight,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE2E3E7),
    onSurfaceVariant = Color(0xFF44464B),
    surfaceContainerHighest = Color(0xFFE9EAEE),
    surfaceContainer = Color(0xFFF0F1F4),
    outline = Color(0xFF75777C),
    outlineVariant = Color(0xFFDDDEE2)
)

// Green kept from the previous theme: universally read as "set completed",
// and kept clearly distinct from the crimson-amber warm-up accent.
val LocalSuccessColor = staticCompositionLocalOf { Color(0xFFA5D6A7) }

@Composable
fun StrongestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSuccessColor provides if (darkTheme) Color(0xFF2E7D32) else Color(0xFFA5D6A7)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
