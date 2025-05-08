package id.creatodidak.kp3k.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
//                    showAnimeNotification()
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

        // Intent untuk Answer
        val answerIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("channel", remoteMessage.data["channel"])
            putExtra("token", remoteMessage.data["token"])
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Intent untuk Decline
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "DECLINE_ACTION"
            putExtra("notificationId", notificationId)
        }

        val answerPendingIntent = PendingIntent.getActivity(
            this,
            0,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle("Panggilan Masuk")
            .setContentText("Klik untuk menjawab panggilan")
            .addAction(R.drawable.baseline_cancel_24, "Tolak", declinePendingIntent)
            .addAction(R.drawable.baseline_call_24, "Jawab", answerPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(answerPendingIntent, true)
            .setAutoCancel(true)
            .setTimeoutAfter(50000)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        CallSoundManager.playSound(applicationContext)

        // Timeout handler
        android.os.Handler(mainLooper).postDelayed({
            if (!isCallAnswered) {
                NotificationManagerCompat.from(this).cancel(notificationId)
                CallSoundManager.stopSound()
                showMissedCallNotification(this)
            }
        }, 50000)
    }

    companion object {
        private const val CALL_CHANNEL_ID = "CALL_CHANNEL"
        private const val ANIME_CHANNEL_ID = "anime_channel_id"
        private const val UPDATE_CHANNEL_ID = "update_channel_id"
        private const val CALL_NOTIFICATION_ID = 1001
        private const val ANIME_NOTIFICATION_ID = 2001
        private const val UPDATE_NOTIFICATION_ID = 3001

        @JvmStatic
        var isCallAnswered: Boolean = false

        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        @JvmStatic
        fun showMissedCallNotification(context: Context) {
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
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(missedChannel)
            }

            val intent = Intent(context, DashboardOpsional::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val missedNotif = NotificationCompat.Builder(context, missedChannelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Panggilan Tidak Terjawab")
                .setContentText("Anda melewatkan panggilan.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(context).notify(880802, missedNotif)
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

    // ... (fungsi lainnya tetap sama seperti sebelumnya)
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

class CallActionReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "DECLINE_ACTION" -> {
                MyFirebaseMessagingService.CallSoundManager.stopSound()
                val notificationId = intent.getIntExtra("notificationId", 880801)
                NotificationManagerCompat.from(context).cancel(notificationId)
                MyFirebaseMessagingService.showMissedCallNotification(context)
            }
        }
    }
}