package com.securevision.core.domain.model

/**
 * Represents a monitored person/entity profile in the system.
 */
data class Profile(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val imagePath: String? = null,
    val isWatchlisted: Boolean = false,
    val accessLevel: AccessLevel = AccessLevel.NONE,
    val createdAt: Long,
    val updatedAt: Long,
    val metadata: Map<String, String> = emptyMap(),
    val embedding: FloatArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false
        return id == other.id && name == other.name && description == other.description &&
            imagePath == other.imagePath && isWatchlisted == other.isWatchlisted &&
            accessLevel == other.accessLevel && createdAt == other.createdAt &&
            updatedAt == other.updatedAt && metadata == other.metadata &&
            embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + isWatchlisted.hashCode()
        result = 31 * result + accessLevel.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}

enum class AccessLevel {
    NONE,
    RESTRICTED,
    STANDARD,
    ELEVATED,
    ADMIN
}
