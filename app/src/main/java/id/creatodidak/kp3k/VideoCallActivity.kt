package id.creatodidak.kp3k

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.dashboard.DashboardOpsional
import id.creatodidak.kp3k.service.MyFirebaseMessagingService.CallSoundManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQ_ID = 22
    }

    private val myAppId = BuildConfig.AGORA_ID
    private lateinit var channelName : String
    private lateinit var token : String
    private var mRtcEngine: RtcEngine? = null

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            Log.d("AgoraDebug", "Join success: $channel, uid: $uid")
//            showToast("Joined channel $channel")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                setupRemoteVideo(uid)
//                showToast("User joined: $uid")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                val i = Intent(this@VideoCallActivity, EndCallActivity::class.java)
                startActivity(i)
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
            val errorMessage = "Agora Error code: $err"
            Log.e("AgoraError", errorMessage)
            val i = Intent(this@VideoCallActivity, EndCallActivity::class.java)
            startActivity(i)
            finish()
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        token = intent.getStringExtra("token").toString()
        val caller = intent.getStringExtra("caller")
        val tvCaller = findViewById<TextView>(R.id.tvNoHp)
        tvCaller.text = caller
        val sh = getSharedPreferences("session", MODE_PRIVATE)
        val role = sh.getString("role", "")

        channelName = if(role.equals("BPKP")){
            sh.getString("nohp", "").toString()
        }else{
            sh.getString("username", "").toString()
        }
        CallSoundManager.stopSound()
        if (checkPermissions()) {
//            showToast("Permission Granted")
            startVideoCalling()
        } else {
//            showToast("Permission Denied")
            requestPermissions()
        }

        val btnEndCall = findViewById<FloatingActionButton>(R.id.btnEndCall)
        val brtSwitchCamera = findViewById<FloatingActionButton>(R.id.btnSwitchCamera)

        btnEndCall.setOnClickListener {
            mRtcEngine?.leaveChannel()
            val i = Intent(this@VideoCallActivity, EndCallActivity::class.java)
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
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID)
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
        if (requestCode == PERMISSION_REQ_ID && checkPermissions()) {
//            showToast("Permission Granted")
            startVideoCalling()
        } else {
//            showToast("Permission Denied")
        }
    }

    private fun startVideoCalling() {
//        showToast("Starting video call...")
        initializeAgoraVideoSDK()
        enableVideo()
        setupLocalVideo()
        joinChannel()
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
//            showToast("Agora SDK initialized")
        } catch (e: Exception) {
            Log.e("AgoraDebug", "RtcEngine creation failed: ${e.message}")
//            showToast("Error initializing RTC engine: ${e.message}")
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }
    }


    private fun enableVideo() {
        mRtcEngine?.apply {
            enableVideo()
            startPreview()
//            showToast("Video enabled and preview started")
        }
    }

    private fun setupLocalVideo() {
        val container: FrameLayout = findViewById(R.id.local_video_view_container)
        val surfaceView = SurfaceView(baseContext)
        container.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
//        showToast("Local video setup completed")
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
//            showToast("Joining channel...")
        } ?: run {
//            showToast("RtcEngine is null")
            Log.e("AgoraDebug", "RtcEngine is null")
        }
    }


    private fun setupRemoteVideo(uid: Int) {
        val container: FrameLayout = findViewById(R.id.remote_video_view_container)
        val surfaceView = SurfaceView(applicationContext).apply {
            setZOrderMediaOverlay(true)
            container.addView(this)
        }
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
//        showToast("Remote video setup completed for user: $uid")
    }

    override fun onDestroy() {
        super.onDestroy()
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
//
//    private fun showToast(message: String) {
//        runOnUiThread {
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//        }
//    }
}
