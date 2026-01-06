package com.example.bloodbank.ui

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val repository = remember { RequestRepository(context) }
    val scope = rememberCoroutineScope()
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

        Text("Create Emergency Request", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBloodGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bloodGroups.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            selectedBloodGroup = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            label = { Text("Location / Hospital") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            fetchLocation(context, fusedLocationClient) {
                locationText = it
            }
        }) {
            Text("Use current location")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Extra instructions") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (selectedBloodGroup.isNotBlank() && locationText.isNotBlank()) {
                    scope.launch(Dispatchers.IO) {
                        repository.addRequest(
                            EmergencyRequest(
                                bloodGroup = selectedBloodGroup,
                                location = locationText,
                                instructions = instructions
                            )
                        )
                    }
                    onBack()
                }
            }
        ) {
            Text("Send Emergency Request")
        }
    }
}
