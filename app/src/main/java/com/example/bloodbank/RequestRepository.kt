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

    suspend fun resolveRequest(id: Int) {
        dao.updateStatus(id, RequestStatus.RESOLVED)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
