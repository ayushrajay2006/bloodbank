package com.example.bloodbank

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

// --- 1. ENUMS ---
enum class RequestStatus {
    PENDING,
    ACCEPTED,
    COMPLETED, // This equals "Resolved"
    ACTIVE,
    CRITICAL   // 👈 ADDED NEW STATUS
}

class Converters {
    @TypeConverter
    fun fromStatus(status: RequestStatus): String = status.name
    @TypeConverter
    fun toStatus(value: String): RequestStatus = RequestStatus.valueOf(value)
}

// --- 2. ENTITIES ---
@Entity(tableName = "emergency_requests")
data class EmergencyRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bloodGroup: String,
    val location: String,
    val instructions: String,
    val contactNumber: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 3. DAOs ---
@Dao
interface EmergencyRequestDao {
    @Insert
    suspend fun insert(request: EmergencyRequest)

    @Query("SELECT * FROM emergency_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<EmergencyRequest>>

    @Query("SELECT * FROM emergency_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Int): EmergencyRequest?

    // 👇 NEW FUNCTION TO UPDATE STATUS
    @Query("UPDATE emergency_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: RequestStatus)
}

@Dao
interface BloodBankDao {
    @Query("SELECT * FROM blood_banks")
    fun getAllBloodBanks(): Flow<List<BloodBank>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banks: List<BloodBank>)

    @Query("SELECT COUNT(*) FROM blood_banks")
    suspend fun count(): Int
}

// --- 4. MAIN DATABASE ---
@Database(
    entities = [EmergencyRequest::class, BloodBank::class],
    version = 5, // 👈 BUMPED VERSION TO 5
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CoreDatabase : RoomDatabase() {

    abstract fun emergencyRequestDao(): EmergencyRequestDao
    abstract fun bloodBankDao(): BloodBankDao

    companion object {
        @Volatile
        private var INSTANCE: CoreDatabase? = null

        fun getDatabase(context: Context): CoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoreDatabase::class.java,
                    "blood_relay_core_db_v5" // 👈 Renamed file to be safe
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}