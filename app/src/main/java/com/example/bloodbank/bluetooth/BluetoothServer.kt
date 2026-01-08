package com.example.bloodbank.bluetooth
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothServerSocket
//import android.bluetooth.BluetoothSocket
//import android.util.Log
//import com.example.bloodbank.`EmergencyDatabase.kt`
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
//class BluetoothServer(
//    private val adapter: BluetoothAdapter,
//    private val database: `EmergencyDatabase.kt`
//) {
//
//    fun start() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                Log.d("BluetoothServer", "Starting server socket")
//
//                val serverSocket: BluetoothServerSocket =
//                    adapter.listenUsingRfcommWithServiceRecord(
//                        "EmergencyRelay",
//                        APP_UUID
//                    )
//
//                Log.d("BluetoothServer", "Waiting for connection...")
//
//                val socket: BluetoothSocket = serverSocket.accept()
//                Log.d("BluetoothServer", "Client connected")
//
//                val reader = BufferedReader(
//                    InputStreamReader(socket.inputStream)
//                )
//
//                val json = reader.readLine()
//                Log.d("BluetoothServer", "Received raw payload: $json")
//
//                socket.close()
//                serverSocket.close()
//            } catch (e: Exception) {
//                Log.e("BluetoothServer", "Server failed", e)
//            }
//        }
//    }
//}
