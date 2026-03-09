package com.securevision.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.securevision.core.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE severity = :severity ORDER BY timestamp DESC")
    fun getAlertsBySeverity(severity: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertById(id: Long): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("DELETE FROM alerts")
    suspend fun deleteAllAlerts()

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun getUnreadAlertCount(): Flow<Int>
}
