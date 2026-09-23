package de.haberland.meitowerdefense.model

/**
 * One color per [EnemyType]/[TowerType], defined once so [de.haberland.meitowerdefense.render.GameRenderer]
 * (android.graphics, wants a packed ARGB Int) and the glossary screen (Compose, wants
 * androidx.compose.ui.graphics.Color) draw the exact same colors instead of two
 * hand-copied hex lists silently drifting apart.
 *
 * Values are packed ARGB (0xFFRRGGBB) as Long, matching the format both
 * `android.graphics.Color`-as-Int (via `.toInt()`, which truncates to the low 32 bits)
 * and `androidx.compose.ui.graphics.Color(Long)` expect directly.
 */
object TypeColors {
    const val ENEMY_BASIC: Long = 0xFFD2D2D2
    const val ENEMY_FAST: Long = 0xFFF0DC3C
    const val ENEMY_ARMORED: Long = 0xFF786450
    const val ENEMY_FLYING: Long = 0xFF96D2FF
    const val ENEMY_BOSS: Long = 0xFFBE283C

    const val TOWER_ARCHER: Long = 0xFF5AAA5A
    const val TOWER_CANNON: Long = 0xFF46464B
    const val TOWER_FIRE: Long = 0xFFDC6E28
    const val TOWER_ICE: Long = 0xFF50B4DC

    fun enemyColor(type: EnemyType): Long = when (type) {
        EnemyType.BASIC -> ENEMY_BASIC
        EnemyType.FAST -> ENEMY_FAST
        EnemyType.ARMORED -> ENEMY_ARMORED
        EnemyType.FLYING -> ENEMY_FLYING
        EnemyType.BOSS -> ENEMY_BOSS
    }

    fun towerColor(type: TowerType): Long = when (type) {
        TowerType.ARCHER -> TOWER_ARCHER
        TowerType.CANNON -> TOWER_CANNON
        TowerType.FIRE -> TOWER_FIRE
        TowerType.ICE -> TOWER_ICE
    }
}
