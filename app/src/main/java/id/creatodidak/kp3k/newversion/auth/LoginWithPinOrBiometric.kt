package id.creatodidak.kp3k.newversion.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.otpview.OTPListener
import com.otpview.OTPTextView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.newversion.NewDashboard

class LoginWithPinOrBiometric : AppCompatActivity() {
    private lateinit var otpTextView: OTPTextView
    private var settedPin: String? = ""
    private var isBiometric: Boolean = false

    private lateinit var sh: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_with_pin_or_biometric)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        otpTextView = findViewById(R.id.inputPinView)
        val tvBiometric = findViewById<TextView>(R.id.tvBiometric)
        val openBiometric = findViewById<ImageView>(R.id.openBiometric)
        val tvPINSalah = findViewById<TextView>(R.id.tvPINSalah)

        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        settedPin = sh.getString("PIN", "")
        isBiometric = sh.getBoolean("BIOMETRIC", false)

        if (isBiometric && isBiometricAvailable()) {
            tvBiometric.visibility = View.VISIBLE
            openBiometric.visibility = View.VISIBLE
        } else {
            tvBiometric.visibility = View.GONE
            openBiometric.visibility = View.GONE
        }

        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {}

            override fun onOTPComplete(otp: String) {
                if (settedPin == otp) {
                    goToDashboard()
                } else {
                    otpTextView.showError()
                    otpTextView.setOTP("")
                    tvPINSalah.visibility = View.VISIBLE

                    tvPINSalah.postDelayed({
                        tvPINSalah.visibility = View.GONE
                    }, 3000)
                }
            }
        }

        openBiometric.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, NewDashboard::class.java))
        finish()
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
            .setSubtitle("Gunakan sidik jari untuk masuk")
            .setNegativeButtonText("Batal")
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToDashboard()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginWithPinOrBiometric, "Autentikasi dibatalkan", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginWithPinOrBiometric, "Sidik jari tidak cocok", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}
