package de.haberland.meitowerdefense.render

import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.Vec2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCameraTest {

    @Test
    fun squareGridOnSquareScreenFillsExactly() {
        val camera = GameCamera(gridWidth = 10, gridHeight = 10, screenWidthPx = 1000f, screenHeightPx = 1000f)
        assertEquals(100f, camera.cellSizePx, 0.01f)
        val (x, y) = camera.gridToScreen(Vec2(0f, 0f))
        assertEquals(0f, x, 0.01f)
        assertEquals(0f, y, 0.01f)
    }

    @Test
    fun widerScreenThanGridLetterboxesHorizontally() {
        // Grid is 10x10 (square), screen is wider than tall - cell size is limited by
        // height, leaving horizontal bars on both sides.
        val camera = GameCamera(gridWidth = 10, gridHeight = 10, screenWidthPx = 2000f, screenHeightPx = 1000f)
        assertEquals(100f, camera.cellSizePx, 0.01f)
        val (x, _) = camera.gridToScreen(Vec2(0f, 0f))
        assertTrue("expected a left offset to center the grid", x > 0f)
    }

    @Test
    fun screenToGridIsTheInverseOfGridToScreenForCellCenters() {
        val camera = GameCamera(gridWidth = 12, gridHeight = 8, screenWidthPx = 1200f, screenHeightPx = 800f)
        val cell = GridPos(5, 3)
        val (x, y) = camera.gridToScreen(cell.toVec2())
        assertEquals(cell, camera.screenToGrid(x, y))
    }

    @Test
    fun screenToGridHandlesTapsOutsideTheGridWithoutCrashing() {
        val camera = GameCamera(gridWidth = 10, gridHeight = 10, screenWidthPx = 1000f, screenHeightPx = 1000f)
        val topLeft = camera.screenToGrid(-50f, -50f)
        assertTrue(topLeft.col < 0)
        assertTrue(topLeft.row < 0)
    }
}
