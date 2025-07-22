package id.creatodidak.kp3k.network

import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {
    private var socket: Socket? = null

    fun connect() {
        if (socket == null) {
            try {
                val opts = IO.Options().apply {
                    reconnection = true
                    forceNew = false // ⚠️ penting untuk hindari reconnect tiap activity
                }
                socket = IO.socket("http://36.93.138.110:3042", opts)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }

        if (socket?.connected() != true) {
            socket?.connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null // bersihkan agar tidak reuse saat tidak dibutuhkan
    }

    fun getSocket(): Socket {
        if (socket == null) {
            connect()
        }
        return socket!!
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
}
