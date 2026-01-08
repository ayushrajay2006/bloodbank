package com.example.bloodbank.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bloodbank.EmergencyDatabase
import com.example.bloodbank.RequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EmergencyRequestsScreen(
    onNotifyBanks: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = EmergencyDatabase.getDatabase(context)
    val repository = RequestRepository(context)

    val requests by database
        .emergencyRequestDao()
        .getAllRequests()
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Emergency Requests",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (requests.isEmpty()) {
            Text("No emergency requests found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(requests) { request ->
                    EmergencyRequestCard(
                        request = request,
                        onMarkResolved = {
                            CoroutineScope(Dispatchers.IO).launch {
                                repository.resolveRequest(request.id)
                            }
                        },
                        onNotifyBanks = {
                            onNotifyBanks(request.id)
                        }
                    )
                }
            }
        }
    }
}
