package id.creatodidak.kp3k.service

import android.content.Context
import android.util.Log
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.network.SocketManager
import io.socket.client.Socket
import org.json.JSONObject

class CallListenerNetwork(private val callback: IncomingCallCallback) {

    private val socket = SocketManager.getSocket()

    fun callListener(ctx: Context){

        socket.on(Socket.EVENT_CONNECT) {
            val nrp = getMyNrp(ctx)

            socket.emit("register", JSONObject().apply {
                put("nrp", nrp)
            })
            Log.i("tes_log", "socket ✅ Connected")
        }


        socket.emit("check-waiting", JSONObject().apply {
            put("nrp", getMyNrp(ctx))
        })

        socket.on("incoming-call") { args ->
            val obj = args[0] as JSONObject

            //sesuaikan user jika perlu tambahkan filter disini agar tidak semua event "incoming-call" masuk
            callback.onIncomingCall(obj) //send callback
        };

        socket.on("end-call") { args ->
            Log.d("tes_log", "socket end call")

        }

    }

}

interface IncomingCallCallback {
    fun onIncomingCall(data: JSONObject)
}
