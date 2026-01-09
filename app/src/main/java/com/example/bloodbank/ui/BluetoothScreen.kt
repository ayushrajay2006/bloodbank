package com.example.bloodbank.ui

import android.bluetooth.BluetoothAdapter
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bloodbank.CoreDatabase
import com.example.bloodbank.bluetooth.BluetoothClient
import com.example.bloodbank.bluetooth.BluetoothServer
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = CoreDatabase.getDatabase(context)
    val adapter = BluetoothAdapter.getDefaultAdapter()

    val server = remember { BluetoothServer(adapter, db) }
    val client = remember { BluetoothClient(adapter) }

    val myRequests by db.emergencyRequestDao().getAllRequests().collectAsState(initial = emptyList())

    // This text will now show EXACTLY what is happening
    var statusText by remember { mutableStateOf("Ready to Connect") }
    var isServerRunning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Relay (Real)", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = MedicalRed,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Real Device Connection", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // 👇 BIG STATUS TEXT UPDATE
            Text(
                text = statusText,
                color = if (statusText.contains("Failed")) Color.Red else Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // RECEIVER BUTTON
            Button(
                onClick = {
                    if (!isServerRunning) {
                        isServerRunning = true
                        // Pass the function to update text
                        server.start { newStatus ->
                            statusText = newStatus
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isServerRunning) Color(0xFF2E7D32) else MedicalRed)
            ) {
                if (isServerRunning) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Server Active")
                } else {
                    Icon(Icons.Default.BluetoothSearching, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Receive Data (Start Server)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SENDER BUTTON
            OutlinedButton(
                onClick = {
                    if (myRequests.isNotEmpty()) {
                        // Pass the function to update text
                        client.sendRequests(myRequests) { newStatus ->
                            statusText = newStatus
                        }
                    } else {
                        statusText = "You have no requests to send! Create one first."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedicalRed)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send My Requests")
            }
        }
    }
}