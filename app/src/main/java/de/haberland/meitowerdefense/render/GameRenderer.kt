package de.haberland.meitowerdefense.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.sim.Enemy
import de.haberland.meitowerdefense.sim.GameSession
import de.haberland.meitowerdefense.sim.Projectile
import de.haberland.meitowerdefense.sim.Tower

/**
 * Draws a [GameSession] with plain Canvas shapes: colored circles per [EnemyType],
 * colored squares per [TowerType], small dots for projectiles. No sprite art (see the
 * project chat for why) - the point of this pass is that every system (targeting,
 * status effects, splash, ground/air separation) is visibly correct on screen, ready to
 * have real art dropped in later without touching any simulation code.
 *
 * Stateless apart from its [Paint] objects (kept as fields purely to avoid reallocating
 * one per shape per frame) - everything it draws comes from the [GameSession] and
 * [GameCamera] passed into [draw].
 */
class GameRenderer {
    private val backgroundPaint = Paint().apply { color = Color.rgb(24, 28, 20) }
    private val gridPaint = Paint().apply { color = Color.argb(35, 255, 255, 255); strokeWidth = 1f }
    private val groundPathPaint = Paint().apply {
        color = Color.rgb(92, 74, 52)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val airPathPaint = Paint().apply {
        color = Color.argb(110, 190, 220, 255)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(16f, 12f), 0f)
    }
    private val rangePaint = Paint().apply { color = Color.argb(70, 255, 255, 255); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val rangeInvalidPaint = Paint().apply { color = Color.argb(70, 255, 90, 90); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val hpBarBackPaint = Paint().apply { color = Color.argb(160, 40, 40, 40) }
    private val hpBarFrontPaint = Paint().apply { color = Color.rgb(120, 220, 120) }
    private val towerLevelPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val shapePaint = Paint().apply { isAntiAlias = true }
    private val outlinePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 3f; color = Color.BLACK }

    fun draw(
        canvas: Canvas,
        session: GameSession,
        camera: GameCamera,
        selectedTowerId: String?,
        placementPreview: PlacementPreview?
    ) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
        drawGrid(canvas, camera)
        drawPolyline(canvas, session.level.groundPath, camera, groundPathPaint, camera.cellSizePx * 0.55f)
        drawPolyline(canvas, session.level.airPath, camera, airPathPaint, camera.cellSizePx * 0.35f)

        session.towers.forEach { drawTower(canvas, it, camera, session.meta, selected = it.id == selectedTowerId) }
        session.projectiles.forEach { drawProjectile(canvas, it, camera) }
        session.enemies.forEach { drawEnemy(canvas, it, camera) }

        placementPreview?.let { drawPlacementPreview(canvas, it, camera) }
    }

    private fun drawGrid(canvas: Canvas, camera: GameCamera) {
        for (col in 0..camera.gridWidth) {
            val (x, yTop) = camera.gridToScreen(Vec2(col.toFloat(), 0f))
            val (_, yBottom) = camera.gridToScreen(Vec2(col.toFloat(), camera.gridHeight.toFloat()))
            canvas.drawLine(x, yTop, x, yBottom, gridPaint)
        }
        for (row in 0..camera.gridHeight) {
            val (xLeft, y) = camera.gridToScreen(Vec2(0f, row.toFloat()))
            val (xRight, _) = camera.gridToScreen(Vec2(camera.gridWidth.toFloat(), row.toFloat()))
            canvas.drawLine(xLeft, y, xRight, y, gridPaint)
        }
    }

