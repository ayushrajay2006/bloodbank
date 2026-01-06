package com.example.bloodbank.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository

@Composable
fun MainScreen(
    onCreateRequest: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RequestRepository(context) }

    val requests by repository
        .getAllRequests()
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Emergency Requests",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCreateRequest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Emergency Request")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (requests.isEmpty()) {
            Text(
                text = "No emergency requests yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(requests) { request ->
                    EmergencyRequestItem(request)
                }
            }
        }
    }
}

@Composable
private fun EmergencyRequestItem(request: EmergencyRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Blood Group: ${request.bloodGroup}", fontSize = 16.sp)
            Text("Location: ${request.location}")
            if (request.instructions.isNotBlank()) {
                Text("Notes: ${request.instructions}")
            }
        }
    }
}
