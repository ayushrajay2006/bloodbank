package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bloodbank.ui.*
import com.example.bloodbank.ui.theme.BloodBankTheme // or EmergencyRelayTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName

        // Seed the database
        BloodBankSeeder.seedIfEmpty(this)

        setContent {
            BloodBankTheme { // Ensure this matches your Theme name in ui/theme/Theme.kt

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
                        // 👇 UPDATED TO USE CoreDatabase
                        val db = CoreDatabase.getDatabase(this)

                        val request by produceState<EmergencyRequest?>(initialValue = null) {
                            try {
                                value = db.emergencyRequestDao().getRequestById(selectedRequestId ?: 0)
                            } catch (e: Exception) {
                                value = null
                            }
                        }

                        // Pass request to map screen
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