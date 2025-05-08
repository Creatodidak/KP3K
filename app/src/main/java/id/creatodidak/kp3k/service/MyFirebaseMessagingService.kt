package id.creatodidak.kp3k.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.creatodidak.kp3k.IncomingCallActivity
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.VideoCallActivity
import id.creatodidak.kp3k.dashboard.DashboardOpsional

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var mediaPlayer: MediaPlayer? = null

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "incoming_call" -> {
                    handleIncomingCall(remoteMessage)
                    Log.i("RECEIVED AGORA TOKEN", data["token"].toString())
                }
                "test" -> {
                    showAnimeNotification()
                }
                "update" -> {
                    showUpdateNotif()
                }
            }
            Log.i("onMessageReceived: ", data["type"].toString())
        }
    }


    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleIncomingCall(remoteMessage: RemoteMessage) {
        val notificationId = 880801
        val channelId = "INCOMING_CALL_CHANNEL"
        val channelName = "Panggilan Masuk"

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.ringtone)

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 500)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, IncomingCallActivity::class.java)
        intent.putExtra("channel", remoteMessage.data["channel"])
        intent.putExtra("token", remoteMessage.data["token"])

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle("Panggilan Masuk")
            .setContentText("Klik untuk menjawab panggilan")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        CallSoundManager.playSound(applicationContext)
        // Timeout setelah 50 detik jika tidak dijawab
        android.os.Handler(mainLooper).postDelayed({
            if (!isCallAnswered) {
                NotificationManagerCompat.from(this).cancel(notificationId)
                CallSoundManager.stopSound()
                showMissedCallNotification()
            }
        }, 50_000) // 50 detik

    }

    private fun showMissedCallNotification() {
        val missedChannelId = "MISSED_CALL_CHANNEL"
        val missedChannelName = "Panggilan Tidak Terjawab"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val missedChannel = NotificationChannel(
                missedChannelId,
                missedChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi panggilan tidak terjawab"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(missedChannel)
        }

        val intent = Intent(this, DashboardOpsional::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val missedNotif = NotificationCompat.Builder(this, missedChannelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Panggilan Tidak Terjawab")
            .setContentText("Anda melewatkan panggilan.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(880802, missedNotif)
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

        // Flag static untuk status panggilan
        @JvmStatic
        var isCallAnswered: Boolean = false
    }


    object CallSoundManager {
        private var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null

        fun playSound(context: Context) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.ringtone)?.apply {
                    isLooping = true
                    start()
                }
            }

            if (vibrator == null) {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val pattern = longArrayOf(0, 250, 250, 250, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(pattern, 0) // 0 = ulangi terus
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }
        }

        fun stopSound() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            vibrator?.cancel()
            vibrator = null
        }
    }


}
