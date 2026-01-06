package com.example.bloodbank.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient

@SuppressLint("MissingPermission")
fun fetchLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onResult: (String) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Permission not granted — user can still type location manually
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val text = "Lat: ${location.latitude}, Lon: ${location.longitude}"
            onResult(text)
        }
    }
}
