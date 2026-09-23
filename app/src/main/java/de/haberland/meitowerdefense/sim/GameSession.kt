package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.MetaProgress

/**
 * The full state of one level playthrough at a point in time. Deliberately an immutable
 * data class rather than a bag of `var`s: [GameSimulator.step] takes one of these plus a
 * time delta and returns a new one, which is what makes the whole simulation replayable
 * and unit-testable without any Android/rendering code involved. GameView (or a
 * ViewModel) is the only thing that actually holds a mutable reference, re-assigning it
 * once per game-loop tick.
 */
data class GameSession(
    val level: LevelDefinition,
    val meta: MetaProgress,
    val gold: Int,
    val lives: Int,
    val towers: List<Tower> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val waveIndex: Int = 0,
    val enemiesSpawnedInWave: Int = 0,
    val timeSinceLastSpawn: Float = 0f,
    /**
     * True whenever the next wave (waveIndex) hasn't started yet. Wave 1 stays true
     * until the player explicitly taps Start - see GameSimulator.startNextWave(). Every
     * later wave also starts this true, but [timeUntilAutoStart] counts down and starts
     * it automatically once it hits zero, the same as before; startNextWave() lets the
     * player call it early instead, for a small gold bonus.
     */
    val waitingForWaveStart: Boolean = true,
    /** Only meaningful while waitingForWaveStart && waveIndex > 0 - see above. */
    val timeUntilAutoStart: Float = 0f,
    val elapsedSeconds: Float = 0f,
    val outcome: GameOutcome = GameOutcome.IN_PROGRESS,
    /** Monotonic counter used to hand out deterministic, unique ids to new towers/enemies/projectiles. */
    val nextEntityId: Int = 0
) {
    companion object {
        fun start(level: LevelDefinition, meta: MetaProgress): GameSession = GameSession(
            level = level,
            meta = meta,
            gold = level.startingGold + meta.startingGoldBonus,
            lives = level.startingLives + meta.startingLivesBonus
        )
    }
}
