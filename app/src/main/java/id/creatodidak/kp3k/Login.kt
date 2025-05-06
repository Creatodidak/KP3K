package id.creatodidak.kp3k

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
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
            lifecycleScope.launch {
                gasLogin()
            }
        }
    }

    private suspend fun gasLogin() {
        if (isValid()) {
            passwordET.setEnabled(false)
            usernameET.setEnabled(false)
            btnLogin.setEnabled(false)

            try {
                val response = Client.retrofit
                    .create(Auth::class.java)
                    .login(LoginRequest(usernameET.text.toString(), passwordET.text.toString(), "BPKP"))

                val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
                with(sharedPreferences.edit()){
                    putString("token", response.token)
                    putString("nrp", response.user?.nrp)
                    putString("nohp", response.user?.nohp)
                    putString("jabatan", response.user?.jabatan)
                    putString("polda", response.user?.polda)
                    putString("polres", response.user?.polres)
                    putString("namapolres", response.polres?.nama)
                    putString("polsek", response.user?.polsek)
                    putString("namapolsek", if (response.polsek != null) response.polsek.nama else "-")
                    putString("foto", response.user?.foto)
                    putString("role", response.user?.role)
                    putString("status", response.user?.status)
                    putString("nama", response.user?.nama)
                    putString("pangkat", response.user?.pangkat)
                    putString("desabinaan", response.desabinaan?.nama)
                    putString("provinsi", response.desabinaan?.provinsi)
                    putString("kabupaten", response.desabinaan?.kabupaten)
                    putString("kecamatan", response.desabinaan?.kecamatan)
                    putBoolean("isLoggedIn", true)
                    apply()
                }
                Loading.show(this@Login)
                if(response.user?.nrp !== null){
                    loadFcmToken(response.user.nrp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (TextUtils.isEmpty(usernameET.text.toString())) {
                usernameET.error = "Username tidak boleh kosong"
            }
            if (TextUtils.isEmpty(passwordET.text.toString())) {
                passwordET.error = "Password tidak boleh kosong"
            }
        }
    }

    private fun loadFcmToken(nrp: String) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fcmToken = task.result

                        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
                        with(sharedPreferences.edit()){
                            putString("fcmtoken", fcmToken)
                            apply()
                        }
                        lifecycleScope.launch {
                            registerFcmToken(fcmToken, nrp)
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private suspend fun registerFcmToken(fcmToken: String, nrp: String) {
        try {
            val response = Client.retrofit
                .create(Auth::class.java)
                .registerFcm(TokenRegister(nrp, fcmToken))

            val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
            with(sharedPreferences.edit()){
                putBoolean("isFcmRegistered", true)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {
            passwordET.setEnabled(true)
            usernameET.setEnabled(true)
            btnLogin.setEnabled(true)
            Loading.hide()

            val i = Intent(this@Login, SetPin::class.java)
            startActivity(i)
            finish()
        }
    }


    private fun isValid(): Boolean {
        return !TextUtils.isEmpty(usernameET.text.toString()) &&
                !TextUtils.isEmpty(passwordET.text.toString())
    }

}