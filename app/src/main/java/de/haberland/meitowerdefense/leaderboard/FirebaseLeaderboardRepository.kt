package de.haberland.meitowerdefense.leaderboard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/** One document per anonymous Firebase user, so publishing a later record updates one row. */
class FirebaseLeaderboardRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LeaderboardRepository {
    private val scores get() = firestore.collection(COLLECTION)

    override fun loadTop(limit: Long, onSuccess: (List<LeaderboardEntry>) -> Unit, onError: (Exception) -> Unit) {
        scores.orderBy(FIELD_SCORE, Query.Direction.DESCENDING).limit(limit)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.documents.mapNotNull { document ->
                    val name = document.getString(FIELD_NAME)?.let(LeaderboardInput::normalizeName)
                    val score = document.getLong(FIELD_SCORE)?.toInt()
                    if (name != null && LeaderboardInput.validName(name) && score != null && LeaderboardInput.validScore(score)) {
                        LeaderboardEntry(name, score)
                    } else null
                })
            }
            .addOnFailureListener { onError(it) }
    }

    override fun submitPersonalBest(
        name: String,
        score: Int,
        onSuccess: (SubmitResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalized = LeaderboardInput.normalizeName(name)
        require(LeaderboardInput.validName(normalized)) { "name must contain 2-${LeaderboardInput.MAX_NAME_LENGTH} characters" }
        require(LeaderboardInput.validScore(score)) { "score must be 1-${LeaderboardInput.MAX_SCORE}" }
        withUser(onError) { uid ->
            val document = scores.document(uid)
            firestore.runTransaction { transaction ->
                val existing = transaction.get(document).getLong(FIELD_SCORE)?.toInt() ?: 0
                if (score <= existing) return@runTransaction false
                transaction.set(document, mapOf(
                    FIELD_NAME to normalized,
                    FIELD_SCORE to score,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                ))
                true
            }.addOnSuccessListener { written ->
                onSuccess(if (written) SubmitResult.Posted else SubmitResult.ExistingScoreIsHigher)
            }.addOnFailureListener { onError(it) }
        }
    }

    override fun deleteOwnEntry(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onSuccess()
            return
        }
        scores.document(uid).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    private fun withUser(onError: (Exception) -> Unit, block: (String) -> Unit) {
        auth.currentUser?.uid?.let(block) ?: auth.signInAnonymously()
            .addOnSuccessListener { result -> result.user?.uid?.let(block) ?: onError(IllegalStateException("Anonymous sign-in returned no user")) }
            .addOnFailureListener { onError(it) }
    }

    companion object {
        const val COLLECTION = "endless_highscores"
        const val FIELD_NAME = "name"
        const val FIELD_SCORE = "score"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
