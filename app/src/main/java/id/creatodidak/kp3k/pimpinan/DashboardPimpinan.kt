package id.creatodidak.kp3k.pimpinan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.Login
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.Welcome
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.pamatwil.DashboardPamatwil
import io.socket.client.Socket
import kotlinx.coroutines.launch
import java.lang.Float.parseFloat
import androidx.core.content.edit
import id.creatodidak.kp3k.helper.formatDuaDesimalKoma

class DashboardPimpinan : AppCompatActivity() {
    private lateinit var sh: SharedPreferences
    val socket = SocketManager.getSocket()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pimpinan)
        sh = getSharedPreferences("session", MODE_PRIVATE)
        val jabatan = sh.getString("jabatan", "")
        val role = sh.getString("role", "")
        val changeAccount = findViewById<ImageView>(R.id.changeAccount)
        if(jabatan.equals("KABID TIK")){
            changeAccount.visibility = View.VISIBLE
        }

        changeAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin beralih role?")
                .setPositiveButton("Ya") { dialog, _ ->
                    dialog.dismiss()
                    if (role.equals("PIMPINAN")) {
                        with(sh.edit()) {
                            putString("role", "PAMATWIL")
                            apply()
                        }

                        val i = Intent(this, DashboardPamatwil::class.java)
                        startActivity(i)
                        finish()
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
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
            val i = Intent(this, MonitoringPimpinan::class.java)
            startActivity(i)
        }
        cvAtensi.setOnClickListener {
            val i = Intent(this, AddAtensi::class.java)
            startActivity(i)
        }
        cvVideoCall.setOnClickListener {
            val i = Intent(this, PanggilanVideo::class.java)
            startActivity(i)
        }
        cvLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    sh.edit() { clear() }
                    socket.disconnect()
                    val i = Intent(this, Welcome::class.java)
                    startActivity(i)
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
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
            tvJPemilikLahan.text = formatDuaDesimalKoma(res.pemilikLahan!!.toDouble())
            tvJBPKP.text = formatDuaDesimalKoma(res.jumlahPersonil!!.toDouble())
            tvJMono.text = formatDuaDesimalKoma(res.lahanMono!!.toDouble())
            tvJTumpangSari.text = formatDuaDesimalKoma(res.lahantumpangsari!!.toDouble())
            val luasMono = res.luasLahanMono?.toString()?.toDoubleOrNull()?.div(10_000) ?: 0.0
            tvJLuasLahanMono.text = formatDuaDesimalKoma(luasMono)
            val luas = res.luasLahanTumpangSari?.toString()?.toDoubleOrNull()?.div(10_000) ?: 0.0
            tvJLuasLahanTumpangSari.text = formatDuaDesimalKoma(luas)
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