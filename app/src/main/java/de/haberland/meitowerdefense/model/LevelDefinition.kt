package de.haberland.meitowerdefense.model

/**
 * A fixed, hand-authored level. Tower placement is not a fixed slot list - any grid cell
 * far enough from both paths and not already occupied is valid (see
 * sim/GameSession.isBuildable) - so a level only needs to define its paths and waves.
 *
 * Endless mode deliberately has no finite [waves] list. Its waves are generated on
 * demand by content/EndlessWaves so the mode can continue indefinitely.
 */
data class LevelDefinition(
    val id: String,
    val displayName: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val groundPath: List<Vec2>,
    val airPath: List<Vec2>,
    val waves: List<WaveEntry>,
    val startingGold: Int,
    val startingLives: Int,
    val timeBetweenWaves: Float = 4f,
    val endless: Boolean = false
) {
    init {
        require(gridWidth > 0 && gridHeight > 0) { "grid dimensions must be positive" }
        require(groundPath.size >= 2) { "groundPath needs at least 2 waypoints" }
        require(airPath.size >= 2) { "airPath needs at least 2 waypoints" }
        require(endless || waves.isNotEmpty()) { "campaign level needs at least one wave" }
        require(startingGold >= 0 && startingLives > 0) { "startingGold must be >= 0 and startingLives > 0" }
        require(timeBetweenWaves >= 0f) { "timeBetweenWaves must be >= 0" }
    }
}
