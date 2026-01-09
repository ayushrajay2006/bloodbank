package com.example.bloodbank.map

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bloodbank.BloodBank
import com.example.bloodbank.R
import com.example.bloodbank.ui.theme.MedicalRed
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodBankMapScreen(
    context: Context,
    userLocation: Location?,
    bloodBanks: List<BloodBank>,
    onBack: () -> Unit
) {
    // State to track which bank is clicked
    var selectedBank by remember { mutableStateOf<BloodBank?>(null) }

    // Logic to find the closest blood bank
    val closestBank = remember(userLocation, bloodBanks) {
        if (userLocation == null || bloodBanks.isEmpty()) null
        else {
            bloodBanks.minByOrNull { bank ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    bank.latitude, bank.longitude,
                    results
                )
                results[0]
            }
        }
    }

    // Helper to launch Google Maps Navigation
    fun launchNavigation(lat: Double, lng: Double) {
        val uri = Uri.parse("google.navigation:q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback if Maps app isn't installed
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blood Banks Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            // Show "Navigate to Closest" ONLY if no specific bank is selected
            if (selectedBank == null && closestBank != null) {
                ExtendedFloatingActionButton(
                    onClick = { launchNavigation(closestBank.latitude, closestBank.longitude) },
                    containerColor = MedicalRed,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Navigation, null) },
                    text = { Text("Navigate to Closest") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        val startPoint = if (userLocation != null) {
                            GeoPoint(userLocation.latitude, userLocation.longitude)
                        } else {
                            GeoPoint(17.4399, 78.4983)
                        }

                        controller.setZoom(14.0)
                        controller.setCenter(startPoint)

                        // 1. YOUR LOCATION
                        if (userLocation != null) {
                            val userMarker = Marker(this)
                            userMarker.position = GeoPoint(userLocation.latitude, userLocation.longitude)
                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            userMarker.title = "You are here"

                            val userIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_my_location_24)
                            if (userIcon != null) userMarker.icon = userIcon

                            overlays.add(userMarker)
                        }

                        // 2. BLOOD BANK PINS
                        bloodBanks.forEach { bank ->
                            val marker = Marker(this)
                            marker.position = GeoPoint(bank.latitude, bank.longitude)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = bank.name

                            val hospitalIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_hospital_24)
                            if (hospitalIcon != null) marker.icon = hospitalIcon

                            // 👇 ON CLICK: Select the bank (Shows the Card)
                            marker.setOnMarkerClickListener { _, _ ->
                                selectedBank = bank
                                true // Return true to consume event (hides default bubble)
                            }

                            overlays.add(marker)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 👇 3. BOTTOM DETAILS CARD (Shows when a pin is clicked)
            if (selectedBank != null) {
                val bank = selectedBank!!
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bank.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(bank.area, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                            IconButton(onClick = { selectedBank = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = MedicalRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (userLocation != null) {
                                    val res = FloatArray(1)
                                    Location.distanceBetween(userLocation.latitude, userLocation.longitude, bank.latitude, bank.longitude, res)
                                    "${"%.1f".format(res[0] / 1000)} km away"
                                } else "Distance unknown",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { launchNavigation(bank.latitude, bank.longitude) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                        ) {
                            Icon(Icons.Default.Navigation, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate Here")
                        }
                    }
                }
            }
        }
    }
}