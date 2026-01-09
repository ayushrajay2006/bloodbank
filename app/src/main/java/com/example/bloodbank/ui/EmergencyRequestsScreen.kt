package com.example.bloodbank.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 👈 Back Icon
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bloodbank.CoreDatabase
import com.example.bloodbank.RequestStatus
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyRequestsScreen(
    onNotifyBanks: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = CoreDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val requests by database.emergencyRequestDao().getAllRequests().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text("Manage Status & Broadcast", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't created any requests yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(requests) { req ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Group: ${req.bloodGroup}", style = MaterialTheme.typography.titleMedium, color = MedicalRed, fontWeight = FontWeight.Bold)

                                    Surface(
                                        color = when(req.status) {
                                            RequestStatus.CRITICAL -> MedicalRed
                                            RequestStatus.COMPLETED -> Color(0xFF4CAF50)
                                            else -> Color(0xFFE0E0E0)
                                        },
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = if (req.status == RequestStatus.COMPLETED) "RESOLVED" else req.status.name,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (req.status == RequestStatus.CRITICAL || req.status == RequestStatus.COMPLETED) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Loc: ${req.location}", style = MaterialTheme.typography.bodyMedium)
                                Text("Phone: ${req.contactNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                                Spacer(modifier = Modifier.height(16.dp))

                                if (req.status != RequestStatus.COMPLETED) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (req.status != RequestStatus.CRITICAL) {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        database.emergencyRequestDao().updateStatus(req.id, RequestStatus.CRITICAL)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Critical")
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    database.emergencyRequestDao().updateStatus(req.id, RequestStatus.COMPLETED)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Resolve")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = { onNotifyBanks(req.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedicalRed)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Notify Nearby Banks")
                                    }
                                } else {
                                    Text(
                                        "This request has been resolved. Great work!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF388E3C),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}