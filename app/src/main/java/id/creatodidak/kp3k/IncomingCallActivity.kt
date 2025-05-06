package id.creatodidak.kp3k

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.creatodidak.kp3k.service.AgoraService
class IncomingCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        // Membuat Notification Channel untuk Android Oreo ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CALL_CHANNEL",
                "Panggilan Masuk",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val channel = intent.getStringExtra("channel")
        val token = intent.getStringExtra("token")

        findViewById<Button>(R.id.btnAccept).setOnClickListener {
            joinAgoraChannel(channel, token)
        }

        findViewById<Button>(R.id.btnReject).setOnClickListener {
            // Logic to reject the call
            finish()  // Close the Incoming Call activity or end the service
        }
    }

    private fun joinAgoraChannel(channel: String?, token: String?) {
        // Gabung ke channel Agora (panggil fungsi dari AgoraService)
        val serviceIntent = Intent(this, AgoraService::class.java)
        serviceIntent.putExtra("channel", channel)
        serviceIntent.putExtra("token", token)
        startService(serviceIntent)

        // Tampilkan UI video call jika perlu
    }
}
