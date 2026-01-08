package com.example.bloodbank.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository
import com.google.android.gms.location.FusedLocationProviderClient
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, now fetch location
                fetchLocation(fusedLocationClient) { locationString ->
                    locationText = locationString
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Create Emergency Request", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedBloodGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Group") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchLocation(fusedLocationClient) { locationString ->
                    locationText = locationString
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
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

private fun fetchLocation(client: FusedLocationProviderClient, onResult: (String) -> Unit) {
    try {
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onResult("Lat: ${location.latitude}, Lon: ${location.longitude}")
            } else {
                onResult("N/A")
            }
        }.addOnFailureListener {
            onResult("Error fetching location")
        }
    } catch (e: SecurityException) {
        onResult("Location permission not granted")
    }
}
