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
    val metadata: Map<String, String> = emptyMap()
)

enum class AccessLevel {
    NONE,
    RESTRICTED,
    STANDARD,
    ELEVATED,
    ADMIN
}
