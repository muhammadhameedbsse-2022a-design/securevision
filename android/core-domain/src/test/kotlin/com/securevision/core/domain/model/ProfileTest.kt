package com.securevision.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileTest {

    @Test
    fun `profile with all fields`() {
        val profile = Profile(
            id = 1L,
            name = "John Doe",
            description = "Test user",
            imagePath = "/path/to/image",
            isWatchlisted = true,
            accessLevel = AccessLevel.ADMIN,
            createdAt = 1000L,
            updatedAt = 2000L,
            age = 30,
            gender = "Male",
            photoURI = "content://photo/1",
            metadata = mapOf("key" to "value"),
            embedding = floatArrayOf(0.1f, 0.2f, 0.3f)
        )

        assertEquals(1L, profile.id)
        assertEquals("John Doe", profile.name)
        assertEquals(30, profile.age)
        assertEquals("Male", profile.gender)
        assertEquals("content://photo/1", profile.photoURI)
    }

    @Test
    fun `profile defaults for optional fields`() {
        val profile = Profile(
            name = "Jane",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        assertEquals(0L, profile.id)
        assertEquals("", profile.description)
        assertNull(profile.imagePath)
        assertEquals(false, profile.isWatchlisted)
        assertEquals(AccessLevel.NONE, profile.accessLevel)
        assertNull(profile.age)
        assertNull(profile.gender)
        assertNull(profile.photoURI)
        assertEquals(emptyMap<String, String>(), profile.metadata)
        assertNull(profile.embedding)
    }

    @Test
    fun `profiles with same content are equal`() {
        val embedding = floatArrayOf(1.0f, 2.0f)
        val profile1 = Profile(id = 1, name = "A", createdAt = 100, updatedAt = 200, age = 25, gender = "Female", embedding = embedding)
        val profile2 = Profile(id = 1, name = "A", createdAt = 100, updatedAt = 200, age = 25, gender = "Female", embedding = embedding)
        assertEquals(profile1, profile2)
        assertEquals(profile1.hashCode(), profile2.hashCode())
    }

    @Test
    fun `profiles with different age are not equal`() {
        val profile1 = Profile(name = "A", createdAt = 100, updatedAt = 200, age = 25)
        val profile2 = Profile(name = "A", createdAt = 100, updatedAt = 200, age = 30)
        assertNotEquals(profile1, profile2)
    }

    @Test
    fun `profiles with different gender are not equal`() {
        val profile1 = Profile(name = "A", createdAt = 100, updatedAt = 200, gender = "Male")
        val profile2 = Profile(name = "A", createdAt = 100, updatedAt = 200, gender = "Female")
        assertNotEquals(profile1, profile2)
    }
}
