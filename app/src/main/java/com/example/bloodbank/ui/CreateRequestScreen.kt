package com.example.bloodbank.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository
import com.example.bloodbank.RequestStatus
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { RequestRepository(context) }
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State Variables
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var selectedBloodGroup by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }

    // Helper: Get Address from Coordinates
    fun getAddressFromLocation(lat: Double, lon: Double) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Uses the full address line for better precision
                    val fullAddress = address.getAddressLine(0) ?: "Lat: $lat, Lon: $lon"
                    withContext(Dispatchers.Main) {
                        locationText = fullAddress
                        isLocating = false
                    }
                } else {
                    withContext(Dispatchers.Main) { locationText = "Lat: $lat, Lon: $lon"; isLocating = false }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { locationText = "Lat: $lat, Lon: $lon"; isLocating = false }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                isLocating = true
                fetchLocation(fusedLocationClient) { loc ->
                    if (loc != null) getAddressFromLocation(loc.latitude, loc.longitude)
                    else { isLocating = false; locationText = "Location not found" }
                }
            } else { isLocating = false }
        }
    )

    // 👇 SCAFFOLD: Holds the Top Bar in place
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Emergency Request", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = OffWhite
    ) { padding ->
        // Content inside the Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Crucial: Respects the Top Bar space
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Patient Details", style = MaterialTheme.typography.labelLarge, color = MedicalRed)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedBloodGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Blood Group Required") },
                    leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null, tint = MedicalRed) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalRed)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    bloodGroups.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { selectedBloodGroup = it; expanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Location", style = MaterialTheme.typography.labelLarge, color = MedicalRed)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Hospital / Location") },
                placeholder = { Text("e.g. Apollo Hospital") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MedicalRed) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalRed)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        isLocating = true
                        fetchLocation(fusedLocationClient) { loc ->
                            if (loc != null) getAddressFromLocation(loc.latitude, loc.longitude)
                            else { isLocating = false; locationText = "Error" }
                        }
                    } else {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MedicalRed.copy(alpha = 0.1f), contentColor = MedicalRed)
            ) {
                if (isLocating) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use My Current Location")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Contact Info", style = MaterialTheme.typography.labelLarge, color = MedicalRed)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Your Phone Number") },
                placeholder = { Text("e.g. +91 98765 43210") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MedicalRed) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalRed)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Additional Info", style = MaterialTheme.typography.labelLarge, color = MedicalRed)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Notes for Donor") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalRed)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedBloodGroup.isNotBlank() && locationText.isNotBlank() && contactNumber.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            repository.addRequest(
                                EmergencyRequest(
                                    bloodGroup = selectedBloodGroup,
                                    location = locationText,
                                    instructions = instructions,
                                    contactNumber = contactNumber,
                                    status = RequestStatus.ACTIVE,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            withContext(Dispatchers.Main) { onBack() }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BROADCAST REQUEST", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(client: FusedLocationProviderClient, onResult: (Location?) -> Unit) {
    try {
        client.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) onResult(location)
            else {
                val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { loc: Location? -> onResult(loc) }
                    .addOnFailureListener { onResult(null) }
            }
        }.addOnFailureListener { onResult(null) }
    } catch (e: SecurityException) { onResult(null) }
}