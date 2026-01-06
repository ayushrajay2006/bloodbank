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

    @Query("DELETE FROM emergency_requests")
    suspend fun clearAll()
}
