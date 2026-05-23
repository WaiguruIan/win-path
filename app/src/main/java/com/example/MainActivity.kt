package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JourneyApp
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JourneyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: JourneyViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()
            val themeHex by viewModel.themeColorHex.collectAsState()

            LaunchedEffect(isDark, themeHex) {
                DynamicTheme.isDarkMode = isDark
                try {
                    val parsedColor = android.graphics.Color.parseColor(themeHex)
                    DynamicTheme.themeColor = androidx.compose.ui.graphics.Color(parsedColor)
                } catch (e: Exception) {
                    // fallbacks
                }
            }

            MyApplicationTheme {
                JourneyApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
