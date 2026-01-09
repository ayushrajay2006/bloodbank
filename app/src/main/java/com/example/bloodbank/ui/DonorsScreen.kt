package com.example.bloodbank.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 👈 Back Icon
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloodbank.CoreDatabase
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestStatus
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = CoreDatabase.getDatabase(context)
    val requests by db.emergencyRequestDao().getAllRequests().collectAsState(initial = emptyList())

    // Filter State
    var myBloodGroup by remember { mutableStateOf("All") }
    val bloodGroups = listOf("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var expanded by remember { mutableStateOf(false) }

    // 👇 UPDATED FILTER LOGIC: Now includes CRITICAL
    val filteredRequests = requests.filter {
        (it.status == RequestStatus.ACTIVE ||
                it.status == RequestStatus.PENDING ||
                it.status == RequestStatus.CRITICAL) && // 👈 Added this!
                (myBloodGroup == "All" || it.bloodGroup == myBloodGroup)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donate Blood", fontWeight = FontWeight.Bold) },
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
            Text("Find a match & save a life", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // BLOOD GROUP FILTER
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "My Blood Group: $myBloodGroup",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    bloodGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = { myBloodGroup = group; expanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active or critical requests found.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredRequests) { req ->
                        DonorRequestCard(req) {
                            val cleanNumber = req.contactNumber.replace(Regex("[^0-9+]"), "").let {
                                if (!it.startsWith("+")) "+91$it" else it
                            }
                            val message = "Hi! I saw your request for ${req.bloodGroup} blood at ${req.location}. I am a match."

                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:$cleanNumber")
                                putExtra("sms_body", message)
                            }
                            try {
                                context.startActivity(smsIntent)
                            } catch (e: Exception) {
                                val fallback = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("sms:$cleanNumber")
                                    putExtra("sms_body", message)
                                }
                                context.startActivity(fallback)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonorRequestCard(req: EmergencyRequest, onConnect: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if(req.status == RequestStatus.CRITICAL) MedicalRed else MedicalRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        req.bloodGroup,
                        modifier = Modifier.padding(8.dp),
                        color = if(req.status == RequestStatus.CRITICAL) Color.White else MedicalRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Requirement", fontWeight = FontWeight.Bold)
                        if(req.status == RequestStatus.CRITICAL) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CRITICAL", color = MedicalRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(req.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if(req.instructions.isNotBlank()) {
                Text("Note: ${req.instructions}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connect via SMS")
            }
        }
    }
}