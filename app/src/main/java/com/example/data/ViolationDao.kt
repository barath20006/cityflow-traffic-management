package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ViolationDao {

    @Query("SELECT * FROM speed_violations ORDER BY timestamp DESC")
    fun getAllViolations(): Flow<List<ViolationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViolation(entity: ViolationEntity)

    @Update
    suspend fun updateViolation(entity: ViolationEntity)

    @Query("UPDATE speed_violations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM speed_violations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM speed_violations")
    suspend fun clearAll()
}
