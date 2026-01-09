package com.example.bloodbank.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your colors here
private val LightColorScheme = lightColorScheme(
    primary = MedicalRed,
    secondary = Color(0xFF388E3C), // Dark Green
    background = OffWhite,
    surface = Color.White
)

@Composable
fun BloodBankTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}