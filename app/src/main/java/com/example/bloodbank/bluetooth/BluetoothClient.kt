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
import java.io.IOException

class BluetoothClient(
    private val adapter: BluetoothAdapter?
) {

    @SuppressLint("MissingPermission")
    fun sendToAllPairedDevices(
        requests: List<EmergencyRequest>,
        onStatus: (String) -> Unit
    ) {
        if (adapter == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
            if (pairedDevices.isEmpty()) {
                withContext(Dispatchers.Main) { onStatus("No paired devices found to sync with.") }
                return@launch
            }

            withContext(Dispatchers.Main) { onStatus("Auto-Syncing with ${pairedDevices.size} devices...") }

            // Loop through EVERY paired device and try to connect
            for (device in pairedDevices) {
                try {
                    Log.d("BluetoothClient", "Trying to connect to ${device.name}")

                    // Create socket
                    val socket = device.createRfcommSocketToServiceRecord(APP_UUID)
                    socket.connect() // This blocks until connected or fails

                    // If we get here, we are connected!
                    val payload = serializeRequests(requests)
                    // Ensure newline for the server reader
                    val finalPayload = if (payload.endsWith("\n")) payload else payload + "\n"

                    val writer = OutputStreamWriter(socket.outputStream)
                    writer.write(finalPayload)
                    writer.flush()

                    // Small delay to ensure transmission
                    Thread.sleep(500)
                    socket.close()

                    withContext(Dispatchers.Main) { onStatus("✓ Synced with ${device.name}") }

                } catch (e: IOException) {
                    // This is normal! If the device is not nearby, connect() will fail.
                    // We just ignore it and try the next device.
                    Log.d("BluetoothClient", "${device.name} not in range.")
                } catch (e: Exception) {
                    Log.e("BluetoothClient", "Error syncing with ${device.name}", e)
                }
            }

            withContext(Dispatchers.Main) { onStatus("Sync Cycle Complete. Listening...") }
        }
    }
}