package com.securevision.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.securevision.core.domain.model.AccessLevel
import com.securevision.core.domain.model.Profile

@Entity(tableName = "profiles")
@TypeConverters(MapConverter::class)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String,
    val imagePath: String?,
    val isWatchlisted: Boolean,
    val accessLevel: String,
    val createdAt: Long,
    val updatedAt: Long,
    val metadataJson: String = "{}",
    val embeddingData: String? = null
) {
    fun toDomain(): Profile {
        val metadata: Map<String, String> = try {
            val converter = MapConverter()
            converter.toMap(metadataJson)
        } catch (e: Exception) {
            emptyMap()
        }

        return Profile(
            id = id,
            name = name,
            description = description,
            imagePath = imagePath,
            isWatchlisted = isWatchlisted,
            accessLevel = AccessLevel.valueOf(accessLevel),
            createdAt = createdAt,
            updatedAt = updatedAt,
            metadata = metadata,
            embedding = embeddingData?.toFloatArray()
        )
    }

    companion object {
        fun fromDomain(profile: Profile): ProfileEntity {
            val converter = MapConverter()
            return ProfileEntity(
                id = profile.id,
                name = profile.name,
                description = profile.description,
                imagePath = profile.imagePath,
                isWatchlisted = profile.isWatchlisted,
                accessLevel = profile.accessLevel.name,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
                metadataJson = converter.fromMap(profile.metadata),
                embeddingData = profile.embedding?.toStorageString()
            )
        }

        private fun String.toFloatArray(): FloatArray? = try {
            if (isBlank()) null
            else split(",").map { it.trim().toFloat() }.toFloatArray()
        } catch (e: Exception) {
            null
        }

        private fun FloatArray.toStorageString(): String =
            joinToString(",") { String.format("%.6f", it) }
    }
}
