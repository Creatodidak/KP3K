package id.creatodidak.kp3k

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.service.MyFirebaseMessagingService

class IncomingCallActivity : AppCompatActivity() {
    private var dYAccept = 0f
    private var dYDecline = 0f
    private var initialYAccept = 0f
    private var initialYDecline = 0f
    private val SWIPE_THRESHOLD = 200f // cukup geser 200px ke atas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        val notificationId = intent.getIntExtra("notificationId", 880801)
        NotificationManagerCompat.from(this).cancel(notificationId)
        MyFirebaseMessagingService.isCallAnswered = true


        val acceptButton = findViewById<FloatingActionButton>(R.id.btAccept)
        val declineButton = findViewById<FloatingActionButton>(R.id.btDecline)

        // Simpan posisi awal
        acceptButton.post { initialYAccept = acceptButton.y }
        declineButton.post { initialYDecline = declineButton.y }

        setupSwipeGesture(acceptButton, isAccept = true)
        setupSwipeGesture(declineButton, isAccept = false)

        // ➕ Entry animation with bounce
        playEntryBounce(acceptButton)
        playEntryBounce(declineButton)
        val caller = intent.getStringExtra("caller")
        val tvCaller = findViewById<TextView>(R.id.tvCaller)
        tvCaller.text = caller
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeGesture(button: FloatingActionButton, isAccept: Boolean) {
        button.setOnTouchListener { view, event ->
            val dY = if (isAccept) dYAccept else dYDecline
            val initialY = if (isAccept) initialYAccept else initialYDecline

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isAccept) dYAccept = view.y - event.rawY else dYDecline = view.y - event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val newY = event.rawY + dY
                    if (newY < view.y) {
                        view.animate().y(newY).setDuration(0).start()
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val finalY = view.y
                    if (finalY < initialY - SWIPE_THRESHOLD) {
                        if (isAccept) onAcceptCall(view) else onDeclineCall(view)
                    } else {
                        resetButton(view, initialY)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun onAcceptCall(view: View) {
        playGlideUpAnimation(view)
        MyFirebaseMessagingService.isCallAnswered = true
        MyFirebaseMessagingService.CallSoundManager.stopSound()
        val tvCaller = findViewById<TextView>(R.id.tvCaller)

        val token = intent.getStringExtra("token")
        val intent = Intent(this, VideoCallActivity::class.java)
        intent.putExtra("token", token)
        intent.putExtra("caller", tvCaller.text.toString())

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 300)
    }

    private fun onDeclineCall(view: View) {
        playGlideUpAnimation(view)
        MyFirebaseMessagingService.isCallAnswered = true
        MyFirebaseMessagingService.CallSoundManager.stopSound()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 300)
    }

    private fun resetButton(view: View, initialY: Float) {
        ObjectAnimator.ofFloat(view, "y", view.y, initialY).apply {
            duration = 400
            interpolator = OvershootInterpolator(2f) // ⬅️ ini bikin bounce
            start()
        }
    }


    private fun playEntryBounce(button: View) {
        // Mulai dari 300px di bawah posisi awal
        button.translationY = 300f

        ObjectAnimator.ofFloat(button, "translationY", 0f).apply {
            duration = 800
            interpolator = OvershootInterpolator(1.5f) // bounce saat mencapai 0
            start()
        }
    }

    private fun playGlideUpAnimation(view: View) {
        val glideAnim = TranslateAnimation(0f, 0f, 0f, -view.height.toFloat()).apply {
            duration = 200
            fillAfter = true
        }
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 200
            fillAfter = true
        }
        view.startAnimation(glideAnim)
        view.startAnimation(fadeOut)
    }
}
