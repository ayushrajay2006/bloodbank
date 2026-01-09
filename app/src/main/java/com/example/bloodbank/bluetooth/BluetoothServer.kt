package com.example.bloodbank.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.bloodbank.CoreDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

class BluetoothServer(
    private val adapter: BluetoothAdapter?,
    private val database: CoreDatabase
) {

    @SuppressLint("MissingPermission")
    fun start(onStatus: (String) -> Unit) {
        if (adapter == null) {
            onStatus("Error: No Bluetooth Adapter")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) { onStatus("Starting Server...") }

                val serverSocket: BluetoothServerSocket =
                    adapter.listenUsingRfcommWithServiceRecord("EmergencyRelay", APP_UUID)

                withContext(Dispatchers.Main) { onStatus("Waiting for connection...") }

                val socket: BluetoothSocket = serverSocket.accept()

                withContext(Dispatchers.Main) { onStatus("Connected! Reading data...") }

                val reader = BufferedReader(InputStreamReader(socket.inputStream))

                // 👇 FIX: Try to read. If it fails but we got data, keep going.
                var json: String? = null
                try {
                    json = reader.readLine()
                } catch (e: IOException) {
                    // This often happens when client disconnects fast.
                    // If json is null here, we truly failed. If we got bytes, we might be ok.
                    Log.e("BluetoothServer", "Socket disconnect during read: ${e.message}")
                }

                if (!json.isNullOrEmpty()) {
                    Log.d("BluetoothServer", "Payload: $json")

                    try {
                        val receivedRequests = deserializeRequests(json.trim())
                        receivedRequests.forEach { req ->
                            database.emergencyRequestDao().insert(req)
                        }
                        withContext(Dispatchers.Main) { onStatus("Success: Received ${receivedRequests.size} requests!") }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { onStatus("Error: Corrupted Data Received") }
                    }

                } else {
                    withContext(Dispatchers.Main) { onStatus("Error: Connection lost or empty data") }
                }

                try {
                    socket.close()
                    serverSocket.close()
                } catch (e: Exception) { /* Ignore close errors */ }

            } catch (e: Exception) {
                Log.e("BluetoothServer", "Error", e)
                withContext(Dispatchers.Main) { onStatus("Server Status: ${e.message}") }
            }
        }
    }
}