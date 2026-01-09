package com.example.bloodbank.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.example.bloodbank.EmergencyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

class BluetoothClient(
    private val adapter: BluetoothAdapter?
) {

    @SuppressLint("MissingPermission")
    fun sendRequests(
        requests: List<EmergencyRequest>,
        onStatus: (String) -> Unit
    ) {
        if (adapter == null) {
            onStatus("Error: No Bluetooth Adapter")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) { onStatus("Looking for paired device...") }

                val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
                if (pairedDevices.isEmpty()) {
                    withContext(Dispatchers.Main) { onStatus("Error: No paired devices. Pair phones in Settings.") }
                    return@launch
                }

                val device = pairedDevices.first()
                withContext(Dispatchers.Main) { onStatus("Connecting to ${device.name}...") }

                adapter.cancelDiscovery()

                val socket = device.createRfcommSocketToServiceRecord(APP_UUID)
                socket.connect()

                withContext(Dispatchers.Main) { onStatus("Connected! Sending Data...") }

                // Get string and ensure it ends with Newline for readLine() to work
                var payload = serializeRequests(requests)
                if (!payload.endsWith("\n")) {
                    payload += "\n"
                }

                val writer = OutputStreamWriter(socket.outputStream)
                writer.write(payload)
                writer.flush()

                // Wait for data to fly over the air
                withContext(Dispatchers.Main) { onStatus("Sending... Keep phones close.") }
                Thread.sleep(2000)

                socket.close()

                withContext(Dispatchers.Main) { onStatus("Success: Sent to ${device.name}!") }

            } catch (e: Exception) {
                Log.e("BluetoothClient", "Error", e)
                withContext(Dispatchers.Main) { onStatus("Failed: ${e.message}") }
            }
        }
    }
}