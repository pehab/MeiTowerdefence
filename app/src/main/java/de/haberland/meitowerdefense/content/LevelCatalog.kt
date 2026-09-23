package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.model.WaveEntry
import de.haberland.meitowerdefense.model.WaveGroup

/**
 * Hand-authored campaign. Levels 1-3 teach the basic enemy roster, levels 4-8 start
 * combining enemy types in overlapping waves and put more pressure on placement and
 * tower composition. Exact balance is intentionally data-only here so it can be tuned
 * without touching the simulation.
 */
object LevelCatalog {

    private fun mixed(vararg groups: WaveGroup) = WaveEntry(groups.toList())

    val forestPath = LevelDefinition(
        id = "forest_path",
        displayName = "Waldpfad",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 4f), Vec2(5f, 4f), Vec2(5f, 1f), Vec2(9f, 1f), Vec2(9f, 7f), Vec2(13f, 7f)
        ),
        airPath = listOf(Vec2(0f, -1f), Vec2(13f, -1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, 6, 1.2f),
            WaveEntry(EnemyType.BASIC, 8, 0.9f),
            WaveEntry(EnemyType.FAST, 6, 0.7f),
            WaveEntry(EnemyType.BASIC, 10, 0.6f),
            WaveEntry(EnemyType.FAST, 8, 0.5f),
            WaveEntry(EnemyType.BASIC, 14, 0.4f)
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
        airPath = listOf(Vec2(0f, -1f), Vec2(13f, -1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, 8, 0.7f),
            WaveEntry(EnemyType.FAST, 8, 0.5f),
            WaveEntry(EnemyType.ARMORED, 4, 1.2f),
            WaveEntry(EnemyType.BASIC, 10, 0.5f),
            WaveEntry(EnemyType.FAST, 6, 0.4f),
            WaveEntry(EnemyType.ARMORED, 6, 1.0f),
            WaveEntry(EnemyType.BASIC, 12, 0.4f),
            WaveEntry(EnemyType.ARMORED, 8, 0.8f)
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
        airPath = listOf(Vec2(0f, 1f), Vec2(6f, 1f), Vec2(8f, 1f), Vec2(13f, 1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, 8, 0.6f),
            WaveEntry(EnemyType.FAST, 8, 0.5f),
            WaveEntry(EnemyType.ARMORED, 5, 1.0f),
            WaveEntry(EnemyType.FLYING, 6, 0.7f),
            WaveEntry(EnemyType.BASIC, 10, 0.4f),
            WaveEntry(EnemyType.ARMORED, 4, 1.0f),
            WaveEntry(EnemyType.FLYING, 8, 0.5f),
            WaveEntry(EnemyType.ARMORED, 8, 0.8f),
            WaveEntry(EnemyType.BOSS, 1, 1.0f)
        ),
        startingGold = 260,
        startingLives = 18,
        timeBetweenWaves = 5f
    )

    val riverbank = LevelDefinition(
        id = "riverbank",
        displayName = "Flussufer",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 6f), Vec2(3f, 6f), Vec2(3f, 2f), Vec2(7f, 2f), Vec2(7f, 6f), Vec2(10f, 6f), Vec2(10f, 3f), Vec2(13f, 3f)
        ),
        airPath = listOf(Vec2(0f, 1f), Vec2(13f, 1f)),
        waves = listOf(
            WaveEntry(EnemyType.BASIC, 10, 0.55f),
            mixed(
                WaveGroup(EnemyType.BASIC, 8, 0.55f),
                WaveGroup(EnemyType.FLYING, 5, 0.75f, startDelaySeconds = 1.5f)
            ),
            WaveEntry(EnemyType.FLYING, 10, 0.5f, hpMultiplier = 1.1f),
            mixed(
                WaveGroup(EnemyType.ARMORED, 5, 1.0f),
                WaveGroup(EnemyType.FAST, 9, 0.45f, startDelaySeconds = 1.0f)
            ),
            mixed(
                WaveGroup(EnemyType.FLYING, 9, 0.5f, hpMultiplier = 1.15f),
                WaveGroup(EnemyType.BASIC, 12, 0.42f, hpMultiplier = 1.1f, startDelaySeconds = 2f)
            ),
            mixed(
                WaveGroup(EnemyType.ARMORED, 7, 0.85f, hpMultiplier = 1.15f),
                WaveGroup(EnemyType.FLYING, 10, 0.48f, hpMultiplier = 1.2f, startDelaySeconds = 1.5f)
            ),
            WaveEntry(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.1f)
        ),
        startingGold = 240,
        startingLives = 18,
        timeBetweenWaves = 5f
    )

    val serpentines = LevelDefinition(
        id = "serpentines",
        displayName = "Serpentinen",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 1f), Vec2(11f, 1f), Vec2(11f, 3f), Vec2(2f, 3f), Vec2(2f, 6f), Vec2(12f, 6f), Vec2(12f, 8f), Vec2(13f, 8f)
        ),
        airPath = listOf(Vec2(0f, 0f), Vec2(13f, 0f)),
        waves = listOf(
            mixed(
                WaveGroup(EnemyType.FAST, 10, 0.42f),
                WaveGroup(EnemyType.BASIC, 8, 0.55f, startDelaySeconds = 1.8f)
            ),
            WaveEntry(EnemyType.ARMORED, 7, 0.9f, hpMultiplier = 1.15f),
            mixed(
                WaveGroup(EnemyType.FAST, 14, 0.34f, hpMultiplier = 1.1f),
                WaveGroup(EnemyType.ARMORED, 4, 1.0f, startDelaySeconds = 2f)
            ),
            WaveEntry(EnemyType.FLYING, 9, 0.5f, hpMultiplier = 1.15f),
            mixed(
                WaveGroup(EnemyType.ARMORED, 8, 0.75f, hpMultiplier = 1.2f),
                WaveGroup(EnemyType.FAST, 12, 0.36f, hpMultiplier = 1.2f, startDelaySeconds = 1.2f)
            ),
            mixed(
                WaveGroup(EnemyType.BASIC, 16, 0.32f, hpMultiplier = 1.25f),
                WaveGroup(EnemyType.FLYING, 8, 0.45f, hpMultiplier = 1.25f, startDelaySeconds = 2f)
            ),
            WaveEntry(EnemyType.ARMORED, 10, 0.72f, hpMultiplier = 1.3f),
            WaveEntry(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.25f)
        ),
        startingGold = 210,
        startingLives = 18,
        timeBetweenWaves = 4.5f
    )

    val crossroads = LevelDefinition(
        id = "crossroads",
        displayName = "Kreuzung",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 4f), Vec2(4f, 4f), Vec2(4f, 1f), Vec2(9f, 1f), Vec2(9f, 7f), Vec2(5f, 7f), Vec2(5f, 4f), Vec2(13f, 4f)
        ),
        airPath = listOf(Vec2(0f, 7f), Vec2(6f, 4f), Vec2(13f, 1f)),
        waves = listOf(
            mixed(
                WaveGroup(EnemyType.BASIC, 12, 0.45f, hpMultiplier = 1.15f),
                WaveGroup(EnemyType.FAST, 8, 0.38f, startDelaySeconds = 1f)
            ),
            mixed(
                WaveGroup(EnemyType.FLYING, 8, 0.48f, hpMultiplier = 1.2f),
                WaveGroup(EnemyType.ARMORED, 5, 0.95f, hpMultiplier = 1.2f, startDelaySeconds = 1.5f)
            ),
            WaveEntry(EnemyType.FAST, 18, 0.3f, hpMultiplier = 1.2f),
            mixed(
                WaveGroup(EnemyType.ARMORED, 8, 0.75f, hpMultiplier = 1.3f),
                WaveGroup(EnemyType.FLYING, 10, 0.43f, hpMultiplier = 1.3f, startDelaySeconds = 1f)
            ),
            mixed(
                WaveGroup(EnemyType.BASIC, 18, 0.3f, hpMultiplier = 1.35f),
                WaveGroup(EnemyType.FAST, 12, 0.28f, hpMultiplier = 1.25f, startDelaySeconds = 1.2f)
            ),
            WaveEntry(EnemyType.ARMORED, 11, 0.68f, hpMultiplier = 1.35f),
            mixed(
                WaveGroup(EnemyType.FLYING, 12, 0.4f, hpMultiplier = 1.4f),
                WaveGroup(EnemyType.FAST, 14, 0.3f, hpMultiplier = 1.35f, startDelaySeconds = 1f)
            ),
            WaveEntry(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.45f)
        ),
        startingGold = 225,
        startingLives = 18,
        timeBetweenWaves = 4.5f
    )

    val fortress = LevelDefinition(
        id = "fortress",
        displayName = "Festung",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 2f), Vec2(3f, 2f), Vec2(3f, 6f), Vec2(6f, 6f), Vec2(6f, 2f), Vec2(10f, 2f), Vec2(10f, 6f), Vec2(13f, 6f)
        ),
        airPath = listOf(Vec2(0f, 7f), Vec2(13f, 2f)),
        waves = listOf(
            WaveEntry(EnemyType.ARMORED, 8, 0.8f, hpMultiplier = 1.3f),
            mixed(
                WaveGroup(EnemyType.ARMORED, 6, 0.8f, hpMultiplier = 1.35f),
                WaveGroup(EnemyType.FAST, 14, 0.3f, hpMultiplier = 1.25f, startDelaySeconds = 1.2f)
            ),
            mixed(
                WaveGroup(EnemyType.FLYING, 12, 0.4f, hpMultiplier = 1.35f),
                WaveGroup(EnemyType.BASIC, 16, 0.32f, hpMultiplier = 1.4f, startDelaySeconds = 1f)
            ),
            WaveEntry(EnemyType.ARMORED, 12, 0.65f, hpMultiplier = 1.45f),
            mixed(
                WaveGroup(EnemyType.FAST, 18, 0.27f, hpMultiplier = 1.4f),
                WaveGroup(EnemyType.FLYING, 10, 0.38f, hpMultiplier = 1.45f, startDelaySeconds = 1.5f)
            ),
            mixed(
                WaveGroup(EnemyType.ARMORED, 10, 0.62f, hpMultiplier = 1.5f),
                WaveGroup(EnemyType.FLYING, 12, 0.37f, hpMultiplier = 1.5f, startDelaySeconds = 1f)
            ),
            mixed(
                WaveGroup(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.45f),
                WaveGroup(EnemyType.FAST, 14, 0.3f, hpMultiplier = 1.45f, startDelaySeconds = 2f)
            ),
            WaveEntry(EnemyType.ARMORED, 14, 0.58f, hpMultiplier = 1.55f),
            mixed(
                WaveGroup(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.6f),
                WaveGroup(EnemyType.FLYING, 14, 0.35f, hpMultiplier = 1.55f, startDelaySeconds = 2f)
            )
        ),
        startingGold = 190,
        startingLives = 18,
        timeBetweenWaves = 4f
    )

    val lastWall = LevelDefinition(
        id = "last_wall",
        displayName = "Letzter Wall",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 4f), Vec2(2f, 4f), Vec2(2f, 1f), Vec2(6f, 1f), Vec2(6f, 7f), Vec2(10f, 7f), Vec2(10f, 3f), Vec2(13f, 3f)
        ),
        airPath = listOf(Vec2(0f, 8f), Vec2(6f, 4f), Vec2(13f, 0f)),
        waves = listOf(
            mixed(WaveGroup(EnemyType.BASIC, 16, 0.34f, hpMultiplier = 1.35f), WaveGroup(EnemyType.FAST, 10, 0.3f, hpMultiplier = 1.3f, startDelaySeconds = 1f)),
            mixed(WaveGroup(EnemyType.ARMORED, 8, 0.7f, hpMultiplier = 1.45f), WaveGroup(EnemyType.FLYING, 10, 0.42f, hpMultiplier = 1.4f, startDelaySeconds = 1.5f)),
            WaveEntry(EnemyType.FAST, 20, 0.26f, hpMultiplier = 1.45f),
            mixed(WaveGroup(EnemyType.ARMORED, 12, 0.62f, hpMultiplier = 1.55f), WaveGroup(EnemyType.BASIC, 18, 0.3f, hpMultiplier = 1.5f, startDelaySeconds = 1f)),
            mixed(WaveGroup(EnemyType.FLYING, 16, 0.35f, hpMultiplier = 1.5f), WaveGroup(EnemyType.FAST, 16, 0.28f, hpMultiplier = 1.5f, startDelaySeconds = 1f)),
            WaveEntry(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.6f),
            mixed(WaveGroup(EnemyType.ARMORED, 14, 0.58f, hpMultiplier = 1.65f), WaveGroup(EnemyType.FAST, 18, 0.25f, hpMultiplier = 1.55f, startDelaySeconds = 1.5f)),
            mixed(WaveGroup(EnemyType.FLYING, 18, 0.33f, hpMultiplier = 1.65f), WaveGroup(EnemyType.BASIC, 20, 0.28f, hpMultiplier = 1.65f, startDelaySeconds = 1f)),
            mixed(WaveGroup(EnemyType.BOSS, 1, 1f, hpMultiplier = 1.75f), WaveGroup(EnemyType.ARMORED, 10, 0.62f, hpMultiplier = 1.65f, startDelaySeconds = 1.5f)),
            mixed(WaveGroup(EnemyType.FAST, 24, 0.23f, hpMultiplier = 1.7f), WaveGroup(EnemyType.FLYING, 16, 0.31f, hpMultiplier = 1.7f, startDelaySeconds = 1f)),
            mixed(WaveGroup(EnemyType.ARMORED, 16, 0.55f, hpMultiplier = 1.8f), WaveGroup(EnemyType.FLYING, 18, 0.3f, hpMultiplier = 1.75f, startDelaySeconds = 1f)),
            mixed(WaveGroup(EnemyType.BOSS, 2, 3f, hpMultiplier = 1.9f), WaveGroup(EnemyType.FAST, 20, 0.24f, hpMultiplier = 1.8f, startDelaySeconds = 1.5f))
        ),
        startingGold = 230,
        startingLives = 20,
        timeBetweenWaves = 4f
    )

    /** Endless uses its own generated wave stream; the map is deliberately neutral. */
    val endless = LevelDefinition(
        id = "endless",
        displayName = "Unendlich",
        gridWidth = 14,
        gridHeight = 9,
        groundPath = listOf(
            Vec2(0f, 4f), Vec2(3f, 4f), Vec2(3f, 1f), Vec2(8f, 1f), Vec2(8f, 7f), Vec2(11f, 7f), Vec2(11f, 4f), Vec2(13f, 4f)
        ),
        airPath = listOf(Vec2(0f, 7f), Vec2(7f, 3f), Vec2(13f, 1f)),
        waves = emptyList(),
        startingGold = 240,
        startingLives = 20,
        timeBetweenWaves = 4f,
        endless = true
    )

    /** Ordered campaign list; index also decides unlock order. */
    val all: List<LevelDefinition> = listOf(
        forestPath, mountainPass, valley, riverbank, serpentines, crossroads, fortress, lastWall
    )

    fun byId(id: String): LevelDefinition? =
        if (id == endless.id) endless else all.find { it.id == id }
}
