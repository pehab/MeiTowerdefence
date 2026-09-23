package de.haberland.meitowerdefense.model

/** Turns a level outcome into a 0-3 star rating (0 = lost). */
object LevelRating {
    fun starsFor(startingLives: Int, remainingLives: Int, won: Boolean): Int {
        if (!won) return 0
        return when {
            remainingLives >= startingLives -> 3
            remainingLives >= startingLives / 2 -> 2
            else -> 1
        }
    }
}
