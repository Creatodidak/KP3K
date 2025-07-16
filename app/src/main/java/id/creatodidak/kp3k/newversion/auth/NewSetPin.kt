package id.creatodidak.kp3k.newversion.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.otpview.OTPListener
import com.otpview.OTPTextView
import id.creatodidak.kp3k.R
import androidx.core.content.edit
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import id.creatodidak.kp3k.newversion.NewDashboard

class NewSetPin : AppCompatActivity() {
    private lateinit var otpTextView : OTPTextView
    private var pin : String = ""
    private var settedPin : String? = ""
    private var isBiometric : Boolean = false

    private lateinit var sh: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_set_pin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fabSetPin = findViewById<FloatingActionButton>(R.id.fabSetPin)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        settedPin = sh.getString("PIN", "")
        isBiometric = sh.getBoolean("BIOMETRIC", false)

        if(settedPin != ""){
            startActivity(Intent(this@NewSetPin, LoginWithPinOrBiometric::class.java))
            finish()
        }

        fabSetPin.isEnabled = false
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)

        otpTextView = findViewById(R.id.setPinView)
        otpTextView.otpListener = object: OTPListener {
            override fun onInteractionListener() {}
            override fun onOTPComplete(otp: String) {
                fabSetPin.isEnabled = true
                pin = otp
            }
        }

        fabSetPin.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Gunakan PIN $pin sebagai PIN Anda?")
                .setPositiveButton("Ya") { dialog, _ ->
                    sh.edit {
                        putString("PIN", pin)
                        apply()
                    }
                    dialog.dismiss()

                    if (isBiometricAvailable()) {
                        AlertDialog.Builder(this)
                            .setTitle("Gunakan Sidik Jari?")
                            .setMessage("Berhasil mengatur PIN, Apakah Anda ingin menggunakan sidik jari untuk login?")
                            .setPositiveButton("Ya") { d2, _ ->
                                showBiometricPrompt()
                                d2.dismiss()
                            }
                            .setNegativeButton("Tidak") { d2, _ ->
                                goToNext()
                                d2.dismiss()
                            }
                            .show()
                    } else {
                        goToNext()
                    }
                }
                .setNegativeButton("Tidak", null)
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
                        apply()
                    }
                    goToNext()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Jika user menekan batal, tetap lanjut tanpa biometric
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        goToNext()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Gagal scan → tidak simpan biometric
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun goToNext() {
        startActivity(Intent(this, NewDashboard::class.java))
        finish()
    }
}
