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
import id.creatodidak.kp3k.newversion.setting.Account
import id.creatodidak.kp3k.newversion.setting.Setting
import kotlinx.coroutines.launch

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

        tvNama.text = sh.getString("nama", "")
        tvNrp.text = sh.getString("pangkat", "")+"/"+sh.getString("nrp", "")

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

        Glide.with(this)
            .load(BASE_URL+"media"+sh.getString("foto", ""))
            .placeholder(R.drawable.outline_account_circle_24)
            .circleCrop()
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .into(ivProfilPhoto)


        if(role.equals("BPKP")){
            tvRole.text = role+" DESA "+sh.getString("desa_nama", "")+" KEC. "+sh.getString("kecamatan_nama", "")+" KAB. "+sh.getString("kabupaten_nama", "")+" "+sh.getString("provinsi_nama", "")
        }else{
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

//        swlDashboard.setOnRefreshListener {
//            lifecycleScope.launch {
//                loadDataFromServer(role!!)
//            }
//        }
//
//        lifecycleScope.launch {
//            loadDataFromServer(role!!)
//        }
    }
//
//    private suspend fun loadDataFromServer(role : String){
//        swlDashboard.isRefreshing = true
//        nsvDashboard.visibility = View.GONE
//        try {
//            syncDataFromServer(this)
//        }catch (e : Exception){
//            e.printStackTrace()
//        }finally {
//            swlDashboard.isRefreshing = false
//            nsvDashboard.visibility = View.VISIBLE
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        lifecycleScope.launch {
//            loadDataFromServer(sh.getString("role", "")!!)
//        }
//    }
}