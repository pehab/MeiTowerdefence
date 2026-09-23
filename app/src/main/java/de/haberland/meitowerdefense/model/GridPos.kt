package de.haberland.meitowerdefense.model

/** A discrete grid cell (tower placement), as opposed to [Vec2] (continuous movement/paths). */
data class GridPos(val col: Int, val row: Int) {
    fun toVec2(): Vec2 = Vec2(col + 0.5f, row + 0.5f)
}
