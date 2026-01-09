package com.example.bloodbank.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bloodbank.BloodBank
import com.example.bloodbank.CoreDatabase
import com.example.bloodbank.map.BloodBankMapScreen
import com.example.bloodbank.ui.theme.OffWhite
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodBankScreen(onBack: () -> Unit) { // Added onBack parameter
    val context = LocalContext.current
    val database = CoreDatabase.getDatabase(context)

    // 1. Get ALL banks first
    val allBanks by database.bloodBankDao().getAllBloodBanks().collectAsState(initial = emptyList())

    var showMap by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    // 2. FILTER LOGIC: Only keep banks within 50km
    val filteredBanks = remember(allBanks, userLocation) {
        if (userLocation == null) {
            allBanks
        } else {
            allBanks.filter { bank ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation!!.latitude, userLocation!!.longitude,
                    bank.latitude, bank.longitude,
                    results
                )
                results[0] <= 50000
            }.sortedBy { bank ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation!!.latitude, userLocation!!.longitude,
                    bank.latitude, bank.longitude,
                    results
                )
                results[0]
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            hasPermission = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) userLocation = loc
                }
                @SuppressLint("MissingPermission")
                val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener { loc ->
                    if (loc != null) userLocation = loc
                }
            } catch (e: SecurityException) { }
        }
    }

    if (showMap) {
        BloodBankMapScreen(
            context = context,
            userLocation = userLocation,
            bloodBanks = filteredBanks,
            onBack = { showMap = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nearby Centers", fontWeight = FontWeight.Bold) },
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
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

                if (userLocation == null) {
                    Text("Locating you...", color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Found ${filteredBanks.size} centers near you", color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map Button
                Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("View Nearby on Map")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredBanks) { bank ->
                        BloodBankItem(bank, userLocation)
                    }
                }
            }
        }
    }
}

@Composable
private fun BloodBankItem(bank: BloodBank, userLocation: Location?) {
    val context = LocalContext.current

    val distanceStr = if (userLocation != null) {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            bank.latitude, bank.longitude,
            results
        )
        String.format("%.1f km", results[0] / 1000)
    } else {
        "..."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = bank.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = bank.area, style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = distanceStr,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${bank.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Call") }

                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${bank.latitude},${bank.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Navigate") }
            }
        }
    }
}