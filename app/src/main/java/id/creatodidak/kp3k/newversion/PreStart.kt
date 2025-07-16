package id.creatodidak.kp3k.newversion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.newversion.auth.NewSetPin
import kotlinx.coroutines.launch
import kotlin.collections.contains

class PreStart : AppCompatActivity() {
    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private var role: String? = null

    private lateinit var permissionButtons: Map<String, TextView>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            updateButtonStatus(perm, granted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pre_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        permissionButtons = mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to findViewById(R.id.allow_ACCESS_FINE_LOCATION),
            Manifest.permission.ACCESS_COARSE_LOCATION to findViewById(R.id.allow_ACCESS_COARSE_LOCATION),
            Manifest.permission.CAMERA to findViewById(R.id.allow_CAMERA),
            Manifest.permission.RECORD_AUDIO to findViewById(R.id.allow_RECORD_AUDIO),
            Manifest.permission.READ_PHONE_STATE to findViewById(R.id.allow_READ_PHONE_STATE),
            Manifest.permission.POST_NOTIFICATIONS to findViewById(R.id.allow_POST_NOTIFICATIONS),
            Manifest.permission.READ_MEDIA_IMAGES to findViewById(R.id.allow_READ_MEDIA_IMAGES),
        )

        // Atur click listener tiap izin
        permissionButtons.forEach { (perm, button) ->
            button.setOnClickListener {
                requestPermissionLauncher.launch(arrayOf(perm))
            }

            val granted = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
            updateButtonStatus(perm, granted)
        }

        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val fcmtoken = sh.getString("fcmtoken", null)
        role = sh.getString("role", null)

        if (areAllPermissionsGranted()) {
            if (fcmtoken != null) {
                startActivity(Intent(this@PreStart, NewSetPin::class.java))
                finish()
            }else{
                loadFcmToken()
            }
        }

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        btnContinue.setOnClickListener {
            if (areAllPermissionsGranted()) {
                loadFcmToken()
            } else {
                Toast.makeText(this, "Harap berikan semua izin terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonStatus(permission: String, granted: Boolean) {
        permissionButtons[permission]?.apply {
            text = if (granted) "IZIN DIBERIKAN" else "BERI IZIN AKSES"
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (granted) R.color.white else android.R.color.holo_red_dark
                )
            )
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (granted) R.color.black else R.color.white
                )
            )
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun loadFcmToken() {
        Loading.show(this@PreStart)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                Loading.hide()
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    val prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE).edit()
                    prefs.putString("fcmtoken", fcmToken).apply()
                    startActivity(Intent(this@PreStart, NewSetPin::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@PreStart, NewSetPin::class.java))
                    finish()
                }
            }
    }


}
