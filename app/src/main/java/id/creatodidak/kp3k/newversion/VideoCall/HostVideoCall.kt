package id.creatodidak.kp3k.newversion.VideoCall

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.network.SocketManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import org.json.JSONObject

class HostVideoCall : AppCompatActivity() {
    private var mRtcEngine: RtcEngine? = null
    private val myAppId = BuildConfig.AGORA_ID

    private lateinit var lyHostWaiting: LinearLayout
    private lateinit var ivNamaHostWaiting: TextView
    private lateinit var ivEndCallHostWaiting: ImageView
    private lateinit var lyHostEnded: LinearLayout
    private lateinit var tvPingHost: TextView
    private lateinit var ivChangeCameraHost: ImageView
    private lateinit var ivMicHost: ImageView
    private lateinit var ivEndCallHost: ImageView
    private lateinit var videoHost: FrameLayout
    private lateinit var videoClient: FrameLayout
    private lateinit var lyHost: ConstraintLayout
    private lateinit var tvClientName: TextView
    private lateinit var tvMutedHost: TextView
    private lateinit var tvEndedStatusHost: TextView

    private lateinit var room: String
    private lateinit var token: String
    private lateinit var receiver: String
    private lateinit var receivernrp: String
    val socket = SocketManager.getSocket()

    private var isWaiting: Boolean = true
    private var isEnded: Boolean = false
    private var isCalling: Boolean = false
    private var isMuted: Boolean = false
    private var isDoEndCall: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private val pingHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_host_video_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
        window.statusBarColor = getColor(R.color.default_bg)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        room = intent.getStringExtra("room")!!
        token = intent.getStringExtra("token")!!
        receiver = intent.getStringExtra("receiver")!!
        receivernrp = intent.getStringExtra("receivernrp")!!

        lyHostWaiting = findViewById(R.id.lyHostWaiting)
        ivNamaHostWaiting = findViewById(R.id.ivNamaHostWaiting)
        ivEndCallHostWaiting = findViewById(R.id.ivEndCallHostWaiting)
        lyHostEnded = findViewById(R.id.lyHostEnded)
        tvPingHost = findViewById(R.id.tvPingHost)
        ivChangeCameraHost = findViewById(R.id.ivChangeCameraHost)
        ivMicHost = findViewById(R.id.ivMicHost)
        ivEndCallHost = findViewById(R.id.ivEndCallHost)
        videoHost = findViewById(R.id.videoHost)
        videoClient = findViewById(R.id.videoClient)
        lyHost = findViewById(R.id.lyHost)
        tvClientName = findViewById(R.id.tvClientName)
        tvMutedHost = findViewById(R.id.tvMutedHost)
        tvMutedHost.visibility = View.GONE
        tvEndedStatusHost = findViewById(R.id.tvEndedStatusHost)

        lyHostEnded.visibility = View.GONE
        lyHost.visibility = View.GONE
        lyHostWaiting.visibility = View.VISIBLE

        playWaitingSound()

        ivNamaHostWaiting.text = receiver
        tvClientName.text = receiver

        ivEndCallHostWaiting.setOnClickListener {
            askUser(this, "Konfirmasi", "Apakah anda yakin ingin mengakhiri panggilan?") {
                val payload = JSONObject().apply {
                    put("from", getMyNrp(this@HostVideoCall))
                    put("to", receivernrp)
                }

                socket.emit("end-call", payload)
                isWaiting = false
                isCalling = false
                isEnded = true
                isDoEndCall = true
                stopWaitingSound()
                lyHostWaiting.visibility = View.GONE
                lyHostEnded.visibility = View.VISIBLE
                tvEndedStatusHost.text = "ANDA MENGAKHIRI PANGGILAN"
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            }
        }

        ivEndCallHost.setOnClickListener {
            askUser(this@HostVideoCall, "Konfirmasi", "Apakah anda yakin ingin mengakhiri panggilan?") {
                val payload = JSONObject().apply {
                    put("from", getMyNrp(this@HostVideoCall))
                    put("to", receivernrp)
                }
                socket.emit("end-call", payload)

                isCalling = false
                isWaiting = false
                isEnded = true
                isDoEndCall = true
                lyHost.visibility = View.GONE
                lyHostEnded.visibility = View.VISIBLE
                tvEndedStatusHost.text = "ANDA MENGAKHIRI PANGGILAN"
                mRtcEngine?.leaveChannel()
                RtcEngine.destroy()
                mRtcEngine = null
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if(isWaiting || isCalling){
                askUser(this@HostVideoCall, "Konfirmasi", "Apakah anda yakin ingin mengakhiri panggilan?") {
                    val payload = JSONObject().apply {
                        put("from", getMyNrp(this@HostVideoCall))
                        put("to", receivernrp)
                    }

                    socket.emit("end-call", payload)
                    isDoEndCall = true

                    stopWaitingSound()
                    finish()
                    if(isCalling){
                        isCalling = false
                        isEnded = true
                        mRtcEngine?.leaveChannel()
                        RtcEngine.destroy()
                        mRtcEngine = null
                    }
                    window.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    )
                }
            }else{
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
                finish()
            }
        }

