package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerBalance
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.model.WaveEntry
import kotlin.random.Random

/**
 * Everything that changes a [GameSession]. Two kinds of entry point:
 * - [step]: the per-frame tick (spawning, movement, combat, win/lose) - called once per
 *   game-loop frame with that frame's delta time.
 * - [buildTower] / [upgradeTower] / [sellTower]: user-initiated actions, called once per
 *   tap, independent of the frame tick.
 *
 * Both kinds are pure functions: GameSession in, GameSession out, no mutation, no
 * Android/rendering code involved. That's what makes GameSimulatorTest possible without
 * a device or emulator.
 */
object GameSimulator {
    private const val MIN_BUILD_DISTANCE_FROM_PATH = 0.6f
    private const val PROJECTILE_SPEED = 9f
    private const val SELL_REFUND_FRACTION = 0.6f
    /** Reward for tapping "Nächste Welle" before the auto-start timer would have fired on its own. */
    const val EARLY_WAVE_BONUS_GOLD = 15

    fun step(session: GameSession, dt: Float, random: Random = Random.Default): GameSession {
        if (session.outcome != GameOutcome.IN_PROGRESS || dt <= 0f) return session

        var s = session
        s = advanceWaveSpawning(s, dt)
        s = moveEnemies(s, dt)
        s = resolveEnemiesReachingEnd(s)
        s = fireTowers(s, dt, random)
        s = moveProjectiles(s, dt, random)
        s = removeDeadEnemies(s)
        s = checkOutcome(s)
        return s.copy(elapsedSeconds = s.elapsedSeconds + dt)
    }

    // --- wave spawning ---

    private fun advanceWaveSpawning(session: GameSession, dt: Float): GameSession {
        if (session.waveIndex >= session.level.waves.size) return session

        if (session.waitingForWaveStart) {
            // Wave 1 has nothing to count down - it always waits for an explicit tap.
            if (session.waveIndex == 0) return session

            val remaining = session.timeUntilAutoStart - dt
            if (remaining > 0f) return session.copy(timeUntilAutoStart = remaining)

            // Auto-start: carry the overflow past zero into this same tick's spawn
            // budget, so the first enemy doesn't lag an extra frame behind the timer
            // hitting zero - the same reasoning startNextWave() relies on for a manual
            // start (see spawnForCurrentWave's own comment).
            val started = beginSpawning(session.copy(timeUntilAutoStart = 0f))
            return spawnForCurrentWave(started, spawnBudget = -remaining)
        }

        return spawnForCurrentWave(session, spawnBudget = dt)
    }

    private fun spawnForCurrentWave(session: GameSession, spawnBudget: Float): GameSession {
        val wave = session.level.waves[session.waveIndex]
        var timeBudget = session.timeSinceLastSpawn + spawnBudget
        var spawnedCount = session.enemiesSpawnedInWave
        var nextId = session.nextEntityId
        val newEnemies = mutableListOf<Enemy>()

        // The first enemy of a wave spawns immediately when the wave starts (threshold
        // 0), not after waiting a full spawnIntervalSeconds like every subsequent one -
        // otherwise a wave "starting" would be invisible to the player for a beat.
        while (spawnedCount < wave.count) {
            val threshold = if (spawnedCount == 0) 0f else wave.spawnIntervalSeconds
            if (timeBudget < threshold) break
            newEnemies += spawnEnemy(session.level, wave, "e-$nextId")
            nextId++
            spawnedCount++
            timeBudget -= threshold
        }

        var next = session.copy(
            enemies = session.enemies + newEnemies,
            enemiesSpawnedInWave = spawnedCount,
            timeSinceLastSpawn = timeBudget,
            nextEntityId = nextId
        )

        if (spawnedCount >= wave.count) {
            // This wave is fully spawned (its enemies may still be alive and walking) -
            // re-arm the gate, with a fresh auto-start countdown, for the next wave.
            next = next.copy(
                waveIndex = next.waveIndex + 1,
                waitingForWaveStart = true,
                timeUntilAutoStart = next.level.timeBetweenWaves
            )
        }
        return next
    }

