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

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(1, notification) // Foreground service WAJIB punya notifikasi
        initializeAgora()
    }

    private fun initializeAgora() {
        val appId = "29942f98950b4396b940f81670ae6fd7" // Ganti dengan App ID kamu
        rtcEngine = RtcEngine.create(applicationContext, appId, object : IRtcEngineEventHandler() {
            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d("AGORA", "User joined: $uid")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                Log.d("AGORA", "User offline: $uid, reason: $reason")
            }
        })

        Log.d("AGORA", "RtcEngine initialized")
    }

    fun joinChannel(token: String?, channelName: String) {
        rtcEngine.joinChannel(token, channelName, "", 0)
        Log.d("AGORA", "Joined channel: $channelName")
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
            .setContentText("Menunggu panggilan masuk...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngine.destroy()
        Log.d("AGORA", "RtcEngine destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
