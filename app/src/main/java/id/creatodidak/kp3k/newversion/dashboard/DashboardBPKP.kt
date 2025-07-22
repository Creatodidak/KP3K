package id.creatodidak.kp3k.newversion.dashboard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.syncDataFromServer
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.isCanVideoCall
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.isPejabat
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.newversion.VideoCall.ClientVideoCall
import id.creatodidak.kp3k.newversion.VideoCall.HostVideoCall
import id.creatodidak.kp3k.newversion.VideoCall.IncomingCallWaiting
import id.creatodidak.kp3k.newversion.setting.Account
import id.creatodidak.kp3k.newversion.setting.Setting
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

class DashboardBPKP : AppCompatActivity() {
    private lateinit var sh : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_bpkp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            insets
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val role = sh.getString("role", "")
        val ivProfilPhoto = findViewById<ImageView>(R.id.ivProfilPhoto)
        val tvNama = findViewById<TextView>(R.id.tvNama)
        val tvNrp = findViewById<TextView>(R.id.tvNrp)
        val tvSatkerPers = findViewById<TextView>(R.id.tvSatkerPers)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val menu_akun = findViewById<ImageView>(R.id.menu_akun)
        val menu_petalahan = findViewById<ImageView>(R.id.menu_petalahan)
        val menu_hotspot = findViewById<ImageView>(R.id.menu_hotspot)
        val menu_setting = findViewById<ImageView>(R.id.menu_setting)
        val menu_jagung = findViewById<ImageView>(R.id.menu_jagung)
        val menu_singkong = findViewById<ImageView>(R.id.menu_singkong)
        val menu_kedelai = findViewById<ImageView>(R.id.menu_kedelai)
        val menu_alsintan = findViewById<ImageView>(R.id.menu_alsintan)
        val menu_gudang = findViewById<ImageView>(R.id.menu_gudang)
        val menu_koperasi = findViewById<ImageView>(R.id.menu_koperasi)
        val menu_videocall = findViewById<ImageView>(R.id.menu_videocall)
        menu_videocall.visibility = if (isCanVideoCall(this)) View.VISIBLE else View.GONE

        tvNama.text =
            if (isPejabat(this)) sh.getString("username", "") else sh.getString("nama", "")
        tvNrp.visibility = if (!isPejabat(this)) View.VISIBLE else View.GONE
        tvNrp.text = sh.getString("pangkat", "") + "/" + sh.getString("nrp", "")

        val jabatan = sh.getString("jabatan", "")
        when (sh.getString("satker_level", "")) {
            "POLDA" -> {
                val polda = "POLDA " + sh.getString("satker_nama", "")
                val persSatker = "$jabatan $polda"
                tvSatkerPers.text = persSatker
            }

            "POLRES" -> {
                val polres = "POLRES " + sh.getString("satker_nama", "")
                val polda = "POLDA " + sh.getString("satker_parent_nama", "")
                val persSatker = "$jabatan $polres $polda"
                tvSatkerPers.text = persSatker
            }

            "POLSEK" -> {
                val polsek = "POLSEK " + sh.getString("satker_nama", "")
                val polres = "POLRES " + sh.getString("satker_parent_nama", "")
                val polda = "POLDA " + sh.getString("satkerparent_parent_nama", "")
                val persSatker = "$jabatan $polsek $polres $polda"
                tvSatkerPers.text = persSatker
            }
        }

        if (!isPejabat(this)) {
            Glide.with(this)
                .load(BASE_URL + "media" + sh.getString("foto", ""))
                .placeholder(R.drawable.outline_account_circle_24)
                .circleCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .into(ivProfilPhoto)
        }


        if (role.equals("BPKP")) {
            tvRole.text = role + " DESA " + sh.getString(
                "desa_nama",
                ""
            ) + " KEC. " + sh.getString(
                "kecamatan_nama",
                ""
            ) + " KAB. " + sh.getString("kabupaten_nama", "") + " " + sh.getString(
                "provinsi_nama",
                ""
            )
        } else {
            tvRole.text = role
        }

        menu_setting.setOnClickListener {
            startActivity(android.content.Intent(this, Setting::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_akun.setOnClickListener {
            startActivity(android.content.Intent(this, Account::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_jagung.setOnClickListener {
            val i = Intent(this, DashboardKomoditas::class.java)
            i.putExtra("komoditas", "jagung")
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_singkong.setOnClickListener {
            val i = Intent(this, DashboardKomoditas::class.java)
            i.putExtra("komoditas", "singkong")
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_kedelai.setOnClickListener {
            val i = Intent(this, DashboardKomoditas::class.java)
            i.putExtra("komoditas", "kedelai")
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_hotspot.setOnClickListener {
            val i = Intent(this, Hotspots::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        menu_videocall.setOnClickListener {
            if(isOnline(this)){
                val i = Intent(this, VideoCall::class.java)
                startActivity(i)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }else{
                showError(this, "Akses Terbatas", "Tidak ada koneksi internet")
            }
        }

            connectToSocket()
    }

    private fun connectToSocket() {
        try {
            val socket = SocketManager.getSocket()

            if (!socket.connected()) {
                socket.connect()
                Log.i("SOCKET", "Connecting...")
            }

            socket.on(Socket.EVENT_CONNECT) {
                val nrp = getMyNrp(this@DashboardBPKP)

                socket.emit("register", JSONObject().apply {
                    put("nrp", nrp)
                })
                Log.i("SOCKET", "✅ Connected")
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) {
                Log.e("SOCKET", "❌ Connect Error: ${it.firstOrNull()}")
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.w("SOCKET", "⚠️ Socket Disconnected")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SOCKET", "❌ Exception: ${e.message}")
        }
    }

}