package id.creatodidak.kp3k

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import id.creatodidak.kp3k.api.Auth
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.TokenRegister
import id.creatodidak.kp3k.dashboard.DashboardOpsional
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {
    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        window.statusBarColor = "#4CAF50".toColorInt()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usernameET = findViewById(R.id.usernameEditText)
        passwordET = findViewById(R.id.passwordEditText)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            lifecycleScope.launch { attemptLogin() }
        }
    }

    private suspend fun attemptLogin() {
        val username = usernameET.text.toString().trim()
        val password = passwordET.text.toString().trim()

        if (!validateInput(username, password)) return

        setInputsEnabled(false)
        Loading.show(this@Login)

        try {
            val response = Client.retrofit
                .create(Auth::class.java)
                .login(LoginRequest(username, password, "BPKP"))

            response.user?.let { user ->
                val prefs = getSharedPreferences("session", MODE_PRIVATE).edit()
                with(prefs) {
                    putString("token", response.token)
                    putString("nrp", user.nrp)
                    putString("nohp", user.nohp)
                    putString("jabatan", user.jabatan)
                    putString("polda", user.polda)
                    putString("polres", user.polres)
                    putString("namapolres", response.polres?.nama)
                    putString("polsek", user.polsek)
                    putString("namapolsek", response.polsek?.nama ?: "-")
                    putString("foto", user.foto)
                    putString("role", user.role)
                    putString("status", user.status)
                    putString("nama", user.nama)
                    putString("pangkat", user.pangkat)
                    putString("desa", response.desabinaan)
                    putString("desaid", response.desabinaanId)
                    putString("provinsi", "KALIMANTAN BARAT")
                    putString("kabupaten", response.kabupatenbinaan)
                    putString("kabupatenid", response.kabupatenbinaanId)
                    putString("kecamatan", response.kecamatanbinaan)
                    putString("kecamatanid", response.kecamatanbinaanId)
                    putBoolean("isLoggedIn", true)
                    apply()
                }
                loadFcmToken(user.nrp!!)
            } ?: showErrorDialog("Username atau password salah")
        } catch (e: Exception) {
            showErrorDialog("Terjadi kesalahan: ${e.localizedMessage}")
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var valid = true
        if (TextUtils.isEmpty(username)) {
            usernameET.error = "Username tidak boleh kosong"
            valid = false
        }
        if (TextUtils.isEmpty(password)) {
            passwordET.error = "Password tidak boleh kosong"
            valid = false
        }
        return valid
    }

    private fun loadFcmToken(nrp: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    val prefs = getSharedPreferences("session", MODE_PRIVATE).edit()
                    prefs.putString("fcmtoken", fcmToken).apply()

                    lifecycleScope.launch {
                        registerFcmToken(fcmToken, nrp)
                    }
                } else {
                    setInputsEnabled(true)
                    Loading.hide()
                    showErrorDialog("Gagal mendapatkan token FCM")
                }
            }
    }

    private suspend fun registerFcmToken(fcmToken: String, nrp: String) {
        try {
            Client.retrofit
                .create(Auth::class.java)
                .registerFcm(TokenRegister(nrp, fcmToken))

            getSharedPreferences("session", MODE_PRIVATE).edit().apply {
                putBoolean("isFcmRegistered", true)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            setInputsEnabled(true)
            Loading.hide()
            startActivity(Intent(this@Login, SetPin::class.java))
            finish()
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Gagal Login")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
        setInputsEnabled(true)
        Loading.hide()
    }

    private fun setInputsEnabled(enabled: Boolean) {
        usernameET.isEnabled = enabled
        passwordET.isEnabled = enabled
        btnLogin.isEnabled = enabled
    }
}
