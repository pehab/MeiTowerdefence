package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EndlessWavesTest {

    @Test
    fun healthCurveStartsGentlyAndAcceleratesInHighWaves() {
        assertEquals(1f, EndlessWaves.hpMultiplier(0), 0.0001f)
        assertEquals(2.1565f, EndlessWaves.hpMultiplier(9), 0.0001f)
        assertEquals(10.2365f, EndlessWaves.hpMultiplier(49), 0.0001f)
        assertEquals(27.0865f, EndlessWaves.hpMultiplier(99), 0.0001f)

        val previousLinearWave100 = 1f + 99 * 0.11f
        assertTrue(EndlessWaves.hpMultiplier(99) > previousLinearWave100 * 2.2f)
    }

    @Test
    fun bossWavesUseTheSameProgressionWithExtraBossPressure() {
        val waveTen = EndlessWaves.wave(9)
        val boss = waveTen.groups.first { it.enemyType == EnemyType.BOSS }
        val fast = waveTen.groups.first { it.enemyType == EnemyType.FAST }
        val flying = waveTen.groups.first { it.enemyType == EnemyType.FLYING }

        assertEquals(EndlessWaves.hpMultiplier(9) * 1.1f, boss.hpMultiplier, 0.0001f)
        assertEquals(EndlessWaves.hpMultiplier(9), fast.hpMultiplier, 0.0001f)
        assertEquals(EndlessWaves.hpMultiplier(9), flying.hpMultiplier, 0.0001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun healthCurveRejectsNegativeIndexes() {
        EndlessWaves.hpMultiplier(-1)
    }
}