    private fun beginSpawning(session: GameSession): GameSession =
        session.copy(waitingForWaveStart = false, timeSinceLastSpawn = 0f, enemiesSpawnedInWave = 0)

    /**
     * Player-triggered: starts the next wave right now instead of waiting for it to
     * auto-start (or, for wave 1, instead of waiting forever). If a timer was still
     * running - i.e. this wave would otherwise have auto-started later on its own -
     * awards [EARLY_WAVE_BONUS_GOLD] for calling it early, the common "start next wave
     * for a bonus" TD mechanic. No bonus for wave 1 (there's no timer to skip) or if
     * called while a wave is already in progress or the level is done.
     */
    fun startNextWave(session: GameSession): GameSession {
        if (!session.waitingForWaveStart) return session
        if (session.waveIndex >= session.level.waves.size) return session

        val calledEarly = session.waveIndex > 0 && session.timeUntilAutoStart > 0f
        val withBonus = if (calledEarly) session.copy(gold = session.gold + EARLY_WAVE_BONUS_GOLD) else session
        return beginSpawning(withBonus)
    }

    private fun spawnEnemy(level: LevelDefinition, wave: WaveEntry, id: String): Enemy {
        val path = if (wave.enemyType.flying) level.airPath else level.groundPath
        val hp = wave.enemyType.baseHp * wave.hpMultiplier
        return Enemy(
            id = id,
            type = wave.enemyType,
            maxHp = hp,
            hp = hp,
            position = path.first(),
            pathIndex = 1
        )
    }

    // --- enemy movement & status-effect decay ---

    private fun moveEnemies(session: GameSession, dt: Float): GameSession {
        val updated = session.enemies.map { enemy ->
            if (enemy.isDead) return@map enemy

            val slowRemaining = (enemy.slowRemaining - dt).coerceAtLeast(0f)
            val frozenRemaining = (enemy.frozenRemaining - dt).coerceAtLeast(0f)
            val burnRemaining = (enemy.burnRemaining - dt).coerceAtLeast(0f)
            val burnDamage = if (enemy.burnRemaining > 0f) enemy.burnDps * dt else 0f

            var e = enemy.copy(
                hp = enemy.hp - burnDamage,
                slowFactor = if (slowRemaining > 0f) enemy.slowFactor else 0f,
                slowRemaining = slowRemaining,
                frozenRemaining = frozenRemaining,
                burnDps = if (burnRemaining > 0f) enemy.burnDps else 0f,
                burnRemaining = burnRemaining
            )
            if (e.isDead) return@map e

            val path = if (enemy.type.flying) session.level.airPath else session.level.groundPath
            val moveDistance = enemy.type.baseSpeed * e.speedFactor * dt
            val stepResult = PathFollower.step(e.position, e.pathIndex, path, moveDistance)
            e.copy(
                position = stepResult.position,
                pathIndex = stepResult.pathIndex,
                distanceTraveled = e.distanceTraveled + stepResult.distanceMoved
            )
        }
        return session.copy(enemies = updated)
    }

    private fun resolveEnemiesReachingEnd(session: GameSession): GameSession {
        val reached = session.enemies.filter {
            val pathLength = if (it.type.flying) session.level.airPath.size else session.level.groundPath.size
            it.reachedEnd(pathLength)
        }
        if (reached.isEmpty()) return session
        val reachedIds = reached.map { it.id }.toSet()
        return session.copy(
            enemies = session.enemies.filterNot { it.id in reachedIds },
            lives = (session.lives - reached.sumOf { it.type.livesCost }).coerceAtLeast(0)
        )
    }

    // --- towers firing ---

    private fun fireTowers(session: GameSession, dt: Float, random: Random): GameSession {
        val newProjectiles = mutableListOf<Projectile>()
        var nextId = session.nextEntityId

        val updatedTowers = session.towers.map { tower ->
            val cooldown = (tower.cooldownRemaining - dt).coerceAtLeast(0f)
            if (cooldown > 0f) return@map tower.copy(cooldownRemaining = cooldown)

            val target = selectTarget(tower, session.enemies)
            if (target == null) return@map tower.copy(cooldownRemaining = 0f)

            newProjectiles += buildProjectile(tower, target.id, session, "p-$nextId")
            nextId++

            if (tower.extraTargetChance > 0f && random.nextFloat() < tower.extraTargetChance) {
                val second = selectTarget(tower, session.enemies, exclude = target.id)
                if (second != null) {
                    newProjectiles += buildProjectile(tower, second.id, session, "p-$nextId")
                    nextId++
                }
            }

            tower.copy(cooldownRemaining = 1f / tower.fireRate)
        }

        return session.copy(
            towers = updatedTowers,
            projectiles = session.projectiles + newProjectiles,
            nextEntityId = nextId
        )
    }

