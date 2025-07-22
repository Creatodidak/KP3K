package id.creatodidak.kp3k.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.creatodidak.kp3k.newversion.VideoCall.IncomingCallWaiting

class NotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NOTIF_CLICK", "Notifikasi diklik, membuka IncomingCallWaiting")

        val activityIntent = Intent(context, IncomingCallWaiting::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }

        context?.startActivity(activityIntent)
    }
}
