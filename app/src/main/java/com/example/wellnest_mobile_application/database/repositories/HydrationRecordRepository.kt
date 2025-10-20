package com.example.wellnest_mobile_application.database.repositories

import com.example.wellnest_mobile_application.database.WellnestDatabase
import com.example.wellnest_mobile_application.database.daos.HydrationRecordDao
import com.example.wellnest_mobile_application.database.entities.HydrationRecordEntity
import com.example.wellnest_mobile_application.models.HydrationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HydrationRecordRepository(private val database: WellnestDatabase) {
    
    private val hydrationRecordDao: HydrationRecordDao = database.hydrationRecordDao()
    
    fun getHydrationRecords(): Flow<List<HydrationRecord>> {
        return hydrationRecordDao.getAllHydrationRecords().map { entities ->
            entities.map { entity ->
                HydrationRecord(
                    id = entity.id,
                    amount = entity.amount,
                    date = entity.date,
                    time = entity.time
                )
            }
        }
    }
    
    suspend fun saveHydrationRecord(record: HydrationRecord): Long {
        val hydrationRecordEntity = HydrationRecordEntity(
            amount = record.amount,
            date = record.date,
            time = record.time
        )
        return hydrationRecordDao.insertHydrationRecord(hydrationRecordEntity)
    }
    
    suspend fun getTodayWaterIntake(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return hydrationRecordDao.getTotalWaterIntakeByDate(today) ?: 0
    }
    
    suspend fun getHydrationRecordsByDate(date: String): List<HydrationRecord> {
        val entities = hydrationRecordDao.getHydrationRecordsByDate(date)
        return entities.map { entity ->
            HydrationRecord(
                id = entity.id,
                amount = entity.amount,
                date = entity.date,
                time = entity.time
            )
        }
    }
    
    suspend fun clearAllHydrationRecords() {
        hydrationRecordDao.deleteAllHydrationRecords()
    }
}
