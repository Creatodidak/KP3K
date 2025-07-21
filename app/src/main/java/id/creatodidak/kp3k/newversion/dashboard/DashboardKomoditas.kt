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
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.PanenDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.DatabaseInstance.getDatabase
import id.creatodidak.kp3k.database.syncDataFromServer
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.forEach

class DashboardKomoditas : AppCompatActivity() {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")
    private lateinit var komoditas : String
    private lateinit var sh: SharedPreferences
    private lateinit var db : AppDatabase
    private lateinit var dbLahan : LahanDao
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbTanaman : TanamanDao
    private lateinit var dbPanen : PanenDao
    private lateinit var swlDashboardKomoditas : SwipeRefreshLayout
    private lateinit var tvTotalLuasLahan : TextView
    private lateinit var tvTotalJumlahLahan : TextView
    private lateinit var tvTotalLuasLahanMonokultur: TextView
    private lateinit var tvTotalJumlahLahanMonokultur: TextView
    private lateinit var tvTotalLuasLahanTumpangsari: TextView
    private lateinit var tvTotalJumlahLahanTumpangsari: TextView
    private lateinit var tvTotalLuasTanam : TextView
    private lateinit var tvTotalProduksi : TextView
    private lateinit var tvTotalOwner : TextView
    private lateinit var ivPemilikLahan : ImageView
    private lateinit var ivLahan : ImageView
    private lateinit var ivTanaman : ImageView
    private lateinit var ivPerkembangan : ImageView
    private lateinit var ivPanen : ImageView
    private lateinit var nsvDashboardKomoditas : NestedScrollView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_komoditas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        db = getDatabase(this@DashboardKomoditas)
        dbLahan = db.lahanDao()
        dbOwner = db.ownerDao()
        dbTanaman = db.tanamanDao()
        dbPanen = db.panenDao()

        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val role = sh.getString("role", "")
        komoditas = intent.getStringExtra("komoditas")!!

        val tvJudulDbKomoditas = findViewById<TextView>(R.id.tvJudulDbKomoditas)
        swlDashboardKomoditas = findViewById(R.id.swlDashboardKomoditas)
        tvTotalLuasLahan = findViewById(R.id.tvTotalLuasLahan)
        tvTotalJumlahLahan = findViewById(R.id.tvTotalJumlahLahan)
        tvTotalLuasLahanMonokultur = findViewById(R.id.tvTotalLuasLahanMonokultur)
        tvTotalJumlahLahanMonokultur = findViewById(R.id.tvTotalJumlahLahanMonokultur)
        tvTotalLuasLahanTumpangsari = findViewById(R.id.tvTotalLuasLahanTumpangsari)
        tvTotalJumlahLahanTumpangsari = findViewById(R.id.tvTotalJumlahLahanTumpangsari)
        tvTotalLuasTanam = findViewById(R.id.tvTotalLuasTanam)
        tvTotalProduksi = findViewById(R.id.tvTotalProduksi)
        tvTotalOwner = findViewById(R.id.tvTotalOwner)
        ivPemilikLahan = findViewById(R.id.ivPemilikLahan)
        ivLahan = findViewById(R.id.ivLahan)
        ivTanaman = findViewById(R.id.ivTanaman)
        ivPerkembangan = findViewById(R.id.ivPerkembangan)
        ivPanen = findViewById(R.id.ivPanen)
        nsvDashboardKomoditas = findViewById(R.id.nsvDashboardKomoditas)
        nsvDashboardKomoditas.visibility = View.GONE

        tvJudulDbKomoditas.text = "DATA KOMODITAS ${komoditas.uppercase()}"

        lifecycleScope.launch {
            loadWilayah()
        }

        swlDashboardKomoditas.setOnRefreshListener {
            lifecycleScope.launch {
                loadOnlineData() // fetch dari API, simpan ke Room
            }
        }

