package com.example.bloodbank

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_banks")
data class BloodBank(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val area: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val bloodGroups: String
)