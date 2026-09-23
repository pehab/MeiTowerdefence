package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.Vec2

/**
 * One enemy instance during a level playthrough. [pathIndex] is the index of the next
 * waypoint it's heading towards; reaching pathIndex >= path.size means it reached the
 * base. [distanceTraveled] accumulates actual movement each tick (so it already reflects
 * slow/freeze effects) and is used purely as a "how far along" ranking for tower
 * targeting - it doesn't drive movement itself.
 */
data class Enemy(
    val id: String,
    val type: EnemyType,
    val maxHp: Float,
    val hp: Float,
    val position: Vec2,
    val pathIndex: Int,
    val distanceTraveled: Float = 0f,
    val slowFactor: Float = 0f,
    val slowRemaining: Float = 0f,
    val frozenRemaining: Float = 0f,
    val burnDps: Float = 0f,
    val burnRemaining: Float = 0f
) {
    val isDead: Boolean get() = hp <= 0f

    /** 0 = fully stopped (frozen), 1 = full speed. Slow and freeze don't stack additively - freeze simply wins. */
    val speedFactor: Float get() = if (frozenRemaining > 0f) 0f else (1f - slowFactor).coerceIn(0f, 1f)

    fun reachedEnd(pathLength: Int): Boolean = pathIndex >= pathLength
}
