package com.example.bloodbank

import android.content.Context
import kotlinx.coroutines.flow.Flow

class RequestRepository(context: Context) {

    // 👇 UPDATED TO USE CoreDatabase
    private val database = CoreDatabase.getDatabase(context)
    private val requestDao = database.emergencyRequestDao()

    suspend fun addRequest(request: EmergencyRequest) {
        requestDao.insert(request)
    }

    fun getAllRequests(): Flow<List<EmergencyRequest>> {
        return requestDao.getAllRequests()
    }
}