package de.haberland.meitowerdefense.render

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.sim.Enemy
import de.haberland.meitowerdefense.sim.GameSession
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.sim.Projectile
import de.haberland.meitowerdefense.sim.Tower
import kotlin.math.hypot

/**
 * Draws painted terrain and textured routes sourced from the level definition, so the
 * visible road and flight indicators follow exactly the same waypoints as the simulation.
 */
class GameRenderer(context: Context) {
    private val resources = context.resources
    private val terrainResources = mapOf(
        "forest_path" to R.drawable.forest_field,
        "mountain_pass" to R.drawable.mountain_field,
        "valley" to R.drawable.valley_field,
        "riverbank" to R.drawable.riverbank_field,
        "serpentines" to R.drawable.serpentine_field,
        "crossroads" to R.drawable.crossroads_field,
        "fortress" to R.drawable.fortress_field,
        "last_wall" to R.drawable.last_wall_field,
        "endless" to R.drawable.endless_field
    )
    // A renderer belongs to one running level; decode only its background, not all nine maps.
    private var terrainBitmap: Bitmap? = null
    private var terrainLevelId: String? = null
    private val terrainPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val towerSprites = mapOf(
        TowerType.ARCHER to BitmapFactory.decodeResource(resources, R.drawable.tower_archer_atlas),
        TowerType.CANNON to BitmapFactory.decodeResource(resources, R.drawable.tower_cannon_atlas),
        TowerType.FIRE to BitmapFactory.decodeResource(resources, R.drawable.tower_fire_atlas),
        TowerType.ICE to BitmapFactory.decodeResource(resources, R.drawable.tower_ice_atlas)
    )
    private val towerSpritePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    // Atlas cells follow EnemyType.entries: basic, fast, armored, flying, boss.
    private val enemyAtlas = BitmapFactory.decodeResource(resources, R.drawable.enemy_atlas)
    private val enemySpritePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val flyingShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(95, 28, 35, 36) }
    private val towerBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(40, 35, 31) }
    private val dirtTexture = BitmapFactory.decodeResource(resources, R.drawable.dirt_path_texture)
    private val dirtShader = BitmapShader(dirtTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val dirtMatrix = Matrix()
    private val roadPath = Path()
    private val roadTrackSides = floatArrayOf(-1f, 1f)
    private val projectileAtlas = BitmapFactory.decodeResource(resources, R.drawable.projectile_atlas)
    private val projectilePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val pathShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(65, 57, 53, 27)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val pathVergePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 135, 125, 62)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val dirtPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        shader = dirtShader
    }
    private val wheelTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(42, 85, 69, 43)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val airFlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(155, 229, 244, 247)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val airFlowShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(95, 53, 88, 102)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val airFlowPaints = arrayOf(airFlowShadowPaint, airFlowPaint)
    private val buildSitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(115, 213, 185, 120) }
    private val buildSiteEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(205, 65, 54, 34)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val backgroundPaint = Paint().apply { color = Color.rgb(24, 28, 20) }
    private val gridPaint = Paint().apply { color = Color.argb(35, 255, 255, 255); strokeWidth = 1f }
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

    fun draw(
        canvas: Canvas,
        session: GameSession,
        camera: GameCamera,
        selectedTowerId: String?,
        placementPreview: PlacementPreview?,
        placingType: TowerType?
    ) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
        val terrainId = session.level.id
        if (terrainLevelId != terrainId) {
            terrainBitmap = terrainResources[terrainId]?.let { BitmapFactory.decodeResource(resources, it) }
            terrainLevelId = terrainId
        }
        terrainBitmap?.let { bitmap ->
            val (left, top) = camera.gridToScreen(Vec2(0f, 0f))
            val (right, bottom) = camera.gridToScreen(Vec2(camera.gridWidth.toFloat(), camera.gridHeight.toFloat()))
            canvas.drawBitmap(bitmap, null, RectF(left, top, right, bottom), terrainPaint)
        }
        drawGrid(canvas, camera)
        drawGroundRoad(canvas, session.level.groundPath, camera)
        drawAirRoute(canvas, session.level.airPath, camera)

        if (placingType != null && session.gold >= placingType.baseCost) {
            drawBuildSites(canvas, session, camera)
        }
        session.towers.forEach { drawTower(canvas, it, camera, selected = it.id == selectedTowerId) }
        if (session.projectiles.isNotEmpty()) {
            val enemyPositions = session.enemies.associate { it.id to it.position }
            session.projectiles.forEach { drawProjectile(canvas, it, enemyPositions[it.targetEnemyId], camera) }
        }
        session.enemies.forEach { drawEnemy(canvas, it, camera) }

        placementPreview?.let { drawPlacementPreview(canvas, it, camera) }
    }

    private fun drawBuildSites(canvas: Canvas, session: GameSession, camera: GameCamera) {
        val radius = camera.cellSizePx * 0.18f
        for (row in 0 until session.level.gridHeight) {
            for (col in 0 until session.level.gridWidth) {
                if (!GameSimulator.canBuildAt(session, GridPos(col, row))) continue
                val (x, y) = camera.gridToScreen(Vec2(col + 0.5f, row + 0.5f))
                canvas.drawCircle(x, y, radius, buildSitePaint)
                canvas.drawCircle(x, y, radius, buildSiteEdgePaint)
            }
        }
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

    private fun drawGroundRoad(canvas: Canvas, points: List<Vec2>, camera: GameCamera) {
        val path = roadPath
        path.rewind()
        points.forEachIndexed { index, point ->
            val (x, y) = camera.gridToScreen(point)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val cell = camera.cellSizePx
        pathShadowPaint.strokeWidth = cell * 0.83f
        pathVergePaint.strokeWidth = cell * 0.76f
        dirtPaint.strokeWidth = cell * 0.68f
        dirtMatrix.setScale(cell / dirtTexture.width, cell / dirtTexture.height)
        dirtShader.setLocalMatrix(dirtMatrix)
        canvas.drawPath(path, pathShadowPaint)
        canvas.drawPath(path, pathVergePaint)
        canvas.drawPath(path, dirtPaint)

        // Paired worn tracks follow each straight section; the rounded road joins keep
        // corners covered even when the route doubles back through the same cell.
        wheelTrackPaint.strokeWidth = cell * 0.065f
        for (i in 0 until points.lastIndex) {
            val (x1, y1) = camera.gridToScreen(points[i])
            val (x2, y2) = camera.gridToScreen(points[i + 1])
            val length = hypot(x2 - x1, y2 - y1)
            if (length < 1f) continue
            val offsetX = -(y2 - y1) / length * cell * 0.18f
            val offsetY = (x2 - x1) / length * cell * 0.18f
            val inset = minOf(cell * 0.35f / length, 0.25f)
            for (side in roadTrackSides) {
                canvas.drawLine(x1 + (x2 - x1) * inset + offsetX * side,
                    y1 + (y2 - y1) * inset + offsetY * side,
                    x2 - (x2 - x1) * inset + offsetX * side,
                    y2 - (y2 - y1) * inset + offsetY * side, wheelTrackPaint)
            }
        }
    }

    private fun drawAirRoute(canvas: Canvas, points: List<Vec2>, camera: GameCamera) {
        val cell = camera.cellSizePx
        airFlowShadowPaint.strokeWidth = cell * 0.085f
        airFlowPaint.strokeWidth = cell * 0.04f
        // Short wind strokes indicate direction without suggesting a physical road.
        // Spacing is in grid units, so density does not change with screen size.
        var distanceToNext = 0.7f
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val length = hypot(dx, dy)
            if (length == 0f) continue
            val ux = dx / length
            val uy = dy / length
            var along = distanceToNext
            while (along < length) {
                val (x, y) = camera.gridToScreen(Vec2(a.x + ux * along, a.y + uy * along))
                val tipX = x + ux * cell * 0.19f
                val tipY = y + uy * cell * 0.19f
                val tailX = x - ux * cell * 0.20f
                val tailY = y - uy * cell * 0.20f
                for (paint in airFlowPaints) {
                    canvas.drawLine(tailX, tailY, tipX, tipY, paint)
                    canvas.drawLine(tipX - ux * cell * 0.13f - uy * cell * 0.10f,
                        tipY - uy * cell * 0.13f + ux * cell * 0.10f, tipX, tipY, paint)
                    canvas.drawLine(tipX - ux * cell * 0.13f + uy * cell * 0.10f,
                        tipY - uy * cell * 0.13f - ux * cell * 0.10f, tipX, tipY, paint)
                }
                along += 1.25f
            }
            distanceToNext = along - length
        }
    }

    private fun drawTower(canvas: Canvas, tower: Tower, camera: GameCamera, selected: Boolean) {
        val (cx, cy) = camera.gridToScreen(tower.position)
        val half = camera.cellSizePx * 0.36f

        if (selected) {
            val rangePx = tower.range * camera.cellSizePx
            canvas.drawCircle(cx, cy, rangePx, rangePaint)
        }

        drawTowerSprite(canvas, tower.type, tower.specialization, cx, cy, camera.cellSizePx, 255)

        // Level stays readable without hiding the weapon that distinguishes each branch.
        val badgeX = cx + half
        val badgeY = cy + half
        canvas.drawCircle(badgeX, badgeY, camera.cellSizePx * 0.15f, towerBadgePaint)
        towerLevelPaint.textSize = camera.cellSizePx * 0.23f
        canvas.drawText(tower.level.toString(), badgeX, badgeY + towerLevelPaint.textSize * 0.35f, towerLevelPaint)
    }

    private fun drawTowerSprite(
        canvas: Canvas, type: TowerType, specialization: Specialization?,
        cx: Float, cy: Float, cellSize: Float, alpha: Int
    ) {
        val atlas = towerSprites[type] ?: return
        val index = when (specialization) {
            Specialization.ARCHER_SNIPER, Specialization.CANNON_SIEGE,
            Specialization.FIRE_INFERNO, Specialization.ICE_DEEP_FREEZE -> 1
            Specialization.ARCHER_RAPID, Specialization.CANNON_MORTAR,
            Specialization.FIRE_SCORCH, Specialization.ICE_FROSTBITE -> 2
            null -> 0
        }
        val spriteWidth = atlas.width / 3
        val half = cellSize * 0.48f
        towerSpritePaint.alpha = alpha
        canvas.drawBitmap(
            atlas, Rect(index * spriteWidth, 0, (index + 1) * spriteWidth, atlas.height),
            RectF(cx - half, cy - half, cx + half, cy + half), towerSpritePaint
        )
        towerSpritePaint.alpha = 255
    }

    private fun drawEnemy(canvas: Canvas, enemy: Enemy, camera: GameCamera) {
        val (cx, cy) = camera.gridToScreen(enemy.position)
        val half = camera.cellSizePx * spriteHalfSizeFor(enemy.type)
        val radius = half * 0.8f
        if (enemy.type.flying) {
            canvas.drawOval(cx - half * 0.6f, cy + half * 0.45f,
                cx + half * 0.6f, cy + half * 0.8f, flyingShadowPaint)
        }
        val cellWidth = enemyAtlas.width / EnemyType.entries.size
        val index = enemy.type.ordinal
        canvas.drawBitmap(enemyAtlas,
            Rect(index * cellWidth, 0, (index + 1) * cellWidth, enemyAtlas.height),
            RectF(cx - half, cy - half, cx + half, cy + half), enemySpritePaint)

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

        val barWidth = half * 1.7f
        val barHeight = camera.cellSizePx * 0.08f
        val barLeft = cx - barWidth / 2f
        val barTop = cy - half - barHeight - 4f
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight, hpBarBackPaint)
        val hpFraction = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
        canvas.drawRect(barLeft, barTop, barLeft + barWidth * hpFraction, barTop + barHeight, hpBarFrontPaint)
    }

    private fun drawProjectile(canvas: Canvas, projectile: Projectile, target: Vec2?, camera: GameCamera) {
        val (x, y) = camera.gridToScreen(projectile.position)
        val index = when (projectile.sourceTowerType) {
            TowerType.ARCHER -> 0
            TowerType.CANNON -> 1
            TowerType.FIRE -> 2
            TowerType.ICE -> 3
        }
        val spriteWidth = projectileAtlas.width / 4
        val half = camera.cellSizePx * when (projectile.sourceTowerType) {
            TowerType.CANNON -> 0.19f
            TowerType.FIRE -> 0.22f
            TowerType.ARCHER, TowerType.ICE -> 0.25f
        }
        val angle = if (target == null) 0f else
            Math.toDegrees(kotlin.math.atan2((target.y - projectile.position.y).toDouble(),
                (target.x - projectile.position.x).toDouble())).toFloat()
        canvas.save()
        canvas.rotate(angle, x, y)
        canvas.drawBitmap(projectileAtlas,
            Rect(index * spriteWidth, 0, (index + 1) * spriteWidth, projectileAtlas.height),
            RectF(x - half, y - half, x + half, y + half), projectilePaint)
        canvas.restore()
    }

    private fun drawPlacementPreview(canvas: Canvas, preview: PlacementPreview, camera: GameCamera) {
        val (cx, cy) = camera.gridToScreen(preview.gridPos.toVec2())
        val rangePx = preview.range * camera.cellSizePx
        canvas.drawCircle(cx, cy, rangePx, if (preview.valid) rangePaint else rangeInvalidPaint)

        drawTowerSprite(canvas, preview.type, null, cx, cy, camera.cellSizePx,
            if (preview.valid) 175 else 90)
    }

    private fun spriteHalfSizeFor(type: EnemyType): Float = when (type) {
        EnemyType.BASIC -> 0.34f
        EnemyType.FAST -> 0.31f
        EnemyType.ARMORED -> 0.42f
        EnemyType.FLYING -> 0.44f
        EnemyType.BOSS -> 0.62f
    }

}

/** What to draw for a tower the player is about to place, before they confirm the tap. */
data class PlacementPreview(
    val type: TowerType,
    val gridPos: de.haberland.meitowerdefense.model.GridPos,
    val range: Float,
    val valid: Boolean
)
