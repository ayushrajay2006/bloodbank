package com.example.bloodbank.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestStatus // 👈 This import is now valid!
import com.example.bloodbank.ui.theme.MedicalRed

@Composable
fun EmergencyRequestCard(request: EmergencyRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Group: ${request.bloodGroup}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MedicalRed
                )

                // Status Badge
                Surface(
                    color = when(request.status) {
                        RequestStatus.ACCEPTED -> Color(0xFFE8F5E9) // Light Green
                        RequestStatus.COMPLETED -> Color.LightGray
                        else -> Color(0xFFFFF3E0) // Light Orange for Pending
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = request.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Location: ${request.location}", style = MaterialTheme.typography.bodyMedium)
            if (request.instructions.isNotBlank()) {
                Text("Note: ${request.instructions}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}