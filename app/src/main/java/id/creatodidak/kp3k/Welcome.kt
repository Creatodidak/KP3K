package id.creatodidak.kp3k

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import id.creatodidak.kp3k.newversion.NewLogin

class Welcome : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            // Misal loading = true, maka tahan splash
            false // false agar langsung hilang
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tampilkan versi aplikasi
        val versionName = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }

        findViewById<TextView>(R.id.versi).text = versionName
        findViewById<Button>(R.id.btMasuk).setOnClickListener {
            startActivity(Intent(this, NewLogin::class.java))
        }

        // Inisialisasi AppUpdateManager dan cek update
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

    private fun checkForUpdateReal() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    // Jalankan update wajib
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        !appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    // Update tersedia tapi tidak bisa IMMEDIATE
                    showUpdateNotAllowedMessage()
                }

                else -> {
                    // Tidak ada update, lanjut
                    proceedToNextScreen()
                }
            }
        }.addOnFailureListener {
            // Gagal cek update (mungkin offline)
            showUpdateNotAllowedMessage()
        }
    }
private fun checkForUpdate() {
    // DEV ONLY: Simulasi seolah ada update
    Handler(Looper.getMainLooper()).postDelayed({
        Toast.makeText(this, "Simulasi update IMMEDIATE", Toast.LENGTH_SHORT).show()
         proceedToNextScreen() // atau abaikan lanjut
    }, 2000)
}

    private fun proceedToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, NewLogin::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1000)
    }

    private fun showUpdateNotAllowedMessage() {
        Toast.makeText(this, "Update aplikasi diperlukan. Silakan coba lagi nanti.", Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity() // Keluar dari aplikasi
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        // Jika proses update tertunda, lanjutkan
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }
}
