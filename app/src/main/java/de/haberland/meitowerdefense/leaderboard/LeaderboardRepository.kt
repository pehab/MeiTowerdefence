package de.haberland.meitowerdefense.leaderboard

data class LeaderboardEntry(val name: String, val score: Int)

sealed interface SubmitResult {
    data object Posted : SubmitResult
    data object ExistingScoreIsHigher : SubmitResult
}

interface LeaderboardRepository {
    fun loadTop(limit: Long = 100, onSuccess: (List<LeaderboardEntry>) -> Unit, onError: (Exception) -> Unit)
    fun submitPersonalBest(name: String, score: Int, onSuccess: (SubmitResult) -> Unit, onError: (Exception) -> Unit)
    fun deleteOwnEntry(onSuccess: () -> Unit, onError: (Exception) -> Unit)
}

object LeaderboardInput {
    const val MAX_NAME_LENGTH = 24
    const val MAX_SCORE = 100_000

    fun normalizeName(value: String): String = value.trim().replace(Regex("\\s+"), " ").take(MAX_NAME_LENGTH)
    fun validName(value: String): Boolean = normalizeName(value).length in 2..MAX_NAME_LENGTH
    fun validScore(value: Int): Boolean = value in 1..MAX_SCORE
    fun isNewPublishableRecord(previousBest: Int, completedWaves: Int): Boolean =
        completedWaves > previousBest && validScore(completedWaves)
}
