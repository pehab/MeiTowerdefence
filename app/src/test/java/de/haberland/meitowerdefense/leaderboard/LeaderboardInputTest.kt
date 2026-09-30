package de.haberland.meitowerdefense.leaderboard

import org.junit.Assert.*
import org.junit.Test

class LeaderboardInputTest {
    @Test
    fun namesAreTrimmedCollapsedAndBoundedBeforePublishing() {
        assertEquals("Peter Haberland", LeaderboardInput.normalizeName("  Peter   Haberland  "))
        assertEquals(24, LeaderboardInput.normalizeName("a".repeat(30)).length)
        assertTrue(LeaderboardInput.validName("AB"))
        assertFalse(LeaderboardInput.validName(" A "))
        assertFalse(LeaderboardInput.validName("   "))
    }

    @Test
    fun scoresUseFullyCompletedPositiveWavesWithinTheRuleRange() {
        assertFalse(LeaderboardInput.validScore(0))
        assertTrue(LeaderboardInput.validScore(1))
        assertTrue(LeaderboardInput.validScore(100_000))
        assertFalse(LeaderboardInput.validScore(100_001))
        assertFalse(LeaderboardInput.isNewPublishableRecord(previousBest = 10, completedWaves = 10))
        assertTrue(LeaderboardInput.isNewPublishableRecord(previousBest = 10, completedWaves = 11))
        assertFalse(LeaderboardInput.isNewPublishableRecord(previousBest = 10, completedWaves = 100_001))
    }
}
