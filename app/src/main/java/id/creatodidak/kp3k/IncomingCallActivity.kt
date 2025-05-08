package id.creatodidak.kp3k

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.service.MyFirebaseMessagingService

class IncomingCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        val token = intent.getStringExtra("token")
        val acceptButton = findViewById<FloatingActionButton>(R.id.btAccept)
        val declineButton = findViewById<FloatingActionButton>(R.id.btDecline)

        acceptButton.setOnClickListener {
            MyFirebaseMessagingService.isCallAnswered = true
            MyFirebaseMessagingService.CallSoundManager.stopSound()

            val intent = Intent(this, VideoCallActivity::class.java).apply {
                putExtra("token", token)
            }
            startActivity(intent)
            finish()
        }

        declineButton.setOnClickListener {
            MyFirebaseMessagingService.CallSoundManager.stopSound()
            finish()
        }
    }
}
