package com.example.bloodbank.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.bloodbank.CoreDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class BluetoothServer(
    private val adapter: BluetoothAdapter?,
    private val database: CoreDatabase
) {
    private var serverJob: Job? = null
    private var shouldLoop = true

    @SuppressLint("MissingPermission")
    fun startListening(onStatus: (String) -> Unit) {
        if (adapter == null) return
        shouldLoop = true

        serverJob = CoroutineScope(Dispatchers.IO).launch {
            var serverSocket: BluetoothServerSocket? = null

            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("EmergencyRelay", APP_UUID)
                withContext(Dispatchers.Main) { onStatus("Auto-Relay Active: Waiting for connection...") }

                while (isActive && shouldLoop) {
                    try {
                        val socket: BluetoothSocket = serverSocket.accept()

                        // ... (Connection Handling logic is same as before) ...
                        val reader = BufferedReader(InputStreamReader(socket.inputStream))
                        val json = reader.readLine()

                        if (!json.isNullOrEmpty()) {
                            val receivedRequests = deserializeRequests(json.trim())
                            val existingRequests = database.emergencyRequestDao().getAllRequests().first()
                            var newCount = 0

                            receivedRequests.forEach { incoming ->
                                val isDuplicate = existingRequests.any { existing ->
                                    existing.timestamp == incoming.timestamp &&
                                            existing.contactNumber == incoming.contactNumber
                                }
                                if (!isDuplicate) {
                                    database.emergencyRequestDao().insert(incoming)
                                    newCount++
                                }
                            }

                            if (newCount > 0) {
                                withContext(Dispatchers.Main) { onStatus("Synced $newCount new requests!") }
                            }
                        }
                        socket.close()

                    } catch (e: Exception) {
                        Log.e("BluetoothServer", "Connection Error", e)
                        // 👇 NEW SAFETY DELAY:
                        // If accept() fails, wait 1s before retrying to prevent CPU overheating
                        delay(1000)
                    }
                }
            } catch (e: Exception) {
                Log.e("BluetoothServer", "Fatal Server Error", e)
                withContext(Dispatchers.Main) { onStatus("Server Error: ${e.message}") }
            } finally {
                serverSocket?.close()
            }
        }
    }

    fun stop() {
        shouldLoop = false
        serverJob?.cancel()
    }
}