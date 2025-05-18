package id.creatodidak.kp3k.pimpinan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.network.SocketManager
import io.socket.client.Socket
import kotlinx.coroutines.launch
import java.lang.Float.parseFloat

class DashboardPimpinan : AppCompatActivity() {
    private lateinit var sh: SharedPreferences
    val socket = SocketManager.getSocket()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_KP3K_PIMPINAN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pimpinan)
        sh = getSharedPreferences("session", MODE_PRIVATE)

        val cvPetaLahan = findViewById<androidx.cardview.widget.CardView>(R.id.cvPetaLahan)
        val cvLaporan = findViewById<androidx.cardview.widget.CardView>(R.id.cvLaporan)
        val cvMonitoring = findViewById<androidx.cardview.widget.CardView>(R.id.cvMonitoring)
        val cvAtensi = findViewById<androidx.cardview.widget.CardView>(R.id.cvAtensi)
        val cvVideoCall = findViewById<androidx.cardview.widget.CardView>(R.id.cvVideoCall)
        val cvLogout = findViewById<androidx.cardview.widget.CardView>(R.id.cvLogout)

        lifecycleScope.launch {
            loadData()
        }
        val tvJabatan = findViewById<TextView>(R.id.tvJabatan)
        tvJabatan.text = sh.getString("jabatan", "")

        cvPetaLahan.setOnClickListener {
            val i = Intent(this, PetaLahanPimpinan::class.java)
            startActivity(i)
        }
        cvLaporan.setOnClickListener {

        }
        cvMonitoring.setOnClickListener {

        }
        cvAtensi.setOnClickListener {

        }
        cvVideoCall.setOnClickListener {

        }
        cvLogout.setOnClickListener {
        }
    }

    private suspend fun loadData() {
        Loading.show(this)
        try {
            val tvJPemilikLahan = findViewById<TextView>(R.id.tvJPemilik)
            val tvJBPKP = findViewById<TextView>(R.id.tvJBPKP)
            val tvJMono = findViewById<TextView>(R.id.tvJMono)
            val tvJTumpangSari = findViewById<TextView>(R.id.tvJTumpangSari)
            val tvJLuasLahanMono = findViewById<TextView>(R.id.tvJLuasLahanMono)
            val tvJLuasLahanTumpangSari = findViewById<TextView>(R.id.tvJLuasLahanTumpangSari)

            val res = Client.retrofit.create(DataPimpinan::class.java).getBasic()
            tvJPemilikLahan.text = res.pemilikLahan.toString()
            tvJBPKP.text = res.jumlahPersonil.toString()
            tvJMono.text = res.lahanMono.toString()
            tvJTumpangSari.text = res.lahantumpangsari.toString()
            val luasMono = res.luasLahanMono?.toString()?.toDoubleOrNull()?.div(10_000) ?: 0.0
            tvJLuasLahanMono.text = String.format("%.2f ha", luasMono)
            val luas = res.luasLahanTumpangSari?.toString()?.toDoubleOrNull()?.div(10_000) ?: 0.0
            tvJLuasLahanTumpangSari.text = String.format("%.2f ha", luas)
            Loading.hide()
        }catch (e: Exception){
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Terjadi kesalahan: ${e.message}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            Loading.hide()
        }
    }
}