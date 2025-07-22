package id.creatodidak.kp3k.pimpinan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.pimpinan.EndCallPimpinan
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.VideoCallActivity
import id.creatodidak.kp3k.service.MyFirebaseMessagingService.CallSoundManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class PimpinanVideoCall : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQ_ID = 22
    }

    private val myAppId = BuildConfig.AGORA_ID
    private lateinit var channelName : String
    private lateinit var token : String
    private lateinit var nama : String
    private lateinit var pangkat : String
    private var mRtcEngine: RtcEngine? = null
    private var waitingSound: MediaPlayer? = null
    private var callTimeoutHandler: Handler? = null
    private var callTimeoutRunnable: Runnable? = null

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            Log.d("AgoraDebug", "Join success: $channel, uid: $uid")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                stopWaitingSound()
                setupRemoteVideo(uid)
            }
        }


        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                stopWaitingSound()
                startActivity(Intent(this@PimpinanVideoCall, EndCallPimpinan::class.java))
                finish()
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            }
        }

        override fun onError(err: Int) {
            super.onError(err)
            runOnUiThread {
                stopWaitingSound()
                startActivity(Intent(this@PimpinanVideoCall, EndCallPimpinan::class.java))
                finish()
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pimpinan_video_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )


        token = intent.getStringExtra("token").toString()
        channelName = intent.getStringExtra("channel").toString()
        nama = intent.getStringExtra("nama").toString()
        pangkat = intent.getStringExtra("pangkat").toString()
        val tvNoHpPimpinan = findViewById<TextView>(R.id.tvNoHpPimpinan)
        tvNoHpPimpinan.text = "$nama\n$pangkat"
        CallSoundManager.stopSound()
        if (checkPermissions()) {
//            showToast("Permission Granted")
            startVideoCalling()
        } else {
//            showToast("Permission Denied")
            requestPermissions()
        }

        val btnEndCall = findViewById<FloatingActionButton>(R.id.btnEndCallPimpinan)
        val brtSwitchCamera = findViewById<FloatingActionButton>(R.id.btnSwitchCameraPimpinan)

        btnEndCall.setOnClickListener {
            mRtcEngine?.leaveChannel()
            val i = Intent(this@PimpinanVideoCall, EndCallPimpinan::class.java)
            startActivity(i)
            finish()
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        brtSwitchCamera.setOnClickListener {
            mRtcEngine?.switchCamera()
        }
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, getRequiredPermissions(),
            PimpinanVideoCall.Companion.PERMISSION_REQ_ID
        )
    }

    private fun stopWaitingSound() {
        try {
            waitingSound?.stop()
            waitingSound?.release()
            waitingSound = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        callTimeoutHandler?.removeCallbacks(callTimeoutRunnable!!)
    }


    private fun checkPermissions(): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PimpinanVideoCall.Companion.PERMISSION_REQ_ID && checkPermissions()) {
            startVideoCalling()
        }
    }

    private fun startVideoCalling() {
        initializeAgoraVideoSDK()
        enableVideo()
        setupLocalVideo()
        joinChannel()

        // Play waiting sound
        waitingSound = MediaPlayer.create(this, R.raw.waiting)
        waitingSound?.isLooping = true
        waitingSound?.start()

        // Set 60s timeout if no user joins
        callTimeoutHandler = Handler(Looper.getMainLooper())
        callTimeoutRunnable = Runnable {
            stopWaitingSound()
            startActivity(Intent(this, EndCallPimpinan::class.java))
            finish()
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        callTimeoutHandler?.postDelayed(callTimeoutRunnable!!, 60_000)
    }

    private fun initializeAgoraVideoSDK() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = applicationContext
                mAppId = myAppId
                mEventHandler = mRtcEventHandler
            }
            Log.d("AgoraDebug", "Creating RtcEngine with AppID: $myAppId")
            mRtcEngine = RtcEngine.create(config)
            Log.d("AgoraDebug", "RtcEngine created successfully")
        } catch (e: Exception) {
            Log.e("AgoraDebug", "RtcEngine creation failed: ${e.message}")
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }
    }


    private fun enableVideo() {
        mRtcEngine?.apply {
            enableVideo()
            startPreview()
        }
    }



    private fun joinChannel() {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = true
        }
        mRtcEngine?.let {
            Log.d("AgoraDebug", "Attempting to join channel: $channelName with token: $token")
            it.joinChannel(token, channelName, 0, options)
        } ?: run {
            Log.e("AgoraDebug", "RtcEngine is null")
        }
    }


    private fun setupRemoteVideo(uid: Int) {
        val container: FrameLayout = findViewById(R.id.VideoRemote)
        val surfaceView = SurfaceView(applicationContext).apply {
            setZOrderMediaOverlay(false)
            container.addView(this)
        }
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun setupLocalVideo() {
        val container: FrameLayout = findViewById(R.id.videoLocal)
        val surfaceView = SurfaceView(baseContext).apply {
            setZOrderMediaOverlay(true)
        }
        container.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
//        showToast("Local video setup completed")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWaitingSound()
        cleanupAgoraEngine()
    }


    private fun cleanupAgoraEngine() {
        mRtcEngine?.apply {
            stopPreview()
            leaveChannel()
//            showToast("Left channel and stopped preview")
        }
        mRtcEngine = null
    }
}