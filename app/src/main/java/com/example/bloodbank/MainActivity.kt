package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bloodbank.map.BloodBankMapScreen
import com.example.bloodbank.ui.*
import com.example.bloodbank.ui.theme.BloodBankTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        BloodBankSeeder.seedIfEmpty(this)

        setContent {
            BloodBankTheme {

                var screen by remember { mutableStateOf("main") }
                var selectedRequestId by remember { mutableStateOf<Int?>(null) }

                when (screen) {

                    "create" -> CreateRequestScreen(
                        onBack = { screen = "main" }
                    )

                    "banks" -> BloodBankScreen(
                        onBack = { screen = "main" }
                    )

                    "donors" -> DonorsScreen(
                        onBack = { screen = "main" }
                    )

                    "requests" -> EmergencyRequestsScreen(
                        onNotifyBanks = { id ->
                            selectedRequestId = id
                            screen = "notify"
                        },
                        onBack = { screen = "main" }
                    )

                    "dengue" -> DengueAssistantScreen(
                        onBack = { screen = "main" },
                        onFindHelp = { screen = "banks" }
                    )

                    // 👇 NEW BLUETOOTH SCREEN
                    "bluetooth" -> BluetoothScreen(
                        onBack = { screen = "main" }
                    )

                    "notify" -> {
                        val db = CoreDatabase.getDatabase(this)
                        val banks by db.bloodBankDao().getAllBloodBanks().collectAsState(initial = emptyList())

                        BloodBankMapScreen(
                            context = this,
                            userLocation = null,
                            bloodBanks = banks,
                            onBack = { screen = "requests" }
                        )
                    }

                    else -> MainScreen(
                        onCreateRequest = { screen = "create" },
                        onViewBloodBanks = { screen = "banks" },
                        onViewRequests = { screen = "requests" },
                        onViewDonors = { screen = "donors" },
                        onDengueClick = { screen = "dengue" },
                        onBluetoothClick = { screen = "bluetooth" } // 👈 WIRED UP
                    )
                }
            }
        }
    }
}