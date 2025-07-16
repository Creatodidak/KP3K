package id.creatodidak.kp3k.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import org.json.JSONArray
import androidx.core.content.edit
import id.creatodidak.kp3k.service.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Locale

fun saveReminderList(context: Context, list: JSONArray) {
    val prefs = context.getSharedPreferences("REMINDER_PANEN", Context.MODE_PRIVATE)
    prefs.edit { putString("REMINDER_LIST", list.toString()) }
}

fun getReminderList(context: Context): JSONArray {
    val prefs = context.getSharedPreferences("REMINDER_PANEN", Context.MODE_PRIVATE)
    val json = prefs.getString("REMINDER_LIST", "[]")
    return JSONArray(json)
}

fun isReminderExists(context: Context, id: String): Boolean {
    val list = getReminderList(context)
    for (i in 0 until list.length()) {
        val reminder = list.getJSONObject(i)
        if (reminder.getString("id") == id && reminder.getBoolean("alarmAktif")) {
            return true
        }
    }
    return false
}

@SuppressLint("ScheduleExactAlarm")
@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun setAlarmReminder(context: Context, id: String, tanggal: String, jam: String, isi: String) {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    val waktu = sdf.parse("$tanggal $jam") ?: return

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("isi", isi)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        id.hashCode(), // unique requestCode
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        waktu.time,
        pendingIntent
    )
}