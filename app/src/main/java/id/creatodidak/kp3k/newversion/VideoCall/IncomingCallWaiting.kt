package id.creatodidak.kp3k.newversion.VideoCall

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.service.MyFirebaseMessagingService
import io.socket.client.Socket
import org.json.JSONObject
import java.util.Locale

class IncomingCallWaiting : AppCompatActivity() {
    private lateinit var ivAcceptIncoming: ImageView
    private lateinit var ivDeclineIncoming: ImageView
    private lateinit var tvCallerNameIncoming: TextView
    private lateinit var tvPreambuleIncoming: TextView

    private lateinit var room: String
    private lateinit var token: String
    private lateinit var caller: String
    private lateinit var callernrp: String

    private val socket = SocketManager.getSocket()
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var instance: IncomingCallWaiting? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()
        setContentView(R.layout.activity_incoming_call_waiting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        NotificationManagerCompat.from(this).cancel(880801)
        socket.on(Socket.EVENT_CONNECT) {
            val nrp = getMyNrp(this@IncomingCallWaiting)

            socket.emit("register", JSONObject().apply {
                put("nrp", nrp)
            })
            Log.i("SOCKET", "✅ Connected")
        }

        window.statusBarColor = getColor(R.color.default_bg)
        ivAcceptIncoming = findViewById(R.id.ivAcceptIncoming)
        ivDeclineIncoming = findViewById(R.id.ivDeclineIncoming)
        tvCallerNameIncoming = findViewById(R.id.tvCallerNameIncoming)
        tvPreambuleIncoming = findViewById(R.id.tvPreambuleIncoming)

        socket.emit("check-waiting", JSONObject().apply {
            put("nrp", getMyNrp(this@IncomingCallWaiting))
        })

        socket.on("incoming-call") { args ->
            val obj = args[0] as JSONObject
            Log.d("DATA_SOCKET", obj.toString())
            Log.d("tes_log", "socket incomingcall "+obj.toString())

            val validUntilStr = obj.optString("validUntil", null)
            if (validUntilStr != null) {
                try {
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
                    val validUntil = formatter.parse(validUntilStr)?.time ?: 0L
                    val now = System.currentTimeMillis()

                    if (now > validUntil) {
                        MyFirebaseMessagingService.isCallAnswered = true
                        MyFirebaseMessagingService.CallSoundManager.stopSound()
                        tvPreambuleIncoming.apply {
                            setTextColor(getColor(R.color.error_bg))
                            text = "Panggilan Terlewatkan"
                        }
                        Log.d("SOCKET", "📴 Panggilan sudah expired, tidak diproses.")
                        return@on
                    }

                    val delay = (validUntil - now).coerceAtLeast(0L)
                    handler.postDelayed({
                        if (!MyFirebaseMessagingService.isCallAnswered) {
                            MyFirebaseMessagingService.isCallAnswered = true
                            MyFirebaseMessagingService.CallSoundManager.stopSound()
                            tvPreambuleIncoming.apply {
                                setTextColor(getColor(R.color.error_bg))
                                text = "Panggilan Tidak Terjawab"
                            }
                            finish()
                        }
                    }, delay)

                } catch (e: Exception) {
                    Log.e("SOCKET", "❌ Gagal parsing validUntil: $validUntilStr", e)
                    return@on
                }
            }

            room = obj.getString("room")
            token = obj.getString("token")
            caller = obj.getString("namaCaller")
            callernrp = obj.getString("nrpCaller")
            runOnUiThread {
                tvCallerNameIncoming.text = caller
            }
        }

        ivAcceptIncoming.setOnClickListener {
            MyFirebaseMessagingService.staticNotify.cancelIncomingCallNotification(this)
            MyFirebaseMessagingService.isCallAnswered = true
            MyFirebaseMessagingService.CallSoundManager.stopSound()
            val data = JSONObject().apply {
                put("to", getMyNrp(this@IncomingCallWaiting))
                put("from", callernrp)
            }
            socket.emit("accept-call", data)
            val i = Intent(this, ClientVideoCall::class.java).apply {
                putExtra("room", room)
                putExtra("token", token)
                putExtra("caller", caller)
                putExtra("callernrp", callernrp)
            }
            startActivity(i)
            finish()
        }

        ivDeclineIncoming.setOnClickListener {
            MyFirebaseMessagingService.staticNotify.cancelIncomingCallNotification(this)
            MyFirebaseMessagingService.isCallAnswered = true
            MyFirebaseMessagingService.CallSoundManager.stopSound()
            val data = JSONObject().apply {
                put("to", getMyNrp(this@IncomingCallWaiting))
                put("from", callernrp)
                put("reason", "tidak dapat menerima panggilan")
            }
            socket.emit("reject-call", data)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        handler.removeCallbacksAndMessages(null)
    }

}
