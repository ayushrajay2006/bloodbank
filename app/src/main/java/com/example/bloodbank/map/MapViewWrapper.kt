package com.example.bloodbank.map

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bloodbank.BloodBank
import com.example.bloodbank.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewWrapper(
    userLocation: Location?,
    bloodBanks: List<BloodBank>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userColor = Color.parseColor("#2196F3") // Blue
    val bankColor = Color.parseColor("#D32F2F") // Red

    // Default to Secunderabad if no GPS
    val startPoint = if (userLocation != null) {
        GeoPoint(userLocation.latitude, userLocation.longitude)
    } else {
        GeoPoint(17.4344, 78.5000)
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(startPoint)
            }
        },
        update = { map ->
            map.overlays.clear()

            fun getTintedIcon(drawableId: Int, color: Int): Drawable? {
                val drawable = ContextCompat.getDrawable(context, drawableId)?.mutate()
                drawable?.setTint(color)
                return drawable
            }

            // 1. User Location Pin
            if (userLocation != null) {
                val userMarker = Marker(map).apply {
                    position = GeoPoint(userLocation.latitude, userLocation.longitude)
                    title = "My Location"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = getTintedIcon(R.drawable.ic_my_location_24, userColor)
                }
                map.overlays.add(userMarker)
            }

            // 2. Blood Bank Pins (With Click-to-Navigate)
            bloodBanks.forEach { bank ->
                val bankMarker = Marker(map).apply {
                    position = GeoPoint(bank.latitude, bank.longitude)
                    title = bank.name
                    snippet = "Opening Google Maps..." // Feedback to user
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = getTintedIcon(R.drawable.ic_hospital_24, bankColor)

                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()

                        // Show Toast
                        Toast.makeText(context, "Navigating to ${bank.name}...", Toast.LENGTH_SHORT).show()

                        // Small delay so user sees the bubble, then launch maps
                        Handler(Looper.getMainLooper()).postDelayed({
                            val gmmIntentUri = Uri.parse("google.navigation:q=${bank.latitude},${bank.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                // Fallback to browser
                                val browserIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(browserIntent)
                            }
                        }, 1000) // 1 second delay
                        true
                    }
                }
                map.overlays.add(bankMarker)
            }
            map.invalidate()
        }
    )
}