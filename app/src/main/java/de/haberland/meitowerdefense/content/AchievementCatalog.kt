package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType

/** Stable IDs and explicit one-time rewards; changing display text never resets progress. */
data class AchievementTrack(
    val id: String,
    val title: String,
    val description: String,
    val metric: AchievementMetric,
    val thresholds: List<Int>,
    val rewards: List<Int>,
    val enemyType: EnemyType? = null,
    val towerType: TowerType? = null
) {
    init {
        require(thresholds.isNotEmpty() && thresholds.size == rewards.size)
        require(thresholds.all { it > 0 } && thresholds.zipWithNext().all { (a, b) -> a < b })
        require(rewards.all { it > 0 })
    }

    fun milestoneId(index: Int): String = "$id:${thresholds[index]}"
}

enum class AchievementMetric { TOTAL_KILLS, ENEMY_KILLS, TOWER_KILLS, CAMPAIGN_CLEARS, CAMPAIGN_PERFECT }

object AchievementCatalog {
    private val killRewards = listOf(1, 2, 4, 8, 15)
    val tracks: List<AchievementTrack> = listOf(
        AchievementTrack("kills", "Verteidiger", "Besiege Gegner über alle Partien hinweg.",
            AchievementMetric.TOTAL_KILLS, listOf(100, 500, 2_000, 10_000, 50_000), killRewards),
        AchievementTrack("armored", "Panzerknacker", "Besiege gepanzerte Gegner.",
            AchievementMetric.ENEMY_KILLS, listOf(50, 200, 1_000, 5_000, 20_000), killRewards, enemyType = EnemyType.ARMORED),
        AchievementTrack("flying", "Lufthoheit", "Schieße fliegende Gegner ab.",
            AchievementMetric.ENEMY_KILLS, listOf(50, 200, 1_000, 5_000, 20_000), killRewards, enemyType = EnemyType.FLYING),
        AchievementTrack("boss", "Bossbezwinger", "Besiege Bosse in Kampagne und Endlosmodus.",
            AchievementMetric.ENEMY_KILLS, listOf(5, 20, 50, 150, 500), killRewards, enemyType = EnemyType.BOSS),
        AchievementTrack("campaign", "Feldherr", "Schließe unterschiedliche Kampagnenlevel ab.",
            AchievementMetric.CAMPAIGN_CLEARS, listOf(1, 4, 8), listOf(1, 2, 5)),
        AchievementTrack("perfect", "Meisterstratege", "Erreiche drei Sterne in unterschiedlichen Kampagnenleveln.",
            AchievementMetric.CAMPAIGN_PERFECT, listOf(1, 4, 8), listOf(1, 3, 6))
    ) + TowerType.entries.map { type ->
        AchievementTrack("tower_${type.name.lowercase(java.util.Locale.ROOT)}", "${type.displayName}-Meister",
            "Besiege Gegner mit ${type.displayName}-Türmen. Beide Spezialisierungen zählen.",
            AchievementMetric.TOWER_KILLS, listOf(100, 500, 2_000, 10_000), listOf(1, 2, 4, 8), towerType = type)
    }

    val milestoneCount: Int = tracks.sumOf { it.thresholds.size }
}
