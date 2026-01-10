package com.example.bloodbank.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bloodbank.bluetooth.BluetoothRelayManager
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Read directly from the Singleton Manager
    val isRelayActive = BluetoothRelayManager.isRelayActive
    val statusLog = BluetoothRelayManager.statusLog

    // Define the permissions we need based on Android Version
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ needs these specific new permissions
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        // Android 11 and below needs Location to find devices
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // Permission Launcher: Handles the user's "Allow" or "Deny" choice
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Success! Start the relay
            BluetoothRelayManager.start(context)
        } else {
            Toast.makeText(context, "Bluetooth permissions are required for Relay Mode", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automatic Relay", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // PULSING ICON
            Box(contentAlignment = Alignment.Center) {
                if(isRelayActive) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MedicalRed.copy(alpha = 0.2f)))
                }
                Icon(
                    imageVector = Icons.Default.BluetoothConnected,
                    contentDescription = null,
                    tint = if(isRelayActive) MedicalRed else Color.Gray,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                if (isRelayActive) "Relay Active" else "Relay Offline",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRelayActive) MedicalRed else Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                statusLog,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(60.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // UPDATED BUTTON WITH PERMISSION CHECK
            Button(
                onClick = {
                    if (isRelayActive) {
                        BluetoothRelayManager.stop()
                    } else {
                        // Check if we already have permissions
                        val hasPermissions = requiredPermissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }

                        if (hasPermissions) {
                            BluetoothRelayManager.start(context)
                        } else {
                            // If not, ASK for them now
                            permissionLauncher.launch(requiredPermissions)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRelayActive) Color.Gray else MedicalRed
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.PowerSettingsNew, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRelayActive) "STOP RELAY" else "START AUTO-SYNC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Background Service", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This relay will now keep running in the background even if you navigate to other screens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
}