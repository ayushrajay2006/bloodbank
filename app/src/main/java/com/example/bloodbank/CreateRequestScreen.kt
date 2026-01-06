package com.example.bloodbank.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    var selectedBloodGroup by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var locationText by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Create Emergency Request", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Blood group dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBloodGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bloodGroups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = {
                            selectedBloodGroup = group
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location field
        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            label = { Text("Location / Hospital") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Use current location button
        Button(
            onClick = {
                fetchLocation(context, fusedLocationClient) { location ->
                    locationText = location
                }
            }
        ) {
            Text("Use current location")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Extra instructions
        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Extra Instructions (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (selectedBloodGroup.isNotBlank() && locationText.isNotBlank()) {
                    val request = EmergencyRequest(
                        bloodGroup = selectedBloodGroup,
                        location = locationText,
                        instructions = instructions
                    )
                    RequestRepository.add(request)
                    onBack()
                }
            }
        ) {
            Text("Send Emergency Request")
        }
    }
}
