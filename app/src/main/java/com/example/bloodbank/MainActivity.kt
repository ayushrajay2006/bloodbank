package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bloodbank.ui.*
import com.example.bloodbank.ui.theme.EmergencyRelayTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BloodBankSeeder.seedIfEmpty(this)

        setContent {
            EmergencyRelayTheme {

                var screen by remember { mutableStateOf("main") }
                var selectedRequestId by remember { mutableStateOf<Int?>(null) }

                when (screen) {

                    "create" -> CreateRequestScreen(
                        onBack = { screen = "main" }
                    )

                    "banks" -> BloodBankScreen()

                    "requests" -> EmergencyRequestsScreen(
                        onNotifyBanks = { id ->
                            selectedRequestId = id
                            screen = "notify"
                        }
                    )

                    "notify" -> {
                        val db = EmergencyDatabase.getDatabase(this)

                        val request by produceState<com.example.bloodbank.EmergencyRequest?>(initialValue = null) {
                            value = db.emergencyRequestDao()
                                .getAllRequestsOnce()
                                .firstOrNull { it.id == selectedRequestId }
                        }

                        BloodBankScreen(emergencyRequest = request)
                    }


                    else -> MainScreen(
                        onCreateRequest = { screen = "create" },
                        onViewBloodBanks = { screen = "banks" },
                        onViewRequests = { screen = "requests" }
                    )
                }
            }
        }
    }
}
