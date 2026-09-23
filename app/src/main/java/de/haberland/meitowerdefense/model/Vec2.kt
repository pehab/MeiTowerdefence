package de.haberland.meitowerdefense.model

import kotlin.math.sqrt

/** Grid-space position or direction. One unit = one grid cell; rendering scales this to pixels. */
data class Vec2(val x: Float, val y: Float) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)

    fun length(): Float = sqrt(x * x + y * y)
    fun distanceTo(other: Vec2): Float = (this - other).length()

    fun normalized(): Vec2 {
        val len = length()
        return if (len < 0.0001f) Vec2(0f, 0f) else Vec2(x / len, y / len)
    }
}
