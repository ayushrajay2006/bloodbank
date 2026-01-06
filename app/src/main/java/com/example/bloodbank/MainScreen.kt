package com.example.bloodbank

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloodbank.ui.CreateRequestScreen

@Composable
fun MainScreen() {

    var showCreate by remember { mutableStateOf(false) }

    if (showCreate) {
        CreateRequestScreen(
            onBack = { showCreate = false }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Emergency Relay", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { showCreate = true }) {
                Text("Create Emergency Request")
            }
        }
    }
}
