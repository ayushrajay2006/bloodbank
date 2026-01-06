package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bloodbank.ui.CreateRequestScreen
import com.example.bloodbank.ui.MainScreen
import com.example.bloodbank.ui.theme.EmergencyRelayTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmergencyRelayTheme {

                var showCreateScreen by remember { mutableStateOf(false) }

                if (showCreateScreen) {
                    CreateRequestScreen(
                        onBack = { showCreateScreen = false }
                    )
                } else {
                    MainScreen(
                        onCreateRequest = { showCreateScreen = true }
                    )
                }
            }
        }
    }
}
