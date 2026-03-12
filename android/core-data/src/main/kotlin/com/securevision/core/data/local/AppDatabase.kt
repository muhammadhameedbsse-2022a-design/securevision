package com.securevision.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = true
)
@TypeConverters(MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun detectionEventDao(): DetectionEventDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "securevision.db"

        /** Migration from v2 to v3: add age, gender, photoURI columns. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN age INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE profiles ADD COLUMN gender TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE profiles ADD COLUMN photoURI TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE detection_events ADD COLUMN age INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE detection_events ADD COLUMN gender TEXT DEFAULT NULL")
            }
        }
    }
}
