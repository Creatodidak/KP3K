package id.creatodidak.kp3k.helper

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import id.creatodidak.kp3k.BuildConfig.AI_URL
import id.creatodidak.kp3k.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.TimeUnit

object LoadAI {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        request: String,
        onDone: (analisa: String, status: Boolean) -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.ly_ai, null)
        val pbAI = view.findViewById<View>(R.id.pbAI)
        val hasilAnalisa = view.findViewById<TextView>(R.id.hasilAnalisa)
        val btSelesai = view.findViewById<Button>(R.id.btSelesai)
        btSelesai.visibility = View.GONE

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val messageObj = JSONObject()
            .put("role", "user")
            .put("content", request.trim())

        val messagesArray = JSONArray().put(messageObj)

        val requestBody = JSONObject()
            .put("messages", messagesArray)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${AI_URL}chat")
            .post(requestBody)
            .build()

        var isDone = false

        Log.d("TANYA_AI", "📤 Mengirim permintaan ke AI: $request")

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TANYA_AI", "❌ Request gagal: ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    hasilAnalisa.text = "❌ Gagal memuat data dari AI!\n${e.message}"
                    pbAI.visibility = View.GONE
                    btSelesai.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("TANYA_AI", "✅ Respon awal diterima: ${response.code}")
                Log.v("TANYA_AI", "📥 Body: $body")

                if (body.isNullOrEmpty()) {
                    Log.e("TANYA_AI", "❌ Response body null atau kosong!")
                    Handler(Looper.getMainLooper()).post {
                        hasilAnalisa.text = "❌ Respon kosong dari server!"
                        pbAI.visibility = View.GONE
                        btSelesai.visibility = View.VISIBLE
                        isDone = false
                    }
                    return
                }

                val json = try {
                    JSONObject(body)
                } catch (e: Exception) {
                    Log.e("TANYA_AI", "❌ Gagal parsing JSON: ${e.message}")
                    null
                }

                val jobId = json?.optString("jobId") ?: ""
                Log.d("TANYA_AI", "🔁 Dapat jobId: $jobId")

                if (jobId.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val url = "${AI_URL.trimEnd('/')}/stream/$jobId"
                        Log.d("TANYA_AI", "🔌 Koneksi SSE ke: $url")

                        val sseRequest = Request.Builder().url(url).build()
                        val factory = EventSources.createFactory(client)

                        factory.newEventSource(sseRequest, object : EventSourceListener() {
                            override fun onOpen(eventSource: EventSource, response: Response) {
                                Log.d("SSE", "✅ SSE terbuka dengan status ${response.code}")
                            }

                            override fun onEvent(
                                eventSource: EventSource,
                                id: String?,
                                type: String?,
                                data: String
                            ) {
                                Log.d("SSE_RAW", "📥 Data chunk: ${data.replace("\n", "\\n")}")

                                if (data == "[SELESAI]") {
                                    Log.d("SSE", "✅ SSE selesai diterima")
                                    Handler(Looper.getMainLooper()).post {
                                        pbAI.visibility = View.GONE
                                        btSelesai.visibility = View.VISIBLE
                                        isDone = true
                                    }
                                    eventSource.cancel()
                                    return
                                }

                                val patched = data.replace(Regex("""(?<=\w)\.\s*"""), ".\n")
                                Handler(Looper.getMainLooper()).post {
                                    hasilAnalisa.append(patched)
                                }
                            }

                            override fun onClosed(eventSource: EventSource) {
                                Log.d("SSE", "🛑 SSE ditutup client")
                            }

                            override fun onFailure(
                                eventSource: EventSource,
                                t: Throwable?,
                                response: Response?
                            ) {
                                if (t is SocketException && t.message?.contains("Socket closed") == true) {
                                    Log.i("SSE", "ℹ️ SSE ditutup secara normal (Socket closed)")
                                } else {
                                    Log.e("SSE", "❌ SSE gagal (Ask Gemini)", t)
                                    Log.e("SSE", "❌ Response: ${response?.code} ${response?.message}")
                                    Handler(Looper.getMainLooper()).post {
                                        hasilAnalisa.text = "❌ Terjadi kesalahan saat streaming data!"
                                    }
                                    isDone = false
                                }

                                Handler(Looper.getMainLooper()).post {
                                    pbAI.visibility = View.GONE
                                    btSelesai.visibility = View.VISIBLE
                                }

                                eventSource.cancel()
                            }
                        })
                    }, 1000)
                } else {
                    Log.e("TANYA_AI", "❌ Job ID kosong atau tidak valid")
                    Handler(Looper.getMainLooper()).post {
                        hasilAnalisa.text = "❌ Gagal memproses permintaan (jobId tidak valid)."
                        pbAI.visibility = View.GONE
                        btSelesai.visibility = View.VISIBLE
                        isDone = false
                    }
                }
            }
        })

        btSelesai.setOnClickListener {
            onDone(hasilAnalisa.text.toString(), isDone)
            hide()
        }

        builder.setCancelable(false)
        currentDialog = builder.create()
        currentDialog?.setView(view, 50, 0, 50, 0)
        currentDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        currentDialog?.show()
    }

    fun hide() {
        currentDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        currentDialog = null
    }
}
