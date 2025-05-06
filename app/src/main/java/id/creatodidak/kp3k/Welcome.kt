package id.creatodidak.kp3k

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.dashboard.DashboardOpsional
import permissions.dispatcher.*
import android.widget.Toast

@RuntimePermissions
class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val versionName = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager
                    .getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                    .versionName
            } else {
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }

        val masukBtn = findViewById<Button>(R.id.btMasuk)
//        val daftarBtn = findViewById<Button>(R.id.btDaftar)
        val versi = findViewById<TextView>(R.id.versi)

        versi.text = "Versi $versionName"
//
//        val logoImage = findViewById<ImageView>(R.id.logoKp3k)
//        Glide.with(this)
//            .load(R.drawable.logo)
//            .circleCrop()
//            .into(logoImage)

        masukBtn.setOnClickListener {
            val i = Intent(this, Login::class.java)
            startActivity(i)
        }

//        daftarBtn.setOnClickListener {
//            val i = Intent(this, VideoCallActivity::class.java)
//            startActivity(i)
//        }

        // Check permissions when the activity is created
        checkPermissionsWithPermissionCheck()
        val sh = getSharedPreferences("session", MODE_PRIVATE)
        if(sh.getBoolean("isLoggedIn", false)){
            val i = Intent(this, SetPin::class.java)
            startActivity(i)
            finish()
        }
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.USE_BIOMETRIC,
        Manifest.permission.USE_FINGERPRINT,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.RECORD_AUDIO
    )
    fun checkPermissions() {
        // All required permissions are granted
        Toast.makeText(this, "Semua izin diberikan!", Toast.LENGTH_SHORT).show()
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.USE_BIOMETRIC,
        Manifest.permission.USE_FINGERPRINT,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.RECORD_AUDIO
    )
    fun onPermissionsDenied() {
        // Handle the case where permissions are denied
        Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.USE_BIOMETRIC,
        Manifest.permission.USE_FINGERPRINT,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.RECORD_AUDIO
    )
    fun showRationaleForPermissions(request: PermissionRequest) {
        // Show rationale dialog to explain why the permissions are needed
        AlertDialog.Builder(this)
            .setMessage("Aplikasi memerlukan izin ini untuk berfungsi dengan baik.")
            .setPositiveButton("Izinkan") { _, _ -> request.proceed() }
            .setNegativeButton("Tolak") { _, _ -> request.cancel() }
            .show()
    }

    @OnNeverAskAgain(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.USE_BIOMETRIC,
        Manifest.permission.USE_FINGERPRINT,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.RECORD_AUDIO
    )
    fun onNeverAskAgain() {
        // Handle case when the user selects "Don't ask again" for permissions
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Delegate permission handling to PermissionsDispatcher
        onRequestPermissionsResult(requestCode, grantResults)
    }
}