    private fun buildProjectile(tower: Tower, targetId: String, session: GameSession, id: String) = Projectile(
        id = id,
        position = tower.position,
        targetEnemyId = targetId,
        speed = PROJECTILE_SPEED,
        damage = tower.damage(session.meta),
        armorPierce = tower.armorPierce,
        splashRadius = tower.splashRadius(session.meta),
        slowFactor = tower.slowFactor,
        slowDuration = tower.slowDuration(session.meta),
        freezeChance = tower.freezeChance,
        burnDps = tower.burnDps,
        burnDuration = tower.burnDuration,
        sourceTowerType = tower.type
    )

    /** Targets the furthest-along, in-range enemy this tower is allowed to hit (ground-only towers skip flyers). */
    private fun selectTarget(tower: Tower, enemies: List<Enemy>, exclude: String? = null): Enemy? =
        enemies
            .asSequence()
            .filter { !it.isDead && it.id != exclude }
            .filter { tower.type.canHitFlying || !it.type.flying }
            .filter { tower.position.distanceTo(it.position) <= tower.range }
            .maxByOrNull { it.distanceTraveled }

    // --- projectile flight & impact ---

    private fun moveProjectiles(session: GameSession, dt: Float, random: Random): GameSession {
        val enemiesById = session.enemies.associateBy { it.id }
        var enemies = session.enemies
        val remaining = mutableListOf<Projectile>()

        for (proj in session.projectiles) {
            val target = enemiesById[proj.targetEnemyId]
            if (target == null || target.isDead) continue // shot fizzles, its target is already gone

            val toTarget = target.position - proj.position
            val distance = toTarget.length()
            val travel = proj.speed * dt

            if (travel >= distance) {
                val impactPos = target.position
                enemies = enemies.map { e ->
                    if (e.isDead) return@map e
                    val isDirectHit = e.id == target.id
                    val isSplashHit = !isDirectHit && proj.splashRadius > 0f && e.position.distanceTo(impactPos) <= proj.splashRadius
                    if (isDirectHit || isSplashHit) applyHit(e, proj, random) else e
                }
            } else {
                remaining += proj.copy(position = proj.position + toTarget.normalized() * travel)
            }
        }

        return session.copy(enemies = enemies, projectiles = remaining)
    }

    private fun applyHit(enemy: Enemy, proj: Projectile, random: Random): Enemy {
        val effectiveArmor = (enemy.type.armor - proj.armorPierce).coerceAtLeast(0)
        val damage = (proj.damage - effectiveArmor).coerceAtLeast(1f)
        var e = enemy.copy(hp = enemy.hp - damage)

        if (proj.slowFactor > 0f && proj.slowDuration > 0f) {
            if (proj.slowFactor >= e.slowFactor || proj.slowDuration >= e.slowRemaining) {
                e = e.copy(
                    slowFactor = maxOf(proj.slowFactor, e.slowFactor),
                    slowRemaining = maxOf(proj.slowDuration, e.slowRemaining)
                )
            }
        }
        if (proj.freezeChance > 0f && random.nextFloat() < proj.freezeChance) {
            e = e.copy(frozenRemaining = maxOf(e.frozenRemaining, proj.slowDuration.coerceAtLeast(1f)))
        }
        if (proj.burnDps > 0f && proj.burnDuration > 0f) {
            e = e.copy(
                burnDps = maxOf(proj.burnDps, e.burnDps),
                burnRemaining = maxOf(proj.burnDuration, e.burnRemaining)
            )
        }
        return e
    }

