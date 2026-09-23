package de.haberland.meitowerdefense.render

import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.Vec2
import kotlin.math.floor

/**
 * Maps between grid-space (float, one unit = one cell, used by the simulation) and
 * screen-space pixels (used for drawing and touch input). Recomputed whenever the
 * surface size changes (see GameSurfaceView.surfaceChanged) so the whole level always
 * fits the screen with square cells, centered, regardless of device aspect ratio.
 *
 * Deliberately free of android.graphics imports - it only does arithmetic on plain
 * Float/Int - so GameCameraTest can run on the plain JVM.
 */
class GameCamera(
    val gridWidth: Int,
    val gridHeight: Int,
    screenWidthPx: Float,
    screenHeightPx: Float
) {
    val cellSizePx: Float
    private val offsetXPx: Float
    private val offsetYPx: Float

    init {
        val cellFromWidth = screenWidthPx / gridWidth
        val cellFromHeight = screenHeightPx / gridHeight
        cellSizePx = minOf(cellFromWidth, cellFromHeight)
        offsetXPx = (screenWidthPx - gridWidth * cellSizePx) / 2f
        offsetYPx = (screenHeightPx - gridHeight * cellSizePx) / 2f
    }

    fun gridToScreen(pos: Vec2): Pair<Float, Float> =
        (offsetXPx + pos.x * cellSizePx) to (offsetYPx + pos.y * cellSizePx)

    fun screenToGrid(xPx: Float, yPx: Float): GridPos {
        val col = floor((xPx - offsetXPx) / cellSizePx).toInt()
        val row = floor((yPx - offsetYPx) / cellSizePx).toInt()
        return GridPos(col, row)
    }
}
