package id.creatodidak.kp3k.newversion.setting

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.DatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@SuppressLint("UseSwitchCompatOrMaterialCode")
class Setting : AppCompatActivity() {
    private lateinit var switchBio: Switch
    private lateinit var btnLogout : Button
    private lateinit var statusBio: TextView
    private lateinit var statusOfflineMode: TextView
    private lateinit var sh: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.default_bg)

        // Init
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        switchBio = findViewById(R.id.switchBio)
        statusBio = findViewById(R.id.statusBio)

        // Set switch state from SharedPreferences
        switchBio.isChecked = sh.getBoolean("BIOMETRIC", false)

        // Set initial status text
        statusBio.text = if (switchBio.isChecked) "Diaktifkan" else "Non-aktif"

        // Disable switch if biometric not available
        if (!isBiometricAvailable()) {
            switchBio.isEnabled = false
            statusBio.text = "Tidak tersedia"
        }

        // Switch Biometric logic
        switchBio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (isBiometricAvailable()) {
                    showBiometricPrompt()
                } else {
                    switchBio.isChecked = false
                    statusBio.text = "Tidak tersedia"
                    Toast.makeText(this, "Perangkat tidak mendukung biometrik", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Tampilkan konfirmasi sebelum mematikan
                confirmDisableBiometric()
            }
        }

        btnLogout = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    sh.edit {
                        clear()
                        apply()
                    }

                    val sharedPreferences = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE).edit()
                    with(sharedPreferences) {
                        clear()
                        apply()
                    }

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val db = DatabaseInstance.getDatabase(this@Setting)
                            db.clearAllTables()
                        }

                        // Tutup activity setelah database dibersihkan
                        delay(300)

                        finishAffinity() // Tutup semua activity
                        exitProcess(0)   // Kill process
                    }
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Sidik Jari")
            .setSubtitle("Autentikasi untuk mengaktifkan sidik jari")
            .setNegativeButtonText("Batal")
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Simpan preferensi hanya jika berhasil
                    sh.edit {
                        putBoolean("BIOMETRIC", true)
                    }
                    statusBio.text = "Diaktifkan"
                    Toast.makeText(this@Setting, "Biometrik berhasil diaktifkan", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        switchBio.isChecked = false
                        Toast.makeText(this@Setting, "Biometrik dibatalkan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@Setting, "Autentikasi gagal. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun confirmDisableBiometric() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nonaktifkan Biometrik")
            .setMessage("Apakah Anda yakin ingin menonaktifkan fitur sidik jari?")
            .setPositiveButton("Ya") { _, _ ->
                // Set biometric ke false
                sh.edit {
                    putBoolean("BIOMETRIC", false)
                }
                statusBio.text = "Non-aktif"
                Toast.makeText(this, "Biometrik dinonaktifkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal") { _, _ ->
                // Kembalikan switch ke ON
                switchBio.isChecked = true
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

}
