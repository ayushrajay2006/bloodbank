package com.example.bloodbank.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.bloodbank.CoreDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

object BluetoothRelayManager {

    var isRelayActive by mutableStateOf(false)
        private set

    var statusLog by mutableStateOf("Offline")
        private set

    private var relayJob: Job? = null
    private var server: BluetoothServer? = null
    private var client: BluetoothClient? = null

    @SuppressLint("MissingPermission")
    fun start(context: Context) {
        if (isRelayActive) return

        val adapter = BluetoothAdapter.getDefaultAdapter()
        val db = CoreDatabase.getDatabase(context)

        server = BluetoothServer(adapter, db)
        client = BluetoothClient(adapter)

        isRelayActive = true
        statusLog = "Starting Auto-Relay..."

        relayJob = CoroutineScope(Dispatchers.IO).launch {
            // 1. Start Server (Listening)
            server?.startListening { status ->
                statusLog = status
            }

            // 2. Start Client Loop (Broadcasting)
            while (isActive) {
                // Get fresh data from DB
                val myRequests = db.emergencyRequestDao().getAllRequests().first()

                if (myRequests.isNotEmpty()) {
                    // Send to all devices
                    client?.sendToAllPairedDevices(myRequests) { status ->
                        // Only update log if significant
                        if (status.contains("Synced") || status.contains("Error")) {
                            statusLog = status
                        }
                    }
                }

                // 👇 CHANGED: Wait 2 MINUTES (120,000ms) instead of 30s
                // This saves massive battery and stops the loop spam
                delay(15000)
            }
        }
    }

    fun stop() {
        isRelayActive = false
        statusLog = "Relay Stopped."
        server?.stop()
        relayJob?.cancel()
        relayJob = null
    }
}