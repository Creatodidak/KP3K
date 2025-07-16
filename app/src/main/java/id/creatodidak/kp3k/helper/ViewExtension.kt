package id.creatodidak.kp3k.helper

import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlin.math.abs

fun View.enableDragAndSnap(marginDp: Float = 24f, autoFadeDelayMillis: Long = 1000L) {
    var dX = 0f
    var dY = 0f
    var startRawX = 0f
    var startRawY = 0f

    val handler = Handler(Looper.getMainLooper())
    val marginPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        marginDp,
        this.resources.displayMetrics
    )

    // Fungsi untuk membuat transparan
    val fadeRunnable = Runnable {
        this.animate().alpha(0.5f).setDuration(300).start()
    }

    fun resetFadeTimer() {
        handler.removeCallbacks(fadeRunnable)
        this.animate().alpha(1f).setDuration(200).start()
        handler.postDelayed(fadeRunnable, autoFadeDelayMillis)
    }

    this.setOnTouchListener { view, event ->
        val parent = view.parent as ViewGroup
        val screenWidth = parent.width
        val screenHeight = parent.height

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = view.x - event.rawX
                dY = view.y - event.rawY
                startRawX = event.rawX
                startRawY = event.rawY
                resetFadeTimer()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = event.rawX + dX
                val newY = event.rawY + dY
                view.x = newX.coerceIn(marginPx, screenWidth - view.width - marginPx)
                view.y = newY.coerceIn(marginPx, screenHeight - view.height - marginPx)
                resetFadeTimer()
                true
            }

            MotionEvent.ACTION_UP -> {
                if (abs(event.rawX - startRawX) < 10 && abs(event.rawY - startRawY) < 10) {
                    view.performClick()
                    resetFadeTimer()
                    return@setOnTouchListener true
                }

                val toLeft = view.x < (screenWidth / 2)
                val y = view.y
                val centerY = screenHeight / 2f
                val snapY = when {
                    y < centerY * 0.75f -> marginPx
                    y > centerY * 1.25f -> screenHeight - view.height - marginPx
                    else -> (screenHeight - view.height) / 2f
                }

                val targetX = if (toLeft) marginPx else screenWidth - view.width - marginPx

                view.animate()
                    .x(targetX)
                    .y(snapY)
                    .setDuration(200)
                    .withEndAction {
                        resetFadeTimer()
                    }
                    .start()

                true
            }

            else -> false
        }
    }

    // Mulai timer pertama kali saat dipasang
    resetFadeTimer()
}
