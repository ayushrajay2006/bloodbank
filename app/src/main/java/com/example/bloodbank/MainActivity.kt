package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.bloodbank.map.BloodBankMapScreen // Import the Map Screen
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

                    "notify" -> {
                        // 👇 FIX: Fetch banks and show Map Screen directly
                        val db = CoreDatabase.getDatabase(this)
                        val banks by db.bloodBankDao().getAllBloodBanks().collectAsState(initial = emptyList())

                        // We can also fetch the request if we want to show its location specifically,
                        // but for "Notify", showing the map of banks is the goal.

                        BloodBankMapScreen(
                            context = this,
                            userLocation = null, // Or pass real location if available
                            bloodBanks = banks,
                            onBack = { screen = "requests" } // Return to requests list
                        )
                    }

                    else -> MainScreen(
                        onCreateRequest = { screen = "create" },
                        onViewBloodBanks = { screen = "banks" },
                        onViewRequests = { screen = "requests" },
                        onViewDonors = { screen = "donors" }
                    )
                }
            }
        }
    }
}