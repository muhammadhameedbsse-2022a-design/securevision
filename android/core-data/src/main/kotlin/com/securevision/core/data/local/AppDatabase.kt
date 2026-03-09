package com.securevision.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.securevision.core.data.local.dao.AlertDao
import com.securevision.core.data.local.dao.DetectionEventDao
import com.securevision.core.data.local.dao.ProfileDao
import com.securevision.core.data.local.entity.AlertEntity
import com.securevision.core.data.local.entity.DetectionEventEntity
import com.securevision.core.data.local.entity.MapConverter
import com.securevision.core.data.local.entity.ProfileEntity

@Database(
    entities = [
        AlertEntity::class,
        DetectionEventEntity::class,
        ProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun detectionEventDao(): DetectionEventDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "securevision.db"
    }
}
