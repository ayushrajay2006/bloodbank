package com.example.bloodbank.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bloodbank.BloodBank
import com.example.bloodbank.EmergencyDatabase
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.buildEmergencyMessage

@Composable
fun BloodBankScreen(
    emergencyRequest: EmergencyRequest? = null
) {
    val context = LocalContext.current
    val database = EmergencyDatabase.getDatabase(context)

    val banks by database
        .bloodBankDao()
        .getAllBloodBanks()
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Nearby Blood Banks",
            style = MaterialTheme.typography.headlineMedium
        )

        if (emergencyRequest != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notifying banks for ${emergencyRequest.bloodGroup} at ${emergencyRequest.location}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(banks) { bank ->
                BloodBankItem(bank, emergencyRequest)
            }
        }
    }
}

@Composable
private fun BloodBankItem(
    bank: BloodBank,
    emergencyRequest: EmergencyRequest?
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(bank.name, style = MaterialTheme.typography.titleMedium)
            Text("Area: ${bank.area}")
            Text("Phone: ${bank.phone}")

            Spacer(modifier = Modifier.height(8.dp))

            // 📞 CALL BUTTON
            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${bank.phone}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Call Blood Bank")
            }

            // 📩 SEND EMERGENCY SMS (only if request exists)
            if (emergencyRequest != null) {
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val message = buildEmergencyMessage(emergencyRequest)
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${bank.phone}")
                            putExtra("sms_body", message)
                        }
                        context.startActivity(smsIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Emergency SMS")
                }
            }
        }
    }
}