    private fun drawPolyline(canvas: Canvas, points: List<Vec2>, camera: GameCamera, paint: Paint, strokeWidth: Float) {
        paint.strokeWidth = strokeWidth
        for (i in 0 until points.size - 1) {
            val (x1, y1) = camera.gridToScreen(points[i])
            val (x2, y2) = camera.gridToScreen(points[i + 1])
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    private fun drawTower(canvas: Canvas, tower: Tower, camera: GameCamera, meta: MetaProgress, selected: Boolean) {
        val (cx, cy) = camera.gridToScreen(tower.position)
        val half = camera.cellSizePx * 0.36f

        if (selected) {
            val rangePx = tower.range * camera.cellSizePx
            canvas.drawCircle(cx, cy, rangePx, rangePaint)
        }

        shapePaint.color = colorForTower(tower.type)
        canvas.drawRect(cx - half, cy - half, cx + half, cy + half, shapePaint)
        canvas.drawRect(cx - half, cy - half, cx + half, cy + half, outlinePaint)

        towerLevelPaint.textSize = camera.cellSizePx * 0.34f
        canvas.drawText(tower.level.toString(), cx, cy + towerLevelPaint.textSize * 0.35f, towerLevelPaint)
    }

    private fun drawEnemy(canvas: Canvas, enemy: Enemy, camera: GameCamera) {
        val (cx, cy) = camera.gridToScreen(enemy.position)
        val radius = camera.cellSizePx * radiusFractionFor(enemy.type)

        shapePaint.color = colorForEnemy(enemy.type)
        canvas.drawCircle(cx, cy, radius, shapePaint)
        outlinePaint.strokeWidth = 2.5f
        canvas.drawCircle(cx, cy, radius, outlinePaint)

        if (enemy.frozenRemaining > 0f) {
            shapePaint.color = Color.argb(140, 170, 230, 255)
            canvas.drawCircle(cx, cy, radius * 1.25f, shapePaint)
        } else if (enemy.slowRemaining > 0f) {
            shapePaint.color = Color.argb(90, 150, 210, 255)
            canvas.drawCircle(cx, cy, radius * 1.2f, shapePaint)
        }
        if (enemy.burnRemaining > 0f) {
            shapePaint.color = Color.argb(140, 255, 140, 40)
            canvas.drawCircle(cx, cy - radius * 1.4f, radius * 0.32f, shapePaint)
        }

        val barWidth = radius * 2.2f
        val barHeight = camera.cellSizePx * 0.08f
        val barLeft = cx - barWidth / 2f
        val barTop = cy - radius - barHeight - 4f
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight, hpBarBackPaint)
        val hpFraction = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
        canvas.drawRect(barLeft, barTop, barLeft + barWidth * hpFraction, barTop + barHeight, hpBarFrontPaint)
    }

    private fun drawProjectile(canvas: Canvas, projectile: Projectile, camera: GameCamera) {
        val (x, y) = camera.gridToScreen(projectile.position)
        shapePaint.color = colorForTower(projectile.sourceTowerType)
        canvas.drawCircle(x, y, camera.cellSizePx * 0.09f, shapePaint)
    }

    private fun drawPlacementPreview(canvas: Canvas, preview: PlacementPreview, camera: GameCamera) {
        val (cx, cy) = camera.gridToScreen(preview.gridPos.toVec2())
        val rangePx = preview.range * camera.cellSizePx
        canvas.drawCircle(cx, cy, rangePx, if (preview.valid) rangePaint else rangeInvalidPaint)

        val half = camera.cellSizePx * 0.36f
        shapePaint.color = colorForTower(preview.type)
        shapePaint.alpha = if (preview.valid) 160 else 90
        canvas.drawRect(cx - half, cy - half, cx + half, cy + half, shapePaint)
        shapePaint.alpha = 255
    }

    private fun radiusFractionFor(type: EnemyType): Float = when (type) {
        EnemyType.BASIC -> 0.26f
        EnemyType.FAST -> 0.20f
        EnemyType.ARMORED -> 0.32f
        EnemyType.FLYING -> 0.24f
        EnemyType.BOSS -> 0.46f
    }

    private fun colorForEnemy(type: EnemyType): Int = when (type) {
        EnemyType.BASIC -> Color.rgb(210, 210, 210)
        EnemyType.FAST -> Color.rgb(240, 220, 60)
        EnemyType.ARMORED -> Color.rgb(120, 100, 80)
        EnemyType.FLYING -> Color.rgb(150, 210, 255)
        EnemyType.BOSS -> Color.rgb(190, 40, 60)
    }

    private fun colorForTower(type: TowerType): Int = when (type) {
        TowerType.ARCHER -> Color.rgb(90, 170, 90)
        TowerType.CANNON -> Color.rgb(70, 70, 75)
        TowerType.FIRE -> Color.rgb(220, 110, 40)
        TowerType.ICE -> Color.rgb(80, 180, 220)
    }
}

/** What to draw for a tower the player is about to place, before they confirm the tap. */
data class PlacementPreview(
    val type: TowerType,
    val gridPos: de.haberland.meitowerdefense.model.GridPos,
    val range: Float,
    val valid: Boolean
)
