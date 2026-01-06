package com.example.bloodbank

import android.content.Context
import kotlinx.coroutines.flow.Flow

class RequestRepository(context: Context) {

    private val dao = EmergencyDatabase
        .getDatabase(context)
        .emergencyRequestDao()

    suspend fun addRequest(request: EmergencyRequest) {
        dao.insert(request)
    }

    fun getAllRequests(): Flow<List<EmergencyRequest>> {
        return dao.getAllRequests()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
