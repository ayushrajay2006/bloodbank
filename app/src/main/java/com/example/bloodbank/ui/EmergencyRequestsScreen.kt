package com.example.bloodbank.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bloodbank.CoreDatabase // 👇 Updated Import
import com.example.bloodbank.ui.theme.MedicalRed

@Composable
fun EmergencyRequestsScreen(
    onNotifyBanks: (Int) -> Unit
) {
    val context = LocalContext.current
    // 👇 UPDATED TO USE CoreDatabase
    val database = CoreDatabase.getDatabase(context)

    val requests by database.emergencyRequestDao().getAllRequests().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Emergency Requests", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active requests found.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(requests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Group: ${req.bloodGroup}", style = MaterialTheme.typography.titleMedium, color = MedicalRed)
                                Text("ID: #${req.id}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Loc: ${req.location}")
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onNotifyBanks(req.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Notify Nearby Banks")
                            }
                        }
                    }
                }
            }
        }
    }
}