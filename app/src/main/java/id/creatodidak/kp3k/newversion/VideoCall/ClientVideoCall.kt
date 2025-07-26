package id.creatodidak.kp3k.newversion.VideoCall

import android.content.Context
import android.content.Intent
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
import id.creatodidak.kp3k.service.MyFirebaseMessagingService
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import org.json.JSONObject

class ClientVideoCall : AppCompatActivity() {
    private var mRtcEngine: RtcEngine? = null
    private val myAppId = BuildConfig.AGORA_ID
    private lateinit var lyClient: ConstraintLayout
    private lateinit var lyClientEnded: LinearLayout
    private lateinit var videoClient: FrameLayout
    private lateinit var videoHost: FrameLayout
    private lateinit var tvHostName: TextView
    private lateinit var ivChangeCameraClient: ImageView
    private lateinit var ivEndCallClient: ImageView
    private lateinit var tvPingClient: TextView
    private lateinit var tvEndedStatusClient: TextView

    private lateinit var room: String
    private lateinit var token: String
    private lateinit var caller: String
    private lateinit var callernrp: String
    val socket = SocketManager.getSocket()

    private var isEnded: Boolean = false
    private var isCalling: Boolean = true
    private var isDoEndCall: Boolean = false
    private val pingHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_client_video_call)

        MyFirebaseMessagingService.staticNotify.cancelIncomingCallNotification(this)
        MyFirebaseMessagingService.isCallAnswered = true
        MyFirebaseMessagingService.CallSoundManager.stopSound()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        room = intent.getStringExtra("room")!!
        token = intent.getStringExtra("token")!!
        caller = intent.getStringExtra("caller")!!
        callernrp = intent.getStringExtra("callernrp")!!

        lyClient = findViewById(R.id.lyClient)
        lyClientEnded = findViewById(R.id.lyClientEnded)
        videoClient = findViewById(R.id.videoClient)
        videoHost = findViewById(R.id.videoHost)
        tvHostName = findViewById(R.id.tvHostName)
        ivChangeCameraClient = findViewById(R.id.ivChangeCameraClient)
        ivEndCallClient = findViewById(R.id.ivEndCallClient)
        tvPingClient = findViewById(R.id.tvPingClient)
        tvEndedStatusClient = findViewById(R.id.tvEndedStatusClient)

        tvHostName.text = caller

        lyClientEnded.visibility = View.GONE
        lyClient.visibility = View.VISIBLE
        ivChangeCameraClient.setOnClickListener {
            mRtcEngine?.switchCamera()
        }

        val fromNotify = intent.getStringExtra("from_notify")
        if (fromNotify == "yes") {
            Log.d("tes_log", "send-- to socket")

            val callernrp = intent.getStringExtra("callernrp") ?: return

            val data = JSONObject().apply {
                put("from", getMyNrp(this@ClientVideoCall))
                put("from", callernrp)
            }
            SocketManager.getSocket().emit("accept-call", data)
        }


        startPingLoop()
        initializeVideoCall()

        socket.on("call-ended"){
            runOnUiThread {
                isEnded = true
                isCalling = false
                stopPingLoop()
                lyClient.visibility = View.GONE
                lyClientEnded.visibility = View.VISIBLE
                tvEndedStatusClient.text = if(isDoEndCall)"ANDA MENGAKHIRI PANGGILAN" else "PANGGILAN DIAKHIRI OLEH ${caller}"
                mRtcEngine?.leaveChannel()
                RtcEngine.destroy()
                mRtcEngine = null
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if(isCalling){
                askUser(this@ClientVideoCall, "Konfirmasi", "Apakah anda yakin ingin mengakhiri panggilan?") {
                    val payload = JSONObject().apply {
                        put("from", getMyNrp(this@ClientVideoCall))
                        put("to", callernrp)
                    }

                    socket.emit("end-call", payload)
                    mRtcEngine?.leaveChannel()
                    RtcEngine.destroy()
                    mRtcEngine = null
                    isDoEndCall = true
                    isCalling = false
                    isEnded = true

                    finish()
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
        ivEndCallClient.setOnClickListener {
            askUser(this@ClientVideoCall, "Konfirmasi", "Apakah anda yakin ingin mengakhiri panggilan?") {
                val payload = JSONObject().apply {
                    put("from", getMyNrp(this@ClientVideoCall))
                    put("to", callernrp)
                }

                isDoEndCall = true
                isCalling = false
                isEnded = true
                socket.emit("end-call", payload)
                lyClient.visibility = View.GONE
                lyClientEnded.visibility = View.VISIBLE
                tvEndedStatusClient.text = "ANDA MENGAKHIRI PANGGILAN"
                mRtcEngine?.leaveChannel()
                RtcEngine.destroy()
                mRtcEngine = null
            }
        }
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
        videoClient.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = SurfaceView(applicationContext).apply {
            setZOrderMediaOverlay(false)
            videoHost.addView(this)
        }
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid))
    }

    private val pingRunnable = object : Runnable {
        override fun run() {
            if (!isEnded) {
                val startTime = System.currentTimeMillis()

                socket.emit("PING")
                socket.once("PONG") {
                    val delay = System.currentTimeMillis() - startTime
                    runOnUiThread {
                        tvPingClient.text = "$delay ms"
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // very important: update the intent attached to the activity

        val fromNotify = intent?.getStringExtra("from_notify")
        if (fromNotify == "yes") {
            Log.d("tes_log", "send-- to socket")

            val callernrp = intent.getStringExtra("callernrp") ?: return

            val data = JSONObject().apply {
                put("from", getMyNrp(this@ClientVideoCall))
                put("from", callernrp)
            }
            SocketManager.getSocket().emit("accept-call", data)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopPingLoop()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }
}