package com.example.bloodbank.ui

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
import com.example.bloodbank.bluetooth.BluetoothRelayManager // 👈 Import the Manager
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Read directly from the Singleton Manager
    val isRelayActive = BluetoothRelayManager.isRelayActive
    val statusLog = BluetoothRelayManager.statusLog

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
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // TOGGLE BUTTON
            Button(
                onClick = {
                    if (isRelayActive) {
                        BluetoothRelayManager.stop()
                    } else {
                        BluetoothRelayManager.start(context)
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