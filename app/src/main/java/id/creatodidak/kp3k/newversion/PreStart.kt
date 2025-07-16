package id.creatodidak.kp3k.newversion

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.newversion.auth.NewSetPin

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
            Manifest.permission.SCHEDULE_EXACT_ALARM to findViewById(R.id.allow_SCHEDULE_EXACT_ALARM),
        )

        // Set listener izin normal
        permissionButtons.forEach { (perm, button) ->
            if (perm == Manifest.permission.SCHEDULE_EXACT_ALARM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                button.setOnClickListener {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Izin sudah diberikan", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                button.setOnClickListener {
                    requestPermissionLauncher.launch(arrayOf(perm))
                }
            }

            updateButtonStatus(perm, checkPermissionStatus(perm))
        }

        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val fcmtoken = sh.getString("fcmtoken", null)
        role = sh.getString("role", null)

        if (areAllPermissionsGranted()) {
            if (fcmtoken != null) {
                startActivity(Intent(this@PreStart, NewSetPin::class.java))
                finish()
            } else {
                loadFcmToken()
            }
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            if (areAllPermissionsGranted()) {
                loadFcmToken()
            } else {
                Toast.makeText(this, "Harap berikan semua izin terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionStatus(permission: String): Boolean {
        return when (permission) {
            Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    alarmManager.canScheduleExactAlarms()
                } else true
            }
            else -> ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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
        val normalPermissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        val alarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else true

        return normalPermissionsGranted && alarmGranted
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
