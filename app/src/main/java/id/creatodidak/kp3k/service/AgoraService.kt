package id.creatodidak.kp3k.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class AgoraService : Service() {

    private lateinit var rtcEngine: RtcEngine
    private var isRtcInitialized = false

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
            startForeground(1, notification)
        initializeAgora()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = intent?.getStringExtra("channel")

        if (channel != null) {
            joinChannel(null, channel)
        }

        return START_STICKY
    }

    private fun initializeAgora() {
        if (isRtcInitialized) return

        val appId = "f5a427c2bfd44f3e8285507e4c1ee34f" // Ganti dengan App ID kamu

        rtcEngine = RtcEngine.create(applicationContext, appId, object : IRtcEngineEventHandler() {
            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d("AGORA", "User joined: $uid")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                Log.d("AGORA", "User offline: $uid, reason: $reason")
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                Log.d("AGORA", "Join channel success: $channel, uid: $uid")
            }
        })

        isRtcInitialized = true
        Log.d("AGORA", "RtcEngine initialized")
    }

    private fun joinChannel(token: String?, channelName: String) {
        if (!isRtcInitialized) {
            Log.e("AGORA", "RtcEngine not initialized")
            return
        }

        rtcEngine.joinChannel(token, channelName, "", 0)
        Log.d("AGORA", "Joining channel: $channelName with token: $token")
    }

    private fun createNotification(): Notification {
        val channelId = "agora_service_channel"
        val channelName = "Agora Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Layanan Panggilan Aktif")
            .setContentText("Terhubung ke layanan panggilan")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRtcInitialized) {
            RtcEngine.destroy()
            isRtcInitialized = false
            Log.d("AGORA", "RtcEngine destroyed")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
