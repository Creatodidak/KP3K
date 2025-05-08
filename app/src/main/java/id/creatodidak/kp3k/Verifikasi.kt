package id.creatodidak.kp3k

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch
import androidx.core.content.edit

class Verifikasi : AppCompatActivity() {
    private var nrp = ""
    private lateinit var lyStep1 : LinearLayout
    private lateinit var lyStep2 : LinearLayout
    private lateinit var sh : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi)

        sh = getSharedPreferences("session", MODE_PRIVATE)
        nrp = sh.getString("nrp", "")!!

        val btnKodeVerifikasi = findViewById<Button>(R.id.btKodeVerifikasi)
        val btnVerifikasi = findViewById<Button>(R.id.btVerifikasi)
        lyStep1 = findViewById<LinearLayout>(R.id.lyStep1)
        lyStep2 = findViewById<LinearLayout>(R.id.lyStep2)
        val tvNoWa = findViewById<EditText>(R.id.tvNoWa)
        val etKodeVerifikasi = findViewById<EditText>(R.id.etKodeVerifikasi)

        btnKodeVerifikasi.setOnClickListener {
            if(tvNoWa.text.toString().isNotEmpty()){
                lifecycleScope.launch {
                    getKodeVerifikasi("08${tvNoWa.text.toString()}")
                }
            }else{
                tvNoWa.error = "Tidak boleh kosong"
            }
        }
        btnVerifikasi.setOnClickListener {
            if(etKodeVerifikasi.text.toString().isNotEmpty()){
                lifecycleScope.launch {
                    tryVerifikasi()
                }
            }else{
                etKodeVerifikasi.error = "Tidak boleh kosong"
            }
        }
    }

    private suspend fun getKodeVerifikasi(nohp : String){
        Loading.show(this)
        try {
            val response = Client.retrofit.create(Data::class.java).getKodeVerifikasi(nrp, nohp)
            if(response.kode == 200){
                Loading.hide()
                lyStep1.visibility = View.GONE
                lyStep2.visibility = View.VISIBLE
            }else{
                Loading.hide()
                AlertDialog.Builder(this@Verifikasi)
                    .setTitle("Error")
                    .setMessage(response.msg)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }catch (e: Exception){
            e.printStackTrace()
            Loading.hide()
            AlertDialog.Builder(this@Verifikasi)
                .setTitle("Error")
                .setMessage(e.message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    private suspend fun tryVerifikasi(){
        Loading.show(this)
        try {
            val kode = findViewById<EditText>(R.id.etKodeVerifikasi).text.toString()
            val response = Client.retrofit.create(Data::class.java).sendKodeVerifikasi(nrp, kode)
            if(response.kode == 200){
                Loading.hide()
                sh.edit() {
                    putString("status", "VERIFIED")
                    putString(
                        "nohp",
                        "08${findViewById<EditText>(R.id.tvNoWa).text.toString()}"
                    )
                    apply()
                }
                AlertDialog.Builder(this@Verifikasi)
                    .setTitle("Berhasil")
                    .setMessage(response.msg)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }else{
                Loading.hide()
                AlertDialog.Builder(this@Verifikasi)
                    .setTitle("Error")
                    .setMessage(response.msg)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }catch (e: Exception){
            e.printStackTrace()
            Loading.hide()
            AlertDialog.Builder(this@Verifikasi)
                .setTitle("Error")
                .setMessage(e.message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}