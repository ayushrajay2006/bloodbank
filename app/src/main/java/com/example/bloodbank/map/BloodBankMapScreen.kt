package com.example.bloodbank.map

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.bloodbank.BloodBank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodBankMapScreen(
    context: Context,
    userLocation: Location?,
    bloodBanks: List<BloodBank>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Blood Banks") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                // Removed Radar Action Button completely
                actions = {}
            )
        },
        floatingActionButton = {
            // "Navigate to Nearest" Button
            ExtendedFloatingActionButton(
                onClick = {
                    if (userLocation != null && bloodBanks.isNotEmpty()) {
                        val nearestBank = bloodBanks.minByOrNull { bank ->
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                userLocation.latitude, userLocation.longitude,
                                bank.latitude, bank.longitude,
                                results
                            )
                            results[0]
                        }

                        if (nearestBank != null) {
                            Toast.makeText(context, "Routing to ${nearestBank.name}", Toast.LENGTH_SHORT).show()
                            val gmmIntentUri = Uri.parse("google.navigation:q=${nearestBank.latitude},${nearestBank.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                            }
                        }
                    } else {
                        Toast.makeText(context, "Waiting for GPS...", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                text = { Text("NAVIGATE TO NEAREST") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Direct Map Wrapper call, no radar check
            MapViewWrapper(
                userLocation = userLocation,
                bloodBanks = bloodBanks,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}