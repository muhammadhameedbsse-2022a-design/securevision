package com.securevision.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchResultTest {

    @Test
    fun `noMatch returns result with no profile and zero similarity`() {
        val result = MatchResult.noMatch()
        assertNull(result.profile)
        assertEquals(0f, result.similarity, 0.0001f)
        assertFalse(result.isMatch)
    }

    @Test
    fun `match result with profile`() {
        val profile = Profile(
            name = "John",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val result = MatchResult(
            profile = profile,
            similarity = 0.92f,
            isMatch = true
        )
        assertTrue(result.isMatch)
        assertEquals("John", result.profile?.name)
        assertEquals(0.92f, result.similarity, 0.0001f)
    }

    @Test
    fun `default threshold is 0_75`() {
        assertEquals(0.75f, MatchResult.DEFAULT_THRESHOLD, 0.0001f)
    }
}
