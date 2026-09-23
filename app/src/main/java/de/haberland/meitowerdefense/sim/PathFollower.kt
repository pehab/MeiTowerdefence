package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.Vec2

/** Result of moving [PathFollower.step]: the new position, waypoint index, and how far this step actually travelled. */
data class PathStep(val position: Vec2, val pathIndex: Int, val distanceMoved: Float)

/**
 * Moves a point along a polyline by a given distance, advancing across as many waypoints
 * as that distance covers in one go. This matters at low frame rates or fast-forward
 * speeds, where a single tick can easily cover more than one short path segment - a
 * naive "move towards the next waypoint only" step would make enemies crawl along the
 * path far slower than their stated speed whenever that happens.
 *
 * pathIndex >= path.size signals the end of the path was reached (or passed) during this
 * step; any leftover distance beyond the last waypoint is simply not spent, distanceMoved
 * reflects only what was actually walked.
 */
object PathFollower {
    fun step(position: Vec2, pathIndex: Int, path: List<Vec2>, distance: Float): PathStep {
        var pos = position
        var idx = pathIndex
        var remaining = distance
        var moved = 0f

        while (remaining > 0f && idx < path.size) {
            val target = path[idx]
            val toTarget = target - pos
            val distToTarget = toTarget.length()

            if (distToTarget <= remaining) {
                pos = target
                remaining -= distToTarget
                moved += distToTarget
                idx++
            } else {
                pos += toTarget.normalized() * remaining
                moved += remaining
                remaining = 0f
            }
        }

        return PathStep(pos, idx, moved)
    }
}
