package com.example.bloodbank.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter

class BluetoothClient(
    private val adapter: BluetoothAdapter
) {

    fun sendTestMessage() {
        Log.d("BluetoothClient", "sendTestMessage() called")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
                Log.d("BluetoothClient", "Paired devices: ${pairedDevices.map { it.name }}")

                if (pairedDevices.isEmpty()) {
                    Log.e("BluetoothClient", "No paired devices")
                    return@launch
                }

                val device = pairedDevices.first()
                Log.d("BluetoothClient", "Connecting to ${device.name}")

                adapter.cancelDiscovery()

                val socket =
                    device.createRfcommSocketToServiceRecord(APP_UUID)

                socket.connect()
                Log.d("BluetoothClient", "Socket connected")

                val writer = OutputStreamWriter(socket.outputStream)
                writer.write("HELLO_FROM_CLIENT\n")
                writer.flush()

                Log.d("BluetoothClient", "Message sent")

                socket.close()
            } catch (e: Exception) {
                Log.e("BluetoothClient", "Client failed", e)
            }
        }
    }
}
