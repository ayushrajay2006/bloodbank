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
                // Open the "Door" once
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("EmergencyRelay", APP_UUID)
                withContext(Dispatchers.Main) { onStatus("Auto-Relay Active: Waiting for connection...") }

                // Keep accepting connections forever
                while (isActive && shouldLoop) {
                    try {
                        // This blocks until a phone connects
                        val socket: BluetoothSocket = serverSocket.accept()

                        // We got a connection!
                        Log.d("BluetoothServer", "Incoming connection accepted")

                        // Handle the data
                        val reader = BufferedReader(InputStreamReader(socket.inputStream))
                        val json = reader.readLine()

                        if (!json.isNullOrEmpty()) {
                            val requests = deserializeRequests(json.trim())
                            requests.forEach { req -> database.emergencyRequestDao().insert(req) }

                            withContext(Dispatchers.Main) {
                                onStatus("Received ${requests.size} requests! Still listening...")
                            }
                        }

                        socket.close()

                    } catch (e: Exception) {
                        Log.e("BluetoothServer", "Connection Error", e)
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