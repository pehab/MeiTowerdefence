package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.*
import org.junit.Assert.*
import org.junit.Test

class GoldIncomeTest {
    private fun session(level: Int) = GameSession.start(LevelCatalog.endless,
        MetaProgress(upgradeLevels = mapOf(MetaUpgradeType.GOLD_INCOME to level)))
    private fun kill(session: GameSession, types: List<EnemyType>): GameSession = GameSimulator.step(
        session.copy(enemies = types.mapIndexed { index, type ->
            Enemy("dead-$index", type, 1f, 0f, session.level.groundPath.first(), pathIndex = 1)
        }), 0.01f)

    @Test
    fun firstGoldaderLevelAccumulatesTheBonusFromSmallRewards() {
        val start = session(1)
        var result = start
        repeat(5) { result = kill(result, listOf(EnemyType.BASIC)) }
        assertEquals(start.gold + 27, result.gold) // 25 base gold + exactly 8%.
        assertEquals(27, result.stats.goldEarned)
        assertEquals(0, result.goldBonusRemainder)
    }

    @Test
    fun groupingAndEnemyOrderDoNotChangeThePayout() {
        val types = listOf(EnemyType.BASIC, EnemyType.FAST, EnemyType.ARMORED, EnemyType.FLYING, EnemyType.BOSS)
        val start = session(3)
        val together = kill(start, types)
        val separate = types.reversed().fold(start) { state, type -> kill(state, listOf(type)) }
        assertEquals(together.gold, separate.gold)
        assertEquals(together.stats.goldEarned, separate.stats.goldEarned)
        assertEquals(together.goldBonusRemainder, separate.goldBonusRemainder)
        assertEquals(start.gold + 184, together.gold) // 149 base + floor(35.76 bonus).
        assertEquals(76, together.goldBonusRemainder)
    }

    @Test
    fun zeroAndMaximumUpgradeLevelsPreserveTheirExactRatesAndFreshRunsStartWithoutRemainders() {
        val plain = session(0)
        assertEquals(plain.gold + 35, kill(plain, List(7) { EnemyType.BASIC }).gold)
        val maximum = session(5)
        assertEquals(maximum.gold + 49, kill(maximum, List(7) { EnemyType.BASIC }).gold)
        assertEquals(40, kill(session(1), listOf(EnemyType.BASIC)).goldBonusRemainder)
        assertEquals(0, session(1).goldBonusRemainder)
    }
}
