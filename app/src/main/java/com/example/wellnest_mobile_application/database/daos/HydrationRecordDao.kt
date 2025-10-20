package com.example.wellnest_mobile_application.database.daos

import androidx.room.*
import com.example.wellnest_mobile_application.database.entities.HydrationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationRecordDao {
    
    @Query("SELECT * FROM hydration_records ORDER BY createdAt DESC")
    fun getAllHydrationRecords(): Flow<List<HydrationRecordEntity>>
    
    @Query("SELECT * FROM hydration_records WHERE id = :recordId")
    suspend fun getHydrationRecordById(recordId: Int): HydrationRecordEntity?
    
    @Query("SELECT * FROM hydration_records WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getHydrationRecordsByDate(date: String): List<HydrationRecordEntity>
    
    @Query("SELECT * FROM hydration_records WHERE date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getHydrationRecordsByDateRange(startDate: String, endDate: String): List<HydrationRecordEntity>
    
    @Query("SELECT SUM(amount) FROM hydration_records WHERE date = :date")
    suspend fun getTotalWaterIntakeByDate(date: String): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydrationRecord(record: HydrationRecordEntity): Long
    
    @Update
    suspend fun updateHydrationRecord(record: HydrationRecordEntity)
    
    @Delete
    suspend fun deleteHydrationRecord(record: HydrationRecordEntity)
    
    @Query("DELETE FROM hydration_records WHERE id = :recordId")
    suspend fun deleteHydrationRecordById(recordId: Int)
    
    @Query("SELECT COUNT(*) FROM hydration_records")
    suspend fun getTotalHydrationRecordsCount(): Int
    
    @Query("SELECT COUNT(*) FROM hydration_records WHERE date = :date")
    suspend fun getHydrationRecordsCountByDate(date: String): Int
    
    @Query("SELECT DISTINCT date FROM hydration_records ORDER BY date DESC")
    suspend fun getAllHydrationDates(): List<String>
    
    @Query("SELECT SUM(amount) FROM hydration_records")
    suspend fun getTotalWaterIntake(): Int?
    
    @Query("DELETE FROM hydration_records")
    suspend fun deleteAllHydrationRecords()
}
