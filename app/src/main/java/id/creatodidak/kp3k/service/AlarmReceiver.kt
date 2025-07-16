package id.creatodidak.kp3k.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlarmManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import id.creatodidak.kp3k.R
import org.json.JSONArray

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        var ringtone: Ringtone? = null
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val isiReminder = intent.getStringExtra("isi") ?: "Ada reminder"
        val reminderId = intent.getStringExtra("id") ?: ""

        if (intent.action == "STOP_ALARM") {
            // 🔕 Hentikan ringtone
            ringtone?.stop()

            // 🧠 Set alarm tidak aktif
            val prefs = context.getSharedPreferences("REMINDER_PANEN", Context.MODE_PRIVATE)
            prefs.edit { putBoolean("alarmAktif", false) }

            // ❌ Hapus reminder dari REMINDER_LIST
            val list = JSONArray(prefs.getString("REMINDER_LIST", "[]"))
            val newList = JSONArray()
            for (i in 0 until list.length()) {
                val obj = list.getJSONObject(i)
                if (obj.getString("id") != reminderId) {
                    newList.put(obj)
                }
            }
            prefs.edit {
                putString("REMINDER_LIST", newList.toString())
            }

            // ❌ Hapus notifikasi
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.cancel(999)

            // 📡 Kirim broadcast ke Activity untuk menutup aplikasi
            val stopAppIntent = Intent("ACTION_STOP_ALARM")
            context.sendBroadcast(stopAppIntent)

            return
        }

        val channelId = "REMINDER_CHANNEL"
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat channel jika perlu (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notifManager.createNotificationChannel(channel)
        }

        // 🔊 Mainkan nada alarm
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, soundUri)
        ringtone?.play()

        // 📦 Intent untuk STOP
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
            putExtra("id", reminderId)
            putExtra("isi", isiReminder)
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🔔 Tampilkan notifikasi dengan tombol STOP
        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("⏰ Reminder Panen")
            .setContentText(isiReminder)
            .setStyle(NotificationCompat.BigTextStyle().bigText(isiReminder))
            .setSmallIcon(R.drawable.kp3klogo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.baseline_cancel_24, "STOP", stopPendingIntent)
            .build()

        notifManager.notify(999, notif)

        // 🔁 Jadwalkan ulang alarm 5 menit kemudian jika tidak dihentikan
        if (reminderId.isNotEmpty()) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val repeatIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("id", reminderId)
                putExtra("isi", isiReminder)
            }
            val repeatPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                repeatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000L // 5 menit
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                repeatPendingIntent
            )
        }
    }
}
