package com.example.bloodbank.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloodbank.ui.theme.MedicalRed
import com.example.bloodbank.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DengueAssistantScreen(
    onBack: () -> Unit,
    onFindHelp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dengue Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. WARNING BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEF6C00))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Dengue cases are rising in your area. Stay vigilant.",
                        color = Color(0xFFEF6C00),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Common Symptoms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // 2. SYMPTOMS LIST
            SymptomItem("Sudden High Fever (104°F)")
            SymptomItem("Severe Joint & Muscle Pain")
            SymptomItem("Pain Behind the Eyes")
            SymptomItem("Skin Rash (Red Spots)")
            SymptomItem("Nausea & Vomiting")

            Spacer(modifier = Modifier.height(24.dp))

            // 3. DO'S AND DON'TS
            Text("Immediate Care Guide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            CareCard(
                title = "Hydrate Well",
                desc = "Drink plenty of water, ORS, and coconut water.",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CareCard(
                title = "Avoid Aspirin/Ibuprofen",
                desc = "These can increase bleeding risk. Use Paracetamol only.",
                icon = Icons.Default.Warning,
                iconColor = MedicalRed
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. ACTION BUTTON
            Button(
                onClick = onFindHelp,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocalHospital, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Find Platelet Donors & Hospitals")
            }
        }
    }
}

@Composable
fun SymptomItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(8.dp).background(MedicalRed, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp)
    }
}

@Composable
fun CareCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}