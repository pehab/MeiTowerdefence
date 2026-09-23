package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.model.WaveEntry

/**
 * The first playable slice: 3 hand-authored levels on a 14x9 grid, ramping up which
 * enemy types appear so each level teaches one new thing (see LevelCatalog.all's
 * comments). More levels are meant to be added here later the same way - nothing about
 * GameSimulator or the UI is specific to there being exactly three.
 *
 * Every WaveEntry is one visually distinct wave (its own pre-wave pause via
 * LevelDefinition.timeBetweenWaves) rather than mixed enemy types spawning at once
 * within a single wave - simpler to both author and read, and mixing types in sequence
 * across waves already gives waves 3-4 rounds apart a good ramp without it.
 */
object LevelCatalog {

    val forestPath = LevelDefinition(
        id = "forest_path",
        displayName = "Waldpfad",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 4f), Vec2(5f, 4f), Vec2(5f, 1f), Vec2(9f, 1f), Vec2(9f, 7f), Vec2(13f, 7f)
        ),
        // No flying wave in this level - kept just outside the grid so it doesn't eat
        // into buildable space the way a full-width path along row 1 would.
        airPath = listOf(Vec2(0f, -1f), Vec2(13f, -1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, count = 6, spawnIntervalSeconds = 1.2f),
            WaveEntry(EnemyType.BASIC, count = 8, spawnIntervalSeconds = 0.9f),
            WaveEntry(EnemyType.FAST, count = 6, spawnIntervalSeconds = 0.7f),
            WaveEntry(EnemyType.BASIC, count = 10, spawnIntervalSeconds = 0.6f),
            WaveEntry(EnemyType.FAST, count = 8, spawnIntervalSeconds = 0.5f),
            WaveEntry(EnemyType.BASIC, count = 14, spawnIntervalSeconds = 0.4f)
        ),
        startingGold = 220,
        startingLives = 18,
        timeBetweenWaves = 5f
    )

    val mountainPass = LevelDefinition(
        id = "mountain_pass",
        displayName = "Bergpass",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 1f), Vec2(3f, 1f), Vec2(3f, 7f), Vec2(7f, 7f), Vec2(7f, 2f), Vec2(11f, 2f), Vec2(11f, 6f), Vec2(13f, 6f)
        ),
        // No flying wave in this level either - same reasoning as forest_path.
        airPath = listOf(Vec2(0f, -1f), Vec2(13f, -1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, count = 8, spawnIntervalSeconds = 0.7f),
            WaveEntry(EnemyType.FAST, count = 8, spawnIntervalSeconds = 0.5f),
            WaveEntry(EnemyType.ARMORED, count = 4, spawnIntervalSeconds = 1.2f),
            WaveEntry(EnemyType.BASIC, count = 10, spawnIntervalSeconds = 0.5f),
            WaveEntry(EnemyType.FAST, count = 6, spawnIntervalSeconds = 0.4f),
            WaveEntry(EnemyType.ARMORED, count = 6, spawnIntervalSeconds = 1.0f),
            WaveEntry(EnemyType.BASIC, count = 12, spawnIntervalSeconds = 0.4f),
            WaveEntry(EnemyType.ARMORED, count = 8, spawnIntervalSeconds = 0.8f)
        ),
        startingGold = 180,
        startingLives = 18,
        timeBetweenWaves = 5f
    )

    val valley = LevelDefinition(
        id = "valley",
        displayName = "Talkessel",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 7f), Vec2(4f, 7f), Vec2(4f, 3f), Vec2(8f, 3f), Vec2(8f, 7f), Vec2(13f, 7f)
        ),
        // Flying enemies cross near the top of the map instead of following the ground
        // road's loop through the valley - a visibly shorter, more direct route.
        airPath = listOf(Vec2(0f, 1f), Vec2(6f, 1f), Vec2(8f, 1f), Vec2(13f, 1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, count = 8, spawnIntervalSeconds = 0.6f),
            WaveEntry(EnemyType.FAST, count = 8, spawnIntervalSeconds = 0.5f),
            WaveEntry(EnemyType.ARMORED, count = 5, spawnIntervalSeconds = 1.0f),
            WaveEntry(EnemyType.FLYING, count = 6, spawnIntervalSeconds = 0.7f),
            WaveEntry(EnemyType.BASIC, count = 10, spawnIntervalSeconds = 0.4f),
            WaveEntry(EnemyType.ARMORED, count = 4, spawnIntervalSeconds = 1.0f),
            WaveEntry(EnemyType.FLYING, count = 8, spawnIntervalSeconds = 0.5f),
            WaveEntry(EnemyType.ARMORED, count = 8, spawnIntervalSeconds = 0.8f),
            WaveEntry(EnemyType.BOSS, count = 1, spawnIntervalSeconds = 1.0f)
        ),
        startingGold = 260,
        startingLives = 18,
        timeBetweenWaves = 5f
    )

    /** Ordered for level-select; index also decides unlock order (level N+1 needs level N beaten). */
    val all: List<LevelDefinition> = listOf(forestPath, mountainPass, valley)

    fun byId(id: String): LevelDefinition? = all.find { it.id == id }
}
