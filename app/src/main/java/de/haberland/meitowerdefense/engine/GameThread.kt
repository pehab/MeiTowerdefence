package de.haberland.meitowerdefense.engine

import android.view.SurfaceHolder

/**
 * The classic Android SurfaceView game loop: on its own thread, repeatedly compute how
 * much time passed since the last frame, call [onTick] with that delta, then draw a
 * frame via [onRender]. [running] is the stop flag GameSurfaceView flips in
 * surfaceDestroyed(), then joins this thread to make sure it's really stopped before the
 * surface goes away.
 *
 * Delta time is clamped to [MAX_DELTA_SECONDS]: without that, resuming after the app was
 * backgrounded for a while would hand the simulation a single giant delta (minutes, even
 * hours), which could make enemies jump straight past every tower in one tick, or spend
 * a long time "catching up" on spawns. Clamping means time that was skipped while
 * backgrounded is simply lost rather than simulated all at once - the reasonable
 * behaviour for a real-time game (contrast with an idle/incremental game, where
 * simulating the gap would be the whole point).
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val onTick: (deltaSeconds: Float) -> Unit,
    private val onRender: (android.graphics.Canvas) -> Unit
) : Thread("GameThread") {

    @Volatile
    var running = false

    override fun run() {
        var lastTimeNanos = System.nanoTime()

        while (running) {
            val frameStartNanos = System.nanoTime()
            val deltaSeconds = ((frameStartNanos - lastTimeNanos) / 1_000_000_000f)
                .coerceIn(0f, MAX_DELTA_SECONDS)
            lastTimeNanos = frameStartNanos

            onTick(deltaSeconds)

            var canvas: android.graphics.Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        onRender(canvas)
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            val elapsedNanos = System.nanoTime() - frameStartNanos
            val remainingNanos = TARGET_FRAME_NANOS - elapsedNanos
            if (remainingNanos > 0L) {
                java.util.concurrent.locks.LockSupport.parkNanos(remainingNanos)
            }
        }
    }

    companion object {
        private const val MAX_DELTA_SECONDS = 0.1f
        private const val TARGET_FPS = 60L
        private const val TARGET_FRAME_NANOS = 1_000_000_000L / TARGET_FPS
    }
}