    private fun removeDeadEnemies(session: GameSession): GameSession {
        val dead = session.enemies.filter { it.isDead }
        if (dead.isEmpty()) return session
        val goldEarned = dead.sumOf { (it.type.goldReward * session.meta.goldIncomeMultiplier).toInt() }
        return session.copy(
            enemies = session.enemies.filterNot { it.isDead },
            gold = session.gold + goldEarned
        )
    }

    private fun checkOutcome(session: GameSession): GameSession {
        if (session.lives <= 0) return session.copy(outcome = GameOutcome.LOST, lives = 0)
        val allWavesSpawned = session.waveIndex >= session.level.waves.size
        return if (allWavesSpawned && session.enemies.isEmpty()) session.copy(outcome = GameOutcome.WON) else session
    }

    // --- user actions: build / upgrade / sell ---

    fun canBuildAt(session: GameSession, gridPos: GridPos): Boolean {
        if (gridPos.col !in 0 until session.level.gridWidth) return false
        if (gridPos.row !in 0 until session.level.gridHeight) return false
        if (session.towers.any { it.gridPos == gridPos }) return false
        val point = gridPos.toVec2()
        return minDistanceToPath(point, session.level.groundPath) >= MIN_BUILD_DISTANCE_FROM_PATH &&
            minDistanceToPath(point, session.level.airPath) >= MIN_BUILD_DISTANCE_FROM_PATH
    }

    /** Returns null (instead of throwing) when the build is invalid, so callers can just no-op on null. */
    fun buildTower(session: GameSession, type: TowerType, gridPos: GridPos): GameSession? {
        if (!canBuildAt(session, gridPos)) return null
        if (session.gold < type.baseCost) return null
        val tower = Tower(id = "t-${session.nextEntityId}", type = type, gridPos = gridPos)
        return session.copy(
            towers = session.towers + tower,
            gold = session.gold - type.baseCost,
            nextEntityId = session.nextEntityId + 1
        )
    }

    /**
     * [chosenSpecialization] is required exactly when the tower is about to cross the
     * TowerBalance.SPECIALIZATION_LEVEL threshold and ignored otherwise; passing the
     * wrong tower's specialization for the branch check is rejected (returns null).
     */
    fun upgradeTower(session: GameSession, towerId: String, chosenSpecialization: Specialization? = null): GameSession? {
        val tower = session.towers.find { it.id == towerId } ?: return null
        if (!tower.canUpgrade) return null
        val cost = tower.upgradeCost() ?: return null
        if (session.gold < cost) return null
        if (tower.needsSpecializationChoice && (chosenSpecialization == null || chosenSpecialization.towerType != tower.type)) return null

        val upgraded = tower.copy(
            level = tower.level + 1,
            specialization = if (tower.needsSpecializationChoice) chosenSpecialization else tower.specialization
        )
        return session.copy(
            towers = session.towers.map { if (it.id == towerId) upgraded else it },
            gold = session.gold - cost
        )
    }

    fun sellTower(session: GameSession, towerId: String): GameSession? {
        val tower = session.towers.find { it.id == towerId } ?: return null
        return session.copy(
            towers = session.towers.filterNot { it.id == towerId },
            gold = session.gold + sellValue(tower)
        )
    }

    private fun sellValue(tower: Tower): Int {
        var spent = tower.type.baseCost
        for (lvl in 1 until tower.level) {
            spent += TowerBalance.upgradeCost(tower.type, lvl) ?: 0
        }
        return (spent * SELL_REFUND_FRACTION).toInt()
    }

    private fun minDistanceToPath(point: Vec2, path: List<Vec2>): Float {
        var min = Float.MAX_VALUE
        for (i in 0 until path.size - 1) {
            val d = distanceToSegment(point, path[i], path[i + 1])
            if (d < min) min = d
        }
        return min
    }

    private fun distanceToSegment(point: Vec2, a: Vec2, b: Vec2): Float {
        val ab = b - a
        val abLenSq = ab.x * ab.x + ab.y * ab.y
        if (abLenSq < 0.0001f) return point.distanceTo(a)
        val t = (((point.x - a.x) * ab.x) + ((point.y - a.y) * ab.y)) / abLenSq
        val closest = a + ab * t.coerceIn(0f, 1f)
        return point.distanceTo(closest)
    }
}
