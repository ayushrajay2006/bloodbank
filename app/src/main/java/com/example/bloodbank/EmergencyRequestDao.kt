package com.example.bloodbank

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: EmergencyRequest)

    @Query("SELECT * FROM emergency_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<EmergencyRequest>>

    @Query("UPDATE emergency_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: RequestStatus)

    @Query("DELETE FROM emergency_requests")
    suspend fun clearAll()

    @Query("SELECT * FROM emergency_requests")
    suspend fun getAllRequestsOnce(): List<EmergencyRequest>

}