        socket.on("call-timeout") {
            Log.d("DATA_SOCKET", "CALL TIMEOUT")
            runOnUiThread {
                if (isWaiting) {
                    isWaiting = false
                    isEnded = true
                    stopWaitingSound()
                    lyHostWaiting.visibility = View.GONE
                    lyHostEnded.visibility = View.VISIBLE
                    tvEndedStatusHost.text = "PANGGILAN BERAKHIR"
                }
            }
        }

        socket.on("call-rejected") {
            Log.d("DATA_SOCKET", "CALL REJECTED")
            runOnUiThread {
                if (isWaiting) {
                    isWaiting = false
                    isEnded = true
                    stopWaitingSound()
                    lyHostWaiting.visibility = View.GONE
                    lyHostEnded.visibility = View.VISIBLE
                    tvEndedStatusHost.text = "PANGGILAN DITOLAK"
                }
            }
        }

        socket.on("call-accepted") {
            Log.d("DATA_SOCKET", "CALL ACCEPTED")
            runOnUiThread {
                isWaiting = false
                isCalling = true
                stopWaitingSound()

                lyHostWaiting.visibility = View.GONE
                lyHostEnded.visibility = View.GONE
                lyHost.visibility = View.VISIBLE

                initializeVideoCall()
            }
        }

        socket.on("call-ended"){
            runOnUiThread {
                isEnded = true
                isWaiting = false
                isCalling = false
                stopPingLoop()
                lyHost.visibility = View.GONE
                lyHostEnded.visibility = View.VISIBLE
                tvEndedStatusHost.text = if(isDoEndCall)"ANDA MENGAKHIRI PANGGILAN" else "PANGGILAN DIAKHIRI OLEH ${receiver}"
                mRtcEngine?.leaveChannel()
                RtcEngine.destroy()
                mRtcEngine = null
            }
        }

        ivChangeCameraHost.setOnClickListener {
            mRtcEngine?.switchCamera()
        }

        ivMicHost.setOnClickListener {
            isMuted = !isMuted
            mRtcEngine?.muteLocalAudioStream(isMuted)
            ivMicHost.setImageResource(
                if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
            )
            tvMutedHost.visibility = if (isMuted) View.VISIBLE else View.GONE
        }

        startPingLoop()
    }

    private fun initializeVideoCall() {
        if (mRtcEngine == null) {
            setupAgora()
        }
        setupLocalVideo()
        joinChannel()
    }

    private fun setupAgora() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = applicationContext
                mAppId = myAppId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        runOnUiThread { setupRemoteVideo(uid) }
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        runOnUiThread { videoClient.removeAllViews() }
                    }
                }
            }
            mRtcEngine = RtcEngine.create(config)

            mRtcEngine?.enableVideo()
            mRtcEngine?.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION)
            mRtcEngine?.setEnableSpeakerphone(true)
            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = true
        }

        mRtcEngine?.joinChannel(token, room, 0, options)

        // Delay 1 detik agar RtcEngine benar-benar siap sebelum mengatur routing audio
        Handler(Looper.getMainLooper()).postDelayed({
            // Set agar audio keluar dari speaker
            mRtcEngine?.setEnableSpeakerphone(true)
            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true

            Log.d("AudioRoute", "AudioManager.mode = ${audioManager.mode}")
            Log.d("AudioRoute", "Speakerphone ON = ${audioManager.isSpeakerphoneOn}")
        }, 1000)
    }


    private fun setupLocalVideo() {
        val surfaceView = SurfaceView(baseContext).apply {
            setZOrderMediaOverlay(true)
        }
        videoHost.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = SurfaceView(applicationContext).apply {
            setZOrderMediaOverlay(false)
            videoClient.addView(this)
        }
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid))
    }


    private fun playWaitingSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.waiting)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private val pingRunnable = object : Runnable {
        override fun run() {
            if (!isEnded) {
                val startTime = System.currentTimeMillis()

                socket.emit("PING")
                socket.once("PONG") {
                    val delay = System.currentTimeMillis() - startTime
                    runOnUiThread {
                        tvPingHost.text = "$delay ms"
                    }
                }

                // Lanjut ping lagi setelah 5 detik
                pingHandler.postDelayed(this, 5000)
            } else {
                // Stop ping loop
                pingHandler.removeCallbacks(this)
            }
        }
    }

    private fun startPingLoop() {
        pingHandler.post(pingRunnable)
    }

    private fun stopPingLoop() {
        pingHandler.removeCallbacks(pingRunnable)
    }
    private fun stopWaitingSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPingLoop()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }
}