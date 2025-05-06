package id.creatodidak.kp3k.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.creatodidak.kp3k.IncomingCallActivity
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.dashboard.DashboardOpsional

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "incoming_call" -> {
                    val channelName = data["channel"]
                    val token = data["token"]
                    showIncomingCallNotification(channelName, token)
                }
                "test" -> {
                    showAnimeNotification()
                }
                "update" -> {
                    showUpdateNotif()
                }
            }
            Log.i( "onMessageReceived: ", data["type"].toString())
        }
    }

    private fun showIncomingCallNotification(channelName: String?, token: String?) {
        createCallChannel()

        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("channel", channelName)
            putExtra("token", token)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_phone_callback_24)
            .setContentTitle("Panggilan Masuk")
            .setContentText("Ada panggilan dari Admin")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        try {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(CALL_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission missing, gagal kirim notifikasi panggilan
        }
    }

    private fun showUpdateNotif() {
        createUpdateChannel()

        val intent = Intent(this, DashboardOpsional::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Update Tersedia!")
            .setContentText("Klik untuk mendapatkan update!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        try {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(UPDATE_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission missing, gagal kirim notifikasi panggilan
        }
    }

    private fun showAnimeNotification() {
        // Cek permission POST_NOTIFICATIONS di Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission belum diberikan, bisa minta di UI jika perlu
            return
        }

        createAnimeChannel()

        val intent = Intent(this, DashboardOpsional::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logo)
        val smallIcon = R.drawable.logo

        val notification = NotificationCompat.Builder(this, ANIME_CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle("Slime Taoshite 300-nen")
            .setContentText("Episode 5 sudah tersedia!")
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(false)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(ANIME_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission missing, gagal kirim notifikasi anime
        }
    }

    private fun createCallChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CALL_CHANNEL_ID,
                "Panggilan Masuk",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk panggilan masuk"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
    private fun createUpdateChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "Update Aplikasi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk Update Aplikasi"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun createAnimeChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ANIME_CHANNEL_ID,
                "Anime Updates",
                NotificationManager.IMPORTANCE_HIGH    // heads-up
            ).apply {
                description = "Notifikasi update episode anime"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound( RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    Notification.AUDIO_ATTRIBUTES_DEFAULT )
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }


    companion object {
        private const val CALL_CHANNEL_ID = "CALL_CHANNEL"
        private const val ANIME_CHANNEL_ID = "anime_channel_id"
        private const val UPDATE_CHANNEL_ID = "update_channel_id"
        private const val CALL_NOTIFICATION_ID = 1001
        private const val ANIME_NOTIFICATION_ID = 2001
        private const val UPDATE_NOTIFICATION_ID = 3001
    }
}
