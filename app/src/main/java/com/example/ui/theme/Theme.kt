package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = DynamicTheme.isDarkMode,
    themeColor: Color = DynamicTheme.themeColor,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            darkColorScheme(
                primary = themeColor,
                secondary = Color(0xFF8B5CF6),
                tertiary = Color(0xFFEC4899),
                background = Color(0xFF000000),
                surface = Color(0xFF121212),
                onPrimary = Color.Black,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF242424),
                onSurfaceVariant = Color(0xFF9E9E9E)
            )
        }
        else -> {
            lightColorScheme(
                primary = themeColor,
                secondary = Color(0xFF7C3AED),
                tertiary = Color(0xFFEC4899),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFF5F5F7),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = Color(0xFF1C1C1E),
                onSurface = Color(0xFF1C1C1E),
                surfaceVariant = Color(0xFFE2E2E5),
                onSurfaceVariant = Color(0xFF6E6E73)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
