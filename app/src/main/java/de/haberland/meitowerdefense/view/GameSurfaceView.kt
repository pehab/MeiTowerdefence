package de.haberland.meitowerdefense.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import de.haberland.meitowerdefense.engine.GameThread
import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.render.GameCamera
import de.haberland.meitowerdefense.render.GameRenderer
import de.haberland.meitowerdefense.render.PlacementPreview
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.ui.GameController
import de.haberland.meitowerdefense.ui.InputMode

/**
 * The game canvas itself. [controller] must be assigned before the surface is created
 * (GameActivity does this right after inflating the view) - everything here just reads
 * from it (touch -> onTapGrid) or drives it (GameThread -> controller.tick per frame).
 *
 * [camera] and [lastTouchGridPos] are written on the main thread (surfaceChanged, touch
 * events) and read from GameThread's background thread in [onRender] - @Volatile is
 * what makes those writes visible there; see GameController's class doc for the same
 * reasoning applied to the session itself.
 */
class GameSurfaceView(context: Context, attrs: AttributeSet? = null) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    var controller: GameController? = null

    private val renderer = GameRenderer()

    @Volatile
    private var camera: GameCamera? = null

    @Volatile
    private var lastTouchGridPos: GridPos? = null

    private var thread: GameThread? = null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread = GameThread(holder, onTick = ::onTick, onRender = ::onRender).also {
            it.running = true
            it.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val level = controller?.session?.level ?: return
        camera = GameCamera(level.gridWidth, level.gridHeight, width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread?.running = false
        var joined = false
        while (!joined) {
            try {
                thread?.join()
                joined = true
            } catch (_: InterruptedException) {
                // keep trying until the thread actually stops
            }
        }
        thread = null
    }

    private fun onTick(dt: Float) {
        controller?.tick(dt)
    }

    private fun onRender(canvas: Canvas) {
        val ctrl = controller ?: return
        val cam = camera ?: return
        renderer.draw(canvas, ctrl.session, cam, ctrl.selectedTowerId, placementPreview(ctrl, cam))
    }

    private fun placementPreview(ctrl: GameController, cam: GameCamera): PlacementPreview? {
        val mode = ctrl.inputMode
        if (mode !is InputMode.Placing) return null
        val pos = lastTouchGridPos ?: return null
        val session = ctrl.session
        val valid = GameSimulator.canBuildAt(session, pos) && session.gold >= mode.type.baseCost
        return PlacementPreview(mode.type, pos, mode.type.baseRange, valid)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cam = camera ?: return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                lastTouchGridPos = cam.screenToGrid(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                controller?.onTapGrid(cam.screenToGrid(event.x, event.y))
                lastTouchGridPos = null
            }
            MotionEvent.ACTION_CANCEL -> {
                lastTouchGridPos = null
            }
        }
        return true
    }
}
