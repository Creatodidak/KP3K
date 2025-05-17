package id.creatodidak.kp3k

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.api.Auth
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.model.PINRegister
import id.creatodidak.kp3k.dashboard.DashboardOpsional
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.creatodidak.kp3k.pimpinan.DashboardPimpinan

class SetPin : AppCompatActivity() {

    private lateinit var pinFields: List<EditText>
    private lateinit var fabNext: FloatingActionButton
    private lateinit var perintah: TextView
    private lateinit var lupaPin: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_pin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        pinFields = listOf(
            findViewById(R.id.pin1),
            findViewById(R.id.pin2),
            findViewById(R.id.pin3),
            findViewById(R.id.pin4),
            findViewById(R.id.pin5),
            findViewById(R.id.pin6)
        )

        setupPinInputs()

        fabNext = findViewById<FloatingActionButton>(R.id.nextFab)
        perintah = findViewById<TextView>(R.id.perintah)
        lupaPin = findViewById<TextView>(R.id.lupaPin)

        val sh = getSharedPreferences("session", MODE_PRIVATE)
        if(sh.getBoolean("isLoggedIn", false)){
            if(sh.getBoolean("isPinSetted", false)){
                perintah.text = "MASUKAN PIN"
                lupaPin.setVisibility(View.VISIBLE)
            }   else{
                perintah.text = "SILAHKAN ATUR PIN TERLEBIH DAHULU!"
            }
        }

        lupaPin.paintFlags = lupaPin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        lupaPin.setOnClickListener {
            val phoneNumber = "6289523468041"
            val message = "Halo, saya lupa pin...\nNRP saya ${sh.getString("nrp", "")}"

            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
                setPackage("com.whatsapp")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp tidak terpasang!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPinInputs() {
        for (i in pinFields.indices) {
            val editText = pinFields[i]

            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < pinFields.size - 1) {
                        pinFields[i + 1].requestFocus()
                    } else if (i == pinFields.size - 1 && allFieldsFilled()) {
                        val pin = pinFields.joinToString("") { it.text.toString() }
                        onPinEntered(pin)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() && i > 0
                ) {
                    pinFields[i - 1].apply {
                        requestFocus()
                        setText("")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun allFieldsFilled(): Boolean {
        return pinFields.all { it.text.toString().isNotEmpty() }
    }

    private fun onPinEntered(pin: String) {
        val sh = getSharedPreferences("session", MODE_PRIVATE)
        if(sh.getBoolean("isPinSetted", false)){
            if(pin == sh.getString("pin", "")){
                sh.edit() { putBoolean("isCurrentlyLogedIn", true) }

                val i = if(sh.getString("role", "") === "BPKP"){
                    Intent(this, DashboardOpsional::class.java)
                }else if(sh.getString("role", "") === "PIMPINAN"){
                    Intent(this, DashboardPimpinan::class.java)
                }else{
                    null
                }
                startActivity(i)
                finish()
            }   else{
                perintah.text = "PIN SALAH!"
            }
        }   else{
            fabNext.setVisibility(View.VISIBLE)
            fabNext.setOnClickListener {
                sh.edit() {
                    putBoolean("isPinSetted", true)
                    putBoolean("isCurrentlyLogedIn", true)
                    putString("pin", pin)
                }
                val nrp = if(intent.getStringExtra("role") === "BPKP"){
                    sh.getString("nrp", "")
                }else{
                    sh.getString("username", "")
                }
                if(nrp !== null){
                    lifecycleScope.launch {
                        registerPin(pin, nrp)
                    }
                }
            }
        }
    }

    private suspend fun registerPin(pin: String, nrp: String) {
        try {
            if(intent.getStringExtra("role") === "BPKP"){
                Client.retrofit
                    .create(Auth::class.java)
                    .registerPin(PINRegister(nrp, pin))
            }else if(intent.getStringExtra("role") === "PIMPINAN"){
                Client.retrofit
                    .create(Auth::class.java)
                    .registerPinPimpinan(Auth.PINRegisterPimpinan(nrp, pin))
            }else if(intent.getStringExtra("role") === "PAMATWIL"){
                Client.retrofit
                    .create(Auth::class.java)
                    .registerPinPamatwil(Auth.PINRegisterPimpinan(nrp, pin))
            }else if(intent.getStringExtra("role") === "KAPOLRES"){
                Client.retrofit
                    .create(Auth::class.java)
                    .registerPinKapolres(Auth.PINRegisterPimpinan(nrp, pin))
            }
            Loading.show(this@SetPin)

            val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
            with(sharedPreferences.edit()){
                putBoolean("isPinRegistered", true)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {
            Loading.hide()
            val i = if(intent.getStringExtra("role") === "BPKP"){
                Intent(this, DashboardOpsional::class.java)
            }else if(intent.getStringExtra("role") === "PIMPINAN"){
                Intent(this, DashboardPimpinan::class.java)
            }else{
                null
            }
            startActivity(i)
            finish()
        }
    }
}
