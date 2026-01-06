package com.example.bloodbank

data class EmergencyRequest(
    val bloodGroup: String,
    val location: String,
    val instructions: String
)
