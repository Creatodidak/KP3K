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
    private lateinit var tvTotalLuasLahanPerhutananSosial: TextView
    private lateinit var tvTotalJumlahLahanPerhutananSosial: TextView
    private lateinit var tvTotalLuasLahanPbph: TextView
    private lateinit var tvTotalJumlahLahanPbph: TextView
    private lateinit var tvTotalLuasTanam : TextView
    private lateinit var tvTotalJumlahTanam : TextView
    private lateinit var tvTotalLuasTanamMonokultur: TextView
    private lateinit var tvTotalJumlahTanamMonokultur: TextView
    private lateinit var tvTotalLuasTanamTumpangsari: TextView
    private lateinit var tvTotalJumlahTanamTumpangsari: TextView
    private lateinit var tvTotalLuasTanamPerhutananSosial: TextView
    private lateinit var tvTotalJumlahTanamPerhutananSosial: TextView
    private lateinit var tvTotalLuasTanamPbph: TextView
    private lateinit var tvTotalJumlahTanamPbph: TextView
    private lateinit var tvTotalJumlahPanen: TextView
    private lateinit var tvTotalHasilPanen: TextView
    private lateinit var tvTotalJumlahPanenMonokultur: TextView
    private lateinit var tvTotalHasilPanenMonokultur: TextView
    private lateinit var tvTotalJumlahPanenTumpangsari: TextView
    private lateinit var tvTotalHasilPanenTumpangsari: TextView
    private lateinit var tvTotalJumlahPanenPbph: TextView
    private lateinit var tvTotalHasilPanenPbph: TextView
    private lateinit var tvTotalJumlahPanenPerhutananSosial: TextView
    private lateinit var tvTotalHasilPanenPerhutananSosial: TextView
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
        tvTotalLuasLahanPerhutananSosial = findViewById(R.id.tvTotalLuasLahanPerhutananSosial)
        tvTotalJumlahLahanPerhutananSosial = findViewById(R.id.tvTotalJumlahLahanPerhutananSosial)
        tvTotalLuasLahanPbph = findViewById(R.id.tvTotalLuasLahanPbph)
        tvTotalJumlahLahanPbph = findViewById(R.id.tvTotalJumlahLahanPbph)

        tvTotalLuasTanam = findViewById(R.id.tvTotalLuasTanam)
        tvTotalJumlahTanam = findViewById(R.id.tvTotalJumlahTanam)
        tvTotalLuasTanamMonokultur = findViewById(R.id.tvTotalLuasTanamMonokultur)
        tvTotalJumlahTanamMonokultur = findViewById(R.id.tvTotalJumlahTanamMonokultur)
        tvTotalLuasTanamTumpangsari = findViewById(R.id.tvTotalLuasTanamTumpangsari)
        tvTotalJumlahTanamTumpangsari = findViewById(R.id.tvTotalJumlahTanamTumpangsari)
        tvTotalLuasTanamPerhutananSosial = findViewById(R.id.tvTotalLuasTanamPerhutananSosial)
        tvTotalJumlahTanamPerhutananSosial = findViewById(R.id.tvTotalJumlahTanamPerhutananSosial)
        tvTotalLuasTanamPbph = findViewById(R.id.tvTotalLuasTanamPbph)
        tvTotalJumlahTanamPbph = findViewById(R.id.tvTotalJumlahTanamPbph)

        tvTotalJumlahPanen = findViewById(R.id.tvTotalJumlahPanen)
        tvTotalHasilPanen = findViewById(R.id.tvTotalHasilPanen)
        tvTotalJumlahPanenMonokultur = findViewById(R.id.tvTotalJumlahPanenMonokultur)
        tvTotalHasilPanenMonokultur = findViewById(R.id.tvTotalHasilPanenMonokultur)
        tvTotalJumlahPanenTumpangsari = findViewById(R.id.tvTotalJumlahPanenTumpangsari)
        tvTotalHasilPanenTumpangsari = findViewById(R.id.tvTotalHasilPanenTumpangsari)
        tvTotalJumlahPanenPbph = findViewById(R.id.tvTotalJumlahPanenPbph)
        tvTotalHasilPanenPbph = findViewById(R.id.tvTotalHasilPanenPbph)
        tvTotalJumlahPanenPerhutananSosial = findViewById(R.id.tvTotalJumlahPanenPerhutananSosial)
        tvTotalHasilPanenPerhutananSosial = findViewById(R.id.tvTotalHasilPanenPerhutananSosial)

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

        ivPerkembangan.setOnClickListener {
            val i = Intent(this@DashboardKomoditas, DataPerkembangan::class.java)
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
            var tLahanPbph = 0
            var tLahanPerhutananSosial = 0
            var tLuasLahanMonokultur = 0.0
            var tLuasLahanTumpangsari = 0.0
            var tLuasLahanPbph = 0.0
            var tLuasLahanPerhutananSosial = 0.0

            dataLahan.forEach {
                when(it.type.name){
                    TypeLahan.MONOKULTUR.name -> {
                        tLahanMonokultur++
                        tLuasLahanMonokultur += (it.luas).toDouble()
                    }
                    TypeLahan.TUMPANGSARI.name -> {
                        tLahanTumpangsari++
                        tLuasLahanTumpangsari += (it.luas).toDouble()
                    }
                    TypeLahan.PBPH.name -> {
                        tLahanPbph++
                        tLuasLahanPbph += (it.luas).toDouble()
                    }
                    TypeLahan.PERHUTANANSOSIAL.name -> {
                        tLahanPerhutananSosial++
                        tLuasLahanPerhutananSosial += (it.luas).toDouble()
                    }
                }
            }

            tvTotalJumlahLahan.text = dataLahan.size.toString()
            tvTotalJumlahLahanMonokultur.text = tLahanMonokultur.toString()
            tvTotalJumlahLahanTumpangsari.text = tLahanTumpangsari.toString()
            tvTotalJumlahLahanPbph.text = tLahanPbph.toString()
            tvTotalJumlahLahanPerhutananSosial.text = tLahanPerhutananSosial.toString()
            tvTotalLuasLahan.text = angkaIndonesia(convertToHektar(totalLuas))
            tvTotalLuasLahanMonokultur.text = angkaIndonesia(convertToHektar(tLuasLahanMonokultur))
            tvTotalLuasLahanTumpangsari.text = angkaIndonesia(convertToHektar(tLuasLahanTumpangsari))
            tvTotalLuasLahanPbph.text = angkaIndonesia(convertToHektar(tLuasLahanPbph))
            tvTotalLuasLahanPerhutananSosial.text = angkaIndonesia(convertToHektar(tLuasLahanPerhutananSosial))

            var tTanam = 0
            var tTanamMonokultur = 0
            var tTanamTumpangsari = 0
            var tTanamPbph = 0
            var tTanamPerhutananSosial = 0
            var tLuasTanam = 0.0
            var tLuasTanamMonokultur = 0.0
            var tLuasTanamTumpangsari = 0.0
            var tLuasTanamPbph = 0.0
            var tLuasTanamPerhutananSosial = 0.0

            dataTanaman.forEach { t ->
                tTanam++
                tLuasTanam += (t.luastanam).toDouble()
                val lahan = dataLahan.find { it.id == t.lahan_id }
                when(lahan?.type?.name){
                    TypeLahan.MONOKULTUR.name -> {
                        tTanamMonokultur++
                        tLuasTanamMonokultur += (t.luastanam).toDouble()
                    }
                    TypeLahan.TUMPANGSARI.name -> {
                        tTanamTumpangsari++
                        tLuasTanamTumpangsari += (t.luastanam).toDouble()
                    }
                    TypeLahan.PBPH.name -> {
                        tTanamPbph++
                        tLuasTanamPbph += (t.luastanam).toDouble()
                    }
                    TypeLahan.PERHUTANANSOSIAL.name -> {
                        tTanamPerhutananSosial++
                        tLuasTanamPerhutananSosial += (t.luastanam).toDouble()
                    }
                }
            }

            tvTotalJumlahTanam.text = tTanam.toString()
            tvTotalJumlahTanamMonokultur.text = tTanamMonokultur.toString()
            tvTotalJumlahTanamTumpangsari.text = tTanamTumpangsari.toString()
            tvTotalJumlahTanamPbph.text = tTanamPbph.toString()
            tvTotalJumlahTanamPerhutananSosial.text = tTanamPerhutananSosial.toString()
            tvTotalLuasTanam.text = angkaIndonesia(convertToHektar(tLuasTanam))
            tvTotalLuasTanamMonokultur.text = angkaIndonesia(convertToHektar(tLuasTanamMonokultur))
            tvTotalLuasTanamTumpangsari.text = angkaIndonesia(convertToHektar(tLuasTanamTumpangsari))
            tvTotalLuasTanamPbph.text = angkaIndonesia(convertToHektar(tLuasTanamPbph))
            tvTotalLuasTanamPerhutananSosial.text = angkaIndonesia(convertToHektar(tLuasTanamPerhutananSosial))

            var tPanen = 0
            var tPanenMonokultur = 0
            var tPanenTumpangsari = 0
            var tPanenPbph = 0
            var tPanenPerhutananSosial = 0
            var tHasilPanen = 0.0
            var tHasilPanenMonokultur = 0.0
            var tHasilPanenTumpangsari = 0.0
            var tHasilPanenPbph = 0.0
            var tHasilPanenPerhutananSosial = 0.0

            dataPanen.forEach { t ->
                tPanen++
                tHasilPanen += (t.jumlahpanen).toDouble()
                val tanaman = dataTanaman.find { it.id == t.tanaman_id }
                val lahan = dataLahan.find { it.id == tanaman?.lahan_id }
                when(lahan?.type?.name){
                    TypeLahan.MONOKULTUR.name -> {
                        tPanenMonokultur++
                        tHasilPanenMonokultur += (t.jumlahpanen).toDouble()
                    }
                    TypeLahan.TUMPANGSARI.name -> {
                        tPanenTumpangsari++
                        tHasilPanenTumpangsari += (t.jumlahpanen).toDouble()
                    }
                    TypeLahan.PBPH.name -> {
                        tPanenPbph++
                        tHasilPanenPbph += (t.jumlahpanen).toDouble()
                    }
                    TypeLahan.PERHUTANANSOSIAL.name -> {
                        tPanenPerhutananSosial++
                        tHasilPanenPerhutananSosial += (t.jumlahpanen).toDouble()
                    }
                }
            }

            tvTotalJumlahPanen.text = tPanen.toString()
            tvTotalJumlahPanenMonokultur.text = tPanenMonokultur.toString()
            tvTotalJumlahPanenTumpangsari.text = tPanenTumpangsari.toString()
            tvTotalJumlahPanenPbph.text = tPanenPbph.toString()
            tvTotalJumlahPanenPerhutananSosial.text = tPanenPerhutananSosial.toString()
            tvTotalHasilPanen.text = angkaIndonesia(convertToTon(tHasilPanen))
            tvTotalHasilPanenMonokultur.text = angkaIndonesia(convertToTon(tHasilPanenMonokultur))
            tvTotalHasilPanenTumpangsari.text = angkaIndonesia(convertToTon(tHasilPanenTumpangsari))
            tvTotalHasilPanenPbph.text = angkaIndonesia(convertToTon(tHasilPanenPbph))
            tvTotalHasilPanenPerhutananSosial.text = angkaIndonesia(convertToTon(tHasilPanenPerhutananSosial))

            tvTotalOwner.text = dataOwner.size.toString()


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