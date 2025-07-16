package id.creatodidak.kp3k.newversion

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.NewAuth
import id.creatodidak.kp3k.api.newModel.LoginResponse
import id.creatodidak.kp3k.api.newModel.NewLoginRequest
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

class NewLogin : AppCompatActivity() {
    private val roles = listOf<String>("PILIH ROLE", "SUPER ADMIN", "PJU POLDA", "PAMATWIL", "PJU POLRES", "KAPOLSEK", "ADMIN POLDA", "ADMIN POLRES", "ADMINPOLSEK", "PERSONIL POLDA", "BINTARA PENGGERAK", "PERSONIL POLRES", "PERSONIL POLSEK")
    private lateinit var spinnerRole: Spinner;
    private lateinit var inputUsername: EditText;
    private lateinit var inputPassword: EditText;
    private lateinit var buttonLogin: Button;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        val sharedPref = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE)
        val lastLoginString = sharedPref.getString("LAST_LOGIN", null)
        val lastLoginRole = sharedPref.getString("ROLE", null)
        val excludedRoles = setOf("PJU POLDA", "PAMATWIL", "PJU POLRES", "PJU POLSEK")

        if (lastLoginString != null && lastLoginRole != null && lastLoginRole !in excludedRoles) {
            try {
                val lastLogin = Instant.parse(lastLoginString)
                val now = Instant.now()
                val duration = Duration.between(lastLogin, now).toMinutes()

                if (duration < 5) {
                    startActivity(Intent(this, VerifikasiOTP::class.java))
                    finish()
                    return
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val isLoggedIn = sh.getBoolean("IS_LOGGED_IN", false)
        if( isLoggedIn) {
            startActivity(Intent(this, PreStart::class.java))
            finish()
            return
        }

        spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        inputUsername = findViewById<EditText>(R.id.inputUsername)
        inputPassword = findViewById<EditText>(R.id.inputPassword)
        buttonLogin = findViewById<Button>(R.id.buttonLogin)

        spinnerRole.adapter = ArrayAdapter(this@NewLogin, android.R.layout.simple_spinner_dropdown_item, roles)

        spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    inputUsername.isEnabled = false
                    inputPassword.isEnabled = false
                }else{
                    inputUsername.isEnabled = true
                    inputPassword.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        buttonLogin.setOnClickListener {
            if(isFormValid()){
                if (spinnerRole.selectedItemPosition in listOf(1, 2, 3)) {
                    startLogin()
                } else {
                    startLogin()
                }
            }else{
                showAlert("Pilih dan Isi Form dengan benar!")
            }
        }
    }

    private fun startLogin() {
        Loading.show(this@NewLogin)

        val call = Client.retrofit.create(NewAuth::class.java)
            .login(
                NewLoginRequest(
                    inputUsername.text.toString(),
                    inputPassword.text.toString()
                )
            )

        call.enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
                Loading.hide()

                if (response.isSuccessful) {
                    val res = response.body()
                    val sh = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE).edit()
                    with(sh) {
                        putBoolean("RECENTLY_LOGIN", true)
                        putString("LAST_LOGIN", res?.createdAt)
                        putString("ROLE", spinnerRole.selectedItem.toString())
                        putString("USERNAME", inputUsername.text.toString())
                        apply()
                    }

                    val intent = Intent(this@NewLogin, VerifikasiOTP::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorJson = response.errorBody()?.string()
                    val msg = try {
                        val json = JSONObject(errorJson ?: "")
                        json.optString("msg", "Login gagal")
                    } catch (e: Exception) {
                        "Login gagal"
                    }

                    if(msg == "Anda sudah melakukan request OTP sebelumnya, silahkan cek Email Polri anda!"){
                        val sh = getSharedPreferences("LOGIN_STATE", MODE_PRIVATE).edit()
                        with(sh) {
                            putBoolean("RECENTLY_LOGIN", true)
                            putString("ROLE", spinnerRole.selectedItem.toString())
                            putString("USERNAME", inputUsername.text.toString())
                            apply()
                        }

                        val intent = Intent(this@NewLogin, VerifikasiOTP::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        showAlert(msg)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                Loading.hide()
                Log.e("LoginError", t.message.toString())
                showAlert("Terjadi kesalahan jaringan: ${t.message}")
            }
        })
    }


    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Login Gagal")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isFormValid(): Boolean {
        if(spinnerRole.selectedItemPosition == 0) {
            return false
        }

        if(inputUsername.text.toString().isEmpty()) {
            inputUsername.error = "Username/NRP tidak boleh kosong!"
            return false
        }

        if(inputPassword.text.toString().isEmpty()) {
            inputPassword.error = "Password tidak boleh kosong!"
            return false
        }

        return true
    }
}
