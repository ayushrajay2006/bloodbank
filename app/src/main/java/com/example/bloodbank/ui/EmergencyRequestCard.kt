package com.example.bloodbank.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestStatus

@Composable
fun EmergencyRequestCard(
    request: EmergencyRequest,
    onMarkResolved: () -> Unit,
    onNotifyBanks: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Blood Group: ${request.bloodGroup}")
            Text("Location: ${request.location}")

            if (request.instructions.isNotBlank()) {
                Text("Instructions: ${request.instructions}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Status: ${request.status}",
                color = if (request.status == RequestStatus.ACTIVE)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(onClick = onNotifyBanks) {
                    Text("Notify Blood Banks")
                }

                if (request.status == RequestStatus.ACTIVE) {
                    Button(onClick = onMarkResolved) {
                        Text("Mark as Resolved")
                    }
                }
            }
        }
    }
}
