package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Singleton state that triggers automatic recomposition across the entire application
object DynamicTheme {
    var isDarkMode by mutableStateOf(false) // default Light Mode
    var themeColor by mutableStateOf(Color(0xFFC084FC)) // default Lilac
}

// Cosmic Figma Theme Colors (Deep Midnight vs Pure White)
val SpaceDarkBg: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF000000) else Color(0xFFFFFFFF)
val SpaceCardBg: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F7)
val SpaceCardBorder: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF242424) else Color(0xFFE2E2E5)

val ActiveGreen: Color get() = DynamicTheme.themeColor
val CompletedGold: Color get() = Color(0xFFEC4899) // Sweet Candy Pink / Magenta
val LockedGray: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF1E1E1E) else Color(0xFFECECEC)
val LockedText: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF757575) else Color(0xFF9E9E9E)

val AccentCyan: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF8B5CF6) else Color(0xFF7C3AED)
val TextLight: Color get() = if (DynamicTheme.isDarkMode) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)
val TextMuted: Color get() = if (DynamicTheme.isDarkMode) Color(0xFF9E9E9E) else Color(0xFF6E6E73)

// Theme Mapped Tokens
val PrimaryDark: Color get() = ActiveGreen
val SecondaryDark: Color get() = AccentCyan
val TertiaryDark: Color get() = CompletedGold
val BackgroundDark: Color get() = SpaceDarkBg
val SurfaceDark: Color get() = SpaceCardBg


