package com.example.bloodbank

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_requests")
data class EmergencyRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bloodGroup: String,
    val location: String,
    val instructions: String,
    val timestamp: Long = System.currentTimeMillis()
)