        ivPemilikLahan.setOnClickListener {
            val i = Intent(this@DashboardKomoditas, DataOwner::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        ivLahan.setOnClickListener {
            val i = Intent(this@DashboardKomoditas, DataLahan::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        ivTanaman.setOnClickListener {
            val i = Intent(this@DashboardKomoditas, DataTanaman::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        ivPanen.setOnClickListener {
            val i = Intent(this@DashboardKomoditas, DataPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
    }

    private fun formatAlamat(vararg parts: String?): String {
        return parts.filterNot { it.isNullOrBlank() }.joinToString(", ")
    }

    private suspend fun loadWilayah() {
        var alamat = ""
        val wilayahDao = db.wilayahDao()
        val roleHelper = RoleHelper(this)
        val role = getMyRole(this)

        when (role) {
            in PROV_TYPE -> {
                val provinsi = wilayahDao.getProvinsiById(roleHelper.id).nama
                alamat = "PROVINSI $provinsi"
            }

            in KAB_TYPE -> {
                val kabupaten = wilayahDao.getKabupatenById(roleHelper.id).nama
                alamat = "KABUPATEN $kabupaten"
            }

            in KEC_TYPE -> {
                val kecamatanList = wilayahDao.getDataKecamatanByPolsekId(sh.getInt("satker_id", 0))
                alamat = kecamatanList.joinToString(", ") { "KEC. ${it.nama}" }
            }

            in DESA_TYPE -> {
                val provinsi = "PROV. " + (sh.getString("provinsi_nama", "") ?: "")
                val kabupaten = "KAB. " + (sh.getString("kabupaten_nama", "") ?: "")
                val kecamatan = "KEC. " + (sh.getString("kecamatan_nama", "") ?: "")
                val desa = "DESA " + (sh.getString("desa_nama", "") ?: "")
                alamat = formatAlamat(desa, kecamatan, kabupaten, provinsi)
            }
        }

        findViewById<TextView>(R.id.tvAlamatKomoditas).text = alamat
    }


    private suspend fun loadOnlineData(){
        swlDashboardKomoditas.isRefreshing = true
        nsvDashboardKomoditas.visibility = View.GONE
        try {
            syncDataFromServer(this)
        }catch (e : Exception){
            e.printStackTrace()
        }finally {
            lifecycleScope.launch { 
                loadOfflineData()
            }
        }
    }

    private suspend fun loadOfflineData(){
        val dataOwner = dbOwner.getAllByKomoditas(komoditas).filter { it.status == "VERIFIED" }
        val dataLahan = dbLahan.getAll(komoditas).filter { it.status == "VERIFIED" }
        val dataTanaman = dbTanaman.getAll(komoditas).filter { it.status == "VERIFIED" }
        val dataPanen = dbPanen.getAll(komoditas).filter { it.status == "VERIFIED" }

        withContext(Dispatchers.Main){
            swlDashboardKomoditas.isRefreshing = false
            var totalLuas = 0.0
            dataLahan.forEach {
                totalLuas += (it.luas).toDouble()
            }
            var tLahanMonokultur = 0
            var tLahanTumpangsari = 0
            var tLuasLahanMonokultur = 0.0
            var tLuasLahanTumpangsari = 0.0
            var tLuasTanam = 0.0
            var tProduksi = 0.0

            dataLahan.forEach {
                if (it.type == TypeLahan.MONOKULTUR){
                    tLahanMonokultur++
                    tLuasLahanMonokultur += (it.luas).toDouble()
                }else{
                    tLahanTumpangsari++
                    tLuasLahanTumpangsari += (it.luas).toDouble()
                }
            }

            dataTanaman.forEach {
                tLuasTanam += (it.luastanam).toDouble()
            }

            dataPanen.forEach {
                tProduksi += (it.jumlahpanen).toDouble()
            }

            tvTotalJumlahLahan.text = dataLahan.size.toString()
            tvTotalJumlahLahanMonokultur.text = tLahanMonokultur.toString()
            tvTotalJumlahLahanTumpangsari.text = tLahanTumpangsari.toString()
            tvTotalLuasLahan.text = angkaIndonesia(convertToHektar(totalLuas))
            tvTotalLuasLahanMonokultur.text = angkaIndonesia(convertToHektar(tLuasLahanMonokultur))
            tvTotalLuasLahanTumpangsari.text = angkaIndonesia(convertToHektar(tLuasLahanTumpangsari))
            tvTotalOwner.text = dataOwner.size.toString()
            tvTotalLuasTanam.text = angkaIndonesia(convertToHektar(tLuasTanam))
            tvTotalProduksi.text = angkaIndonesia(convertToTon(tProduksi))
            nsvDashboardKomoditas.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadOnlineData() // fetch dari API, simpan ke Room
        }
    }
}