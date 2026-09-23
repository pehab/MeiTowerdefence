package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.Vec2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PathFollowerTest {

    private fun assertVecEquals(expected: Vec2, actual: Vec2, tolerance: Float = 0.001f) {
        assertTrue("expected $expected but was $actual", abs(expected.x - actual.x) < tolerance && abs(expected.y - actual.y) < tolerance)
    }

    @Test
    fun stepsPartWayTowardTheNextWaypoint() {
        val path = listOf(Vec2(0f, 0f), Vec2(10f, 0f))
        val result = PathFollower.step(Vec2(0f, 0f), pathIndex = 1, path = path, distance = 4f)
        assertVecEquals(Vec2(4f, 0f), result.position)
        assertEquals(1, result.pathIndex)
        assertEquals(4f, result.distanceMoved, 0.001f)
    }

    @Test
    fun advancesToTheNextWaypointIndexOnExactArrival() {
        val path = listOf(Vec2(0f, 0f), Vec2(5f, 0f), Vec2(5f, 5f))
        val result = PathFollower.step(Vec2(0f, 0f), pathIndex = 1, path = path, distance = 5f)
        assertVecEquals(Vec2(5f, 0f), result.position)
        assertEquals(2, result.pathIndex)
    }

    @Test
    fun crossesMultipleWaypointsInOneLargeStep() {
        // Three short 1-unit segments; a single 2.5-unit step should cross two full
        // segments and land halfway through the third - exactly the "low frame rate or
        // fast-forward" case this exists for.
        val path = listOf(Vec2(0f, 0f), Vec2(1f, 0f), Vec2(2f, 0f), Vec2(3f, 0f))
        val result = PathFollower.step(Vec2(0f, 0f), pathIndex = 1, path = path, distance = 2.5f)
        assertVecEquals(Vec2(2.5f, 0f), result.position)
        assertEquals(3, result.pathIndex)
        assertEquals(2.5f, result.distanceMoved, 0.001f)
    }

    @Test
    fun reportsPathIndexAtOrBeyondSizeWhenTheEndIsReachedOrPassed() {
        val path = listOf(Vec2(0f, 0f), Vec2(1f, 0f))
        val result = PathFollower.step(Vec2(0f, 0f), pathIndex = 1, path = path, distance = 100f)
        assertTrue(result.pathIndex >= path.size)
        // distanceMoved only counts what was actually walked (1 unit to the last
        // waypoint), not the full requested 100.
        assertEquals(1f, result.distanceMoved, 0.001f)
    }

    @Test
    fun zeroDistanceLeavesPositionAndIndexUnchanged() {
        val path = listOf(Vec2(0f, 0f), Vec2(5f, 0f))
        val start = Vec2(2f, 0f)
        val result = PathFollower.step(start, pathIndex = 1, path = path, distance = 0f)
        assertVecEquals(start, result.position)
        assertEquals(1, result.pathIndex)
        assertEquals(0f, result.distanceMoved, 0.001f)
    }
}
