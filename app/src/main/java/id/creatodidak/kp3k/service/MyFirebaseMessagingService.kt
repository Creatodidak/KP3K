package id.creatodidak.kp3k.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.dashboard.DashboardOpsional
import androidx.core.net.toUri
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.newversion.NewDashboard
import id.creatodidak.kp3k.newversion.VideoCall.ClientVideoCall
import id.creatodidak.kp3k.newversion.VideoCall.IncomingCallWaiting
import id.creatodidak.kp3k.newversion.dashboard.DashboardKomoditas
import org.json.JSONObject
import java.util.Locale

class MyFirebaseMessagingService : FirebaseMessagingService(), IncomingCallCallback {


    override fun onCreate() {
        super.onCreate()
        Log.d("tes_log", "ONCREATE CALLED")

        val cl = CallListenerNetwork(this)
        cl.callListener(this)
    }

    override fun onIncomingCall(data: JSONObject) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        handleIncomingCall2(null, data)
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i("onMessageReceived: ", "Message received ${remoteMessage.data}")
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "videocall" -> {
                    return;
                }
                "update" -> {
                    showUpdateNotif(remoteMessage)
                }
                "verifikasi" -> {
                    handleVerifikasi(remoteMessage)
                }
                "adminverifikasi" -> {
                    adminhandleVerifikasi(remoteMessage)
                }
                "reminder" -> {
                    handleReminder(remoteMessage)
                }
                "atensi" -> {
                    handleAtensi(remoteMessage)
                }
                else -> {
                    handleDefault(remoteMessage)
                }
            }
            Log.i("onMessageReceived: ", data["type"].toString())
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleDefault(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "DEFAULT_CHANNEL"
        val channelName = "DEFAULT CHANNEL"

        val soundUri =
            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notifupdate).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, NewDashboard::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val msg = remoteMessage.data["msg"] ?: ""

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle(msg)
            .setContentText("Klik untuk melihat detail")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Klik untuk melihat detail"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    @SuppressLint("LaunchActivityFromNotification", "NewApi")
    @RequiresPermission(allOf = [Manifest.permission.VIBRATE, Manifest.permission.POST_NOTIFICATIONS])
    private fun handleIncomingCall2(remoteMessage: RemoteMessage?, customData: JSONObject = JSONObject()) {

        val notificationId = 880801
        val channelId = "VIDEOCALL_CHANNEL"
        val channelName = "Panggilan Masuk"

        val dataMap = remoteMessage?.data ?: customData.keys().asSequence()
            .associateWith { key -> customData.optString(key) }

        val valid_until = dataMap["valid_until"] ?: dataMap["validUntil"]
        val title = dataMap["title"] ?: "Panggilan Masuk"
        val caller_name = dataMap["caller_name"] ?: dataMap["namaCaller"] ?: "User KP3K"
        val callernrp = dataMap["nrpCaller"] ?: dataMap["nrpCaller"] ?: "nrp"
        val dtoken = dataMap["token"] ?: "token"
        val droom = dataMap["room"] ?: "toom"

        val validUntilString = valid_until
        val validUntil = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
                .parse(validUntilString ?: "")?.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null);
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 250, 1000, 500)
            }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val caller = android.app.Person.Builder()
            .setName(title)
            .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.logo))
            .build()

        val fullScreenIntent = Intent(this, IncomingCallWaiting::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(Bundle().apply {
                for ((key, value) in dataMap) {
                    putString(key, value)
                }
            })

        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 2, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptIntent = Intent(this, ClientVideoCall::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notificationId", notificationId)
            putExtra("callernrp", callernrp)
            putExtra("caller", caller_name)
            putExtra("token", dtoken)
            putExtra("room", droom)
            putExtra("from_notify", "yes")
        }

        val acceptPending = PendingIntent.getActivity(
            this,
            0,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(this, CallActionReceiverNew::class.java).apply {
            action = "ACTION_DECLINE_CALL"
            putExtra("notificationId", notificationId)
            putExtra("callernrp", callernrp)
        }

        val declinePending = PendingIntent.getBroadcast(
            this, 1, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 34) – gunakan CallStyle
            Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setCategory(Notification.CATEGORY_CALL)
                .setStyle(
                    Notification.CallStyle.forScreeningCall(caller, declinePending, acceptPending)
                        .setIsVideo(true)
                )
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true)
                .addPerson(caller)
                .build()
        } else {

            val remoteViews = RemoteViews(packageName, R.layout.custom_call_notification)
            remoteViews.setOnClickPendingIntent(R.id.btn_accept, acceptPending)
            remoteViews.setOnClickPendingIntent(R.id.btn_decline, declinePending)

            remoteViews.setTextViewText(R.id.caller_name, caller_name)
            remoteViews.setTextViewText(R.id.call_title, "Klik untuk menjawab panggilan")

            // Android <14 – fallback ke NotificationCompat biasa
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
                .setContentTitle(title ?: "Panggilan Masuk")
                .setContentText("Geser untuk aksi")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSilent(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                //.addAction(R.drawable.ic_accept, "Terima", acceptPending)
                //.addAction(R.drawable.ic_decline, "Tolak", declinePending)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .build()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        NotificationManagerCompat.from(this).notify(notificationId, builder)

        CallSoundManager.playSound(applicationContext)

        validUntil?.let {
            val delay = (it - System.currentTimeMillis()).coerceAtLeast(0L)
            Handler(mainLooper).postDelayed({
                NotificationManagerCompat.from(this).cancel(notificationId)
                CallSoundManager.stopSound()
                showMissedCall()
            }, delay)
        }
    }

    //OLD
    @SuppressLint("LaunchActivityFromNotification")
    @RequiresPermission(allOf = [Manifest.permission.VIBRATE, Manifest.permission.POST_NOTIFICATIONS])
    private fun handleIncomingCall(remoteMessage: RemoteMessage) {
        val notificationId = 880801
        val channelId = "VIDEOCALL_CHANNEL"
        val channelName = "Panggilan Masuk"

        val validUntilString = remoteMessage.data["valid_until"]
        val validUntil = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
                .parse(validUntilString ?: "")?.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val currentTime = System.currentTimeMillis()
        Log.d("FCM_CALL", "validUntil=$validUntil, now=$currentTime")

        if (validUntil != null && currentTime > validUntil) {
            showMissedCall()
            return
        }

        val soundUri = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.ringtone).toUri()

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(
                soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 250, 1000, 500)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, IncomingCallWaiting::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle(remoteMessage.data["title"] ?: "Panggilan Masuk")
            .setContentText("Klik untuk menjawab panggilan")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        CallSoundManager.playSound(applicationContext)

        validUntil?.let { until ->
            val delay = (until - currentTime).coerceAtLeast(0L)
            Handler(mainLooper).postDelayed({
                if (!isCallAnswered) {
                    NotificationManagerCompat.from(this).cancel(notificationId)
                    CallSoundManager.stopSound()
                    showMissedCall()
                }
            }, delay)
        }
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showMissedCall() {
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
            this, 0, intent,
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
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleVerifikasi(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "VERIFIKASI_CHANNEL_V2"
        val channelName = "Informasi Verifikasi"

        val soundUri =
            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notif).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val idverifikasi = remoteMessage.data["idverifikasi"] ?: ""
        val komoditasverifikasi = remoteMessage.data["komoditasverifikasi"] ?: ""
        val intent = Intent(this, DashboardKomoditas::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("komoditas", komoditasverifikasi)
            putExtra("id", idverifikasi)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = when(komoditasverifikasi){
            "jagung" -> R.drawable.ic_jagung_gpt
            "singkong" -> R.drawable.ic_ubi_gpt
            "kedelai" -> R.drawable.ic_kedelai_gpt
            else -> R.drawable.atensi
        }

        val msg = remoteMessage.data["msg"] ?: ""
        val title = if (msg.contains("ditolak", ignoreCase = true)) {
            "Pengajuan Ditolak!"
        } else {
            "Pengajuan Diterima!"
        }


        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, icon))
            .setContentTitle(title)
            .setContentText("KOMODITAS ${komoditasverifikasi.toUpperCase()}\n\n$msg\n\nKlik untuk melihat detail\"")
            .setStyle(NotificationCompat.BigTextStyle().bigText("KOMODITAS ${komoditasverifikasi.toUpperCase()}\n\n$msg\n\nKlik untuk melihat detail\""))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun adminhandleVerifikasi(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "ADMIN_VERIFIKASI_CHANNEL"
        val channelName = "ADMIN_VERIFIKASI_CHANNEL"

        val soundUri = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notif).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val idverifikasi = remoteMessage.data["idverifikasi"] ?: ""
        val komoditasverifikasi = remoteMessage.data["komoditasverifikasi"] ?: ""
        val intent = Intent(this, DashboardKomoditas::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("komoditas", komoditasverifikasi)
            putExtra("id", idverifikasi)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val msg = remoteMessage.data["msg"] ?: ""
        val title = remoteMessage.data["title"] ?: ""
        val icon = when(komoditasverifikasi){
            "jagung" -> R.drawable.ic_jagung_gpt
            "singkong" -> R.drawable.ic_ubi_gpt
            "kedelai" -> R.drawable.ic_kedelai_gpt
            else -> R.drawable.atensi
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, icon))
            .setContentTitle(title)
            .setContentText("KOMODITAS ${komoditasverifikasi.toUpperCase()}\n\n$msg\n\nKlik untuk melihat detail")
            .setStyle(NotificationCompat.BigTextStyle().bigText("KOMODITAS ${komoditasverifikasi.toUpperCase()}\n\n$msg\n\nKlik untuk melihat detail"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }


    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleAtensi(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "ATENSI_CHANNEL_V21"
        val channelName = "Atensi Pimpinan"

        val soundUri =
            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notifupdate).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, DashboardOpsional::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val msg = remoteMessage.data["msg"] ?: ""

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle(msg)
            .setContentText("Klik untuk melihat detail")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Klik untuk melihat detail"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleReminder(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "REMINDER_CHANNEL"
        val channelName = "Mengingatkan Tugas"

        val soundUri =
            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notifupdate).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, DashboardOpsional::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val msg = remoteMessage.data["msg"] ?: ""
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle("Pengingat dari Admin")
            .setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun showUpdateNotif(remoteMessage: RemoteMessage) {
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "UPDATE_CHANNEL_V4"
        val channelName = "Informasi Update"

        val soundUri =
            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notifupdate).toUri()
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250, 250)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://play.google.com/store/apps/details?id=id.creatodidak.kp3k".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentTitle("Update Terbaru KP3K")
            .setContentText("Klik untuk mengunduh update")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Klik untuk mengunduh update"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setAutoCancel(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, builder.build())

    }

    companion object {
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


    object staticNotify {

        fun cancelIncomingCallNotification(ctx: Context) {
            val notificationId = 880801
            NotificationManagerCompat.from(ctx).cancel(notificationId)
            CallSoundManager.stopSound()

        }
    }

    object CallSoundManager {
        private var mediaPlayer: MediaPlayer? = null
        private var vibrator: Vibrator? = null
        private var isPlaying = false

        fun playSound(context: Context) {
            if (isPlaying) return //

            isPlaying = true

            mediaPlayer = MediaPlayer.create(context, R.raw.ringtone)?.apply {
                isLooping = true
                start()
            }

            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 250, 250, 250, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }


        fun stopSound() {
            isPlaying = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            vibrator?.cancel()
            vibrator = null
        }
    }
}

class CallActionReceiverNew : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("notificationId", 880801)
        val callerNRP = intent.getStringExtra("callernrp")
        when (intent.action) {
            "ACTION_DECLINE_CALL" -> {

                Log.d("tes_log", "NOTIFY DECLINE CLICKED")
                NotificationManagerCompat.from(context).cancel(id)
                if (callerNRP != null) {

                    MyFirebaseMessagingService.isCallAnswered = true
                    MyFirebaseMessagingService.CallSoundManager.stopSound()

                    val data = JSONObject().apply {
                        put("to", getMyNrp(context))
                        put("from", callerNRP)
                        put("reason", "tidak dapat menerima panggilan")
                    }

                    SocketManager.getSocket().emit("reject-call", data)
                }
                IncomingCallWaiting.instance?.finish()
            }
        }
    }
}

//OLD
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
