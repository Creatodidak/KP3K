package id.creatodidak.kp3k.newversion

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.otpview.OTPListener
import com.otpview.OTPTextView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.NewAuth
import id.creatodidak.kp3k.api.newModel.LoginResponse
import id.creatodidak.kp3k.api.newModel.NewLoginRequest
import id.creatodidak.kp3k.api.newModel.OTPRequest
import id.creatodidak.kp3k.api.newModel.OTPResponse
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.showError
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

class VerifikasiOTP : AppCompatActivity() {
    private lateinit var otpTextView : OTPTextView
    private lateinit var emailOTP: TextView
    private lateinit var countdownText: TextView
    private var countDownTimer: CountDownTimer? = null
    private lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verifikasi_otp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        countdownText = findViewById(R.id.countdownText)
        otpTextView = findViewById(R.id.otp_view) as OTPTextView
        emailOTP = findViewById(R.id.emailOTP)

        sharedPref = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE)
        val lastLoginString = sharedPref.getString("LAST_LOGIN", null)

        val sh = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE)
        emailOTP.text = "${sh.getString("USERNAME", "")}@polri.go.id"
        otpTextView.requestFocusOTP()
        otpTextView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                lifecycleScope.launch {
                    cekOTP(otp)
                }
            }
        }

        if (lastLoginString != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val lastLogin = Instant.parse(lastLoginString)
                val now = Instant.now()
                val elapsedMillis = Duration.between(lastLogin, now).toMillis()
                val remainingMillis = 5 * 60 * 1000 - elapsedMillis

                if (remainingMillis <= 0) {
                    redirectToLogin()
                    return
                }

                startCountdown(remainingMillis)

            } catch (e: Exception) {
                e.printStackTrace()
                redirectToLogin()
            }
        } else {
            redirectToLogin()
        }
    }

    private fun startCountdown(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                countdownText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                redirectToLogin()
            }
        }.start()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, NewLogin::class.java))
        finish()
    }

    private suspend fun cekOTP(otp: String) {
        Loading.show(this@VerifikasiOTP)
        try {
            val response = Client.retrofit.create(NewAuth::class.java)
                .cekOTP(
                    OTPRequest(
                        sharedPref.getString("USERNAME", "")!!,
                        otp
                    )
                )

            if (response.isSuccessful && response.body() != null) {
                val res = response.body()
                val data = res?.data!!
                val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE).edit()
                with(sh) {
                    putBoolean("IS_LOGGED_IN", true)
                    putString("LAST_LOGIN", Instant.now().toString())
                    putString("LAST_LOGIN_ROLE", data.role)

                    putInt("id", data.id ?: -1)
                    putString("nrp", data.nrp ?: "")
                    putString("nohp", data.nohp ?: "")
                    putString("jabatan", data.jabatan ?: "")
                    putString("pangkat", data.pangkat ?: "")
                    putString("foto", data.foto ?: "")
                    putString("role", data.role ?: "")
                    putString("status", data.status ?: "")
                    putInt("satkerId", data.satkerId ?: -1)
                    putInt("desaBinaanId", data.desaBinaanId ?: -1)
                    putString("password", data.password ?: "")
                    putString("passwordiv", data.passwordiv ?: "")
                    putString("nama", data.nama ?: "")

                    // Satker (nested object)
                    putString("satker_nama", data.satker?.nama ?: "")
                    putString("satker_kode", data.satker?.kode ?: "")
                    putString("satker_level", data.satker?.level ?: "")
                    putInt("satker_id", data.satker?.id ?: -1)
                    putInt("satker_kabupatenId", data.satker?.kabupatenId ?: -1)
                    putInt("satker_provinsiId", data.satker?.provinsiId ?: -1)

                    // Parent Satker
                    putString("satker_parent_nama", data.satker?.parent?.nama ?: "")
                    putString("satker_parent_level", data.satker?.parent?.level ?: "")

                    //Parent of Parent Satker
                    putString("satkerparent_parent_nama", data.satker?.parent?.parent?.nama ?: "")
                    putString("satkerparent_parent_level", data.satker?.parent?.parent?.level ?: "")

                    // Desa Binaan
                    putString("desa_nama", data.desaBinaan?.nama ?: "")
                    putInt("desa_id", data.desaBinaan?.id ?: -1)
                    putInt("desa_kecamatanId", data.desaBinaan?.kecamatanId ?: -1)

                    // Kecamatan
                    putString("kecamatan_nama", data.desaBinaan?.kecamatan?.nama ?: "")
                    putInt("kecamatan_id", data.desaBinaan?.kecamatan?.id ?: -1)

                    // Kabupaten
                    putString("kabupaten_nama", data.desaBinaan?.kecamatan?.kabupaten?.nama ?: "")
                    putInt("kabupaten_id", data.desaBinaan?.kecamatan?.kabupaten?.id ?: -1)

                    // Provinsi
                    putString("provinsi_nama", data.desaBinaan?.kecamatan?.kabupaten?.provinsi?.nama ?: "")
                    putInt("provinsi_id", data.desaBinaan?.kecamatan?.kabupaten?.provinsi?.id ?: -1)

                    apply()
                }

                val sharedPreferencesEditor = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE).edit()
                sharedPreferencesEditor.clear()
                sharedPreferencesEditor.apply()

                val intent = Intent(this@VerifikasiOTP, PreStart::class.java)
                startActivity(intent)
                finish()
            } else {
                val errorJson = response.errorBody()?.string()
                val msg = try {
                    val json = JSONObject(errorJson ?: "")
                    json.optString("msg", "Error")
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }

                showError(this, "Error", msg)
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString())
        }finally {
            Loading.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}