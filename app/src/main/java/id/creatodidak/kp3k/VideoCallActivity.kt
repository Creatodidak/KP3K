package id.creatodidak.kp3k

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class VideoCallActivity : AppCompatActivity() {
    private lateinit var rtcEngine: RtcEngine
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private var isMicMuted = false

    // Channel config
    private val channelName = "testChannel"
    private val appId = "f5a427c2bfd44f3e8285507e4c1ee34f"
    private val PERMISSION_REQ_ID = 22
    private val requestPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, requestPermissions, PERMISSION_REQ_ID)
        } else {
            initializeAgoraEngine()
            setupVideoConfig()
            joinChannel()
        }

        // Tombol End Call
        findViewById<FrameLayout>(R.id.btnEndCall).setOnClickListener {
            leaveChannel()
            finish()
        }

        // Tombol Mute Mic
        setupMuteButton()
    }

    private fun setupMuteButton() {
        val muteButton = findViewById<FrameLayout>(R.id.btnMuteMic)
        val micIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_mic)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(12, 12, 12, 12)
            }
        }

        muteButton.addView(micIcon)
        muteButton.setOnClickListener { toggleMicrophone() }
    }

    private fun toggleMicrophone() {
        isMicMuted = !isMicMuted

        // Update UI
        findViewById<ImageView>(R.id.ivMicIcon).apply {
            setImageResource(
                if (isMicMuted) R.drawable.ic_mic_off
                else R.drawable.ic_mic
            )
            alpha = if (isMicMuted) 0.5f else 1.0f
        }

        // Update Agora
        rtcEngine.muteLocalAudioStream(isMicMuted)

        // Feedback visual
        findViewById<FrameLayout>(R.id.btnMuteMic).isSelected = isMicMuted
        Toast.makeText(
            this,
            if (isMicMuted) "Mic dimatikan" else "Mic diaktifkan",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkPermissions(): Boolean {
        return requestPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeAgoraEngine() {
        try {
            rtcEngine = RtcEngine.create(baseContext, appId, object : IRtcEngineEventHandler() {
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    runOnUiThread { setupRemoteVideo(uid) }
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    runOnUiThread { removeRemoteVideo() }
                }

                override fun onError(err: Int) {
                    runOnUiThread {
                        Toast.makeText(
                            this@VideoCallActivity,
                            "Error: $err", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun setupVideoConfig() {
        rtcEngine.enableVideo()
        rtcEngine.setVideoEncoderConfiguration(VideoEncoderConfiguration().apply {
            dimensions = VideoEncoderConfiguration.VD_640x360
            frameRate = 15
            orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        })
    }

    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(this).apply {
            holder.setFormat(PixelFormat.TRANSLUCENT)
            setZOrderOnTop(true)
        }

        rtcEngine.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )

        findViewById<FrameLayout>(R.id.localVideoContainer).apply {
            addView(localSurfaceView)
            visibility = View.VISIBLE
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfaceView = SurfaceView(this).apply {
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        rtcEngine.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )

        findViewById<FrameLayout>(R.id.remoteVideoContainer).apply {
            addView(remoteSurfaceView)
            visibility = View.VISIBLE
        }
    }

    private fun removeRemoteVideo() {
        findViewById<FrameLayout>(R.id.remoteVideoContainer).apply {
            removeAllViews()
            visibility = View.GONE
        }
        remoteSurfaceView = null
    }

    private fun joinChannel() {
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        setupLocalVideo()
        rtcEngine.startPreview()
        rtcEngine.joinChannel(null, channelName, null, 0)
    }

    private fun leaveChannel() {
        rtcEngine.leaveChannel()
        rtcEngine.stopPreview()
        removeRemoteVideo()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initializeAgoraEngine()
            setupVideoConfig()
            joinChannel()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        localSurfaceView?.holder?.surface?.release()
        remoteSurfaceView?.holder?.surface?.release()
    }

    override fun onResume() {
        super.onResume()
        rtcEngine.startPreview()
        // Sync mute state
        rtcEngine.muteLocalAudioStream(isMicMuted)
    }

    override fun onPause() {
        super.onPause()
        rtcEngine.stopPreview()
    }
}