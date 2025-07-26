package id.creatodidak.kp3k.newversion.dashboard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.newModel.ByEntity.DataLahanWithTanamanAndOwner
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.syncDataFromServer
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.newversion.DataLahan.AddLahan
import id.creatodidak.kp3k.newversion.DataLahan.DraftLahan
import id.creatodidak.kp3k.newversion.DataLahan.RejectedLahan
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory
import id.creatodidak.kp3k.newversion.DataLahan.VerifikasiLahan
import kotlinx.coroutines.launch
import java.util.Locale

class DataLahan : AppCompatActivity() {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")

    private lateinit var db : AppDatabase
    private lateinit var dbLahan : LahanDao
    private lateinit var dbTanaman : TanamanDao
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String

    // Header
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var tvAlamatKomoditas: TextView

    // Total Lahan
    private lateinit var tvTotalJumlahLahan: TextView
    private lateinit var tvTotalLuasLahan: TextView
    private lateinit var tvTotalJumlahLahanTertanamPersen: TextView
    private lateinit var tvTotalLuasLahanTertanamHektar: TextView

    // Monokultur
    private lateinit var tvTotalJumlahLahanMonokultur: TextView
    private lateinit var tvTotalLuasLahanMonokultur: TextView
    private lateinit var tvTotalJumlahLahanTertanamPersenMonokultur: TextView
    private lateinit var tvTotalLuasLahanTertanamHektarMonokultur: TextView

    // Tumpangsari
    private lateinit var tvTotalJumlahLahanTumpangsari: TextView
    private lateinit var tvTotalLuasLahanTumpangsari: TextView
    private lateinit var tvTotalJumlahLahanTertanamPersenTumpangsari: TextView
    private lateinit var tvTotalLuasLahanTertanamHektarTumpangsari: TextView

    private lateinit var tvTotalJumlahLahanPerhutananSosial: TextView
    private lateinit var tvTotalLuasLahanPerhutananSosial: TextView
    private lateinit var tvTotalJumlahLahanTertanamPersenPerhutananSosial: TextView
    private lateinit var tvTotalLuasLahanTertanamHektarPerhutananSosial: TextView

    private lateinit var tvTotalJumlahLahanPbph: TextView
    private lateinit var tvTotalLuasLahanPbph: TextView
    private lateinit var tvTotalJumlahLahanTertanamPersenPbph: TextView
    private lateinit var tvTotalLuasLahanTertanamHektarPbph: TextView

    // CardViews untuk navigasi berdasarkan wilayah
    private lateinit var cvDataLahanByProvinsi: CardView
    private lateinit var cvDataLahanByKabupaten: CardView
    private lateinit var cvDataLahanByKecamatan: CardView
    private lateinit var cvDataLahanByDesa: CardView
    private lateinit var cvDataLahanByOwner: CardView
    private lateinit var cvDataLahanByPolda: CardView
    private lateinit var cvDataLahanByPolres: CardView
    private lateinit var cvDataLahanByPolsek: CardView
    private lateinit var fabAddDataLahan: FloatingActionButton

    private lateinit var svDataLahan : ScrollView
    private lateinit var swlDataLahan: SwipeRefreshLayout

    private lateinit var lyDraft: LinearLayout
    private lateinit var tvDraft: TextView
    private lateinit var lyUnverified: LinearLayout
    private lateinit var tvUnverified: TextView
    private lateinit var lyRejected: LinearLayout
    private lateinit var tvRejected: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_lahan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)
        dbLahan = db.lahanDao()
        dbTanaman = db.tanamanDao()
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()

        swlDataLahan = findViewById(R.id.swlDataLahan)
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        lyDraft = findViewById(R.id.lyDraft)
        tvDraft = findViewById(R.id.tvDraft)
        lyUnverified = findViewById(R.id.lyUnverified)
        tvUnverified = findViewById(R.id.tvUnverified)
        lyRejected = findViewById(R.id.lyRejected)
        tvRejected = findViewById(R.id.tvRejected)

        tvAlamatKomoditas = findViewById(R.id.tvAlamatKomoditas)

        tvTotalJumlahLahan = findViewById(R.id.tvTotalJumlahLahan)
        tvTotalLuasLahan = findViewById(R.id.tvTotalLuasLahan)
        tvTotalJumlahLahanTertanamPersen = findViewById(R.id.tvTotalJumlahLahanTertanamPersen)
        tvTotalLuasLahanTertanamHektar = findViewById(R.id.tvTotalLuasLahanTertanamHektar)

        tvTotalJumlahLahanMonokultur = findViewById(R.id.tvTotalJumlahLahanMonokultur)
        tvTotalLuasLahanMonokultur = findViewById(R.id.tvTotalLuasLahanMonokultur)
        tvTotalJumlahLahanTertanamPersenMonokultur = findViewById(R.id.tvTotalJumlahLahanTertanamPersenMonokultur)
        tvTotalLuasLahanTertanamHektarMonokultur = findViewById(R.id.tvTotalLuasLahanTertanamHektarMonokultur)

        tvTotalJumlahLahanTumpangsari = findViewById(R.id.tvTotalJumlahLahanTumpangsari)
        tvTotalLuasLahanTumpangsari = findViewById(R.id.tvTotalLuasLahanTumpangsari)
        tvTotalJumlahLahanTertanamPersenTumpangsari = findViewById(R.id.tvTotalJumlahLahanTertanamPersenTumpangsari)
        tvTotalLuasLahanTertanamHektarTumpangsari = findViewById(R.id.tvTotalLuasLahanTertanamHektarTumpangsari)

        tvTotalJumlahLahanPerhutananSosial = findViewById(R.id.tvTotalJumlahLahanPerhutananSosial)
        tvTotalLuasLahanPerhutananSosial = findViewById(R.id.tvTotalLuasLahanPerhutananSosial)
        tvTotalJumlahLahanTertanamPersenPerhutananSosial = findViewById(R.id.tvTotalJumlahLahanTertanamPersenPerhutananSosial)
        tvTotalLuasLahanTertanamHektarPerhutananSosial = findViewById(R.id.tvTotalLuasLahanTertanamHektarPerhutananSosial)

        tvTotalJumlahLahanPbph = findViewById(R.id.tvTotalJumlahLahanPbph)
        tvTotalLuasLahanPbph = findViewById(R.id.tvTotalLuasLahanPbph)
        tvTotalJumlahLahanTertanamPersenPbph = findViewById(R.id.tvTotalJumlahLahanTertanamPersenPbph)
        tvTotalLuasLahanTertanamHektarPbph = findViewById(R.id.tvTotalLuasLahanTertanamHektarPbph)

        cvDataLahanByProvinsi = findViewById(R.id.cvDataLahanByProvinsi)
        cvDataLahanByKabupaten = findViewById(R.id.cvDataLahanByKabupaten)
        cvDataLahanByKecamatan = findViewById(R.id.cvDataLahanByKecamatan)
        cvDataLahanByDesa = findViewById(R.id.cvDataLahanByDesa)
        cvDataLahanByOwner = findViewById(R.id.cvDataLahanByOwner)
        cvDataLahanByPolda = findViewById(R.id.cvDataLahanByPolda)
        cvDataLahanByPolres = findViewById(R.id.cvDataLahanByPolres)
        cvDataLahanByPolsek = findViewById(R.id.cvDataLahanByPolsek)

        fabAddDataLahan = findViewById(R.id.fabAddDataLahan)

        cvDataLahanByProvinsi.visibility = View.GONE
        cvDataLahanByKabupaten.visibility = View.GONE
        cvDataLahanByKecamatan.visibility = View.GONE
        cvDataLahanByDesa.visibility = View.GONE
        cvDataLahanByOwner.visibility = View.GONE
        cvDataLahanByPolda.visibility = View.GONE
        cvDataLahanByPolres.visibility = View.GONE
        cvDataLahanByPolsek.visibility = View.GONE

        svDataLahan = findViewById(R.id.svDataLahan)
        svDataLahan.visibility = View.GONE

        when(getMyRole(this)){
            in PROV_TYPE -> {
                cvDataLahanByProvinsi.visibility = View.VISIBLE
                cvDataLahanByKabupaten.visibility = View.VISIBLE
                cvDataLahanByKecamatan.visibility = View.VISIBLE
                cvDataLahanByDesa.visibility = View.VISIBLE
                cvDataLahanByOwner.visibility = View.VISIBLE
                cvDataLahanByPolda.visibility = View.VISIBLE
                cvDataLahanByPolres.visibility = View.VISIBLE
                cvDataLahanByPolsek.visibility = View.VISIBLE
            }
            in KAB_TYPE -> {
                cvDataLahanByKabupaten.visibility = View.VISIBLE
                cvDataLahanByKecamatan.visibility = View.VISIBLE
                cvDataLahanByDesa.visibility = View.VISIBLE
                cvDataLahanByOwner.visibility = View.VISIBLE
                cvDataLahanByPolres.visibility = View.VISIBLE
                cvDataLahanByPolsek.visibility = View.VISIBLE
            }
            in KEC_TYPE -> {
                cvDataLahanByKecamatan.visibility = View.VISIBLE
                cvDataLahanByDesa.visibility = View.VISIBLE
                cvDataLahanByOwner.visibility = View.VISIBLE
                cvDataLahanByPolsek.visibility = View.VISIBLE
            }
            in DESA_TYPE -> {
                cvDataLahanByDesa.visibility = View.VISIBLE
                cvDataLahanByOwner.visibility = View.VISIBLE
            }
        }

        fabAddDataLahan.setOnClickListener {
            val i = Intent(this, AddLahan::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        cvDataLahanByProvinsi.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "provinsi")
            startActivity(i)
        }

        cvDataLahanByKabupaten.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kabupaten")
            startActivity(i)
        }

        cvDataLahanByKecamatan.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kecamatan")
            startActivity(i)
        }

        cvDataLahanByDesa.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "desa")
            startActivity(i)
        }

        cvDataLahanByOwner.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "owner")
            startActivity(i)
        }

        cvDataLahanByPolda.setOnClickListener{
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polda")
            startActivity(i)
        }

        cvDataLahanByPolres.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polres")
            startActivity(i)
        }

        cvDataLahanByPolsek.setOnClickListener {
            val i = Intent(this, ShowDataLahanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polsek")
            startActivity(i)
        }

        swlDataLahan.setOnRefreshListener {
            lifecycleScope.launch {
                loadDataOnline()
            }
        }
    }

    private suspend fun loadDataOnline(){
        try {
            swlDataLahan.isRefreshing = true
            svDataLahan.visibility = View.GONE
            fabAddDataLahan.visibility = View.GONE
            syncDataFromServer(this)
        }catch (e: Exception){
            e.printStackTrace()
        }finally {
            loadWilayah()
        }
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

        lifecycleScope.launch {
            loadDataOffline()
        }

        val draftData = db.draftLahanDao().getOfflineLahan(komoditas)
        val unverifiedData = db.lahanDao().getUnverifiedLahan(komoditas)
        val rejectedData = when(getMyLevel(this)){
            "kecamatan" -> db.lahanDao().getRejectedLahanKecamatan(komoditas, RoleHelper(this).ids)
            "desa" -> db.lahanDao().getRejectedLahanDesa(komoditas, RoleHelper(this).id)
            else -> emptyList()
        }

        if(draftData.isNotEmpty()){
            lyDraft.visibility = View.VISIBLE
            tvDraft.text = "Terdapat ${draftData.size} Data Lahan belum anda kirim ke server, silahkan klik peringatan ini untuk membuka list draft!"
            lyDraft.setOnClickListener {
                val i = Intent(this@DataLahan, DraftLahan::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        }else{
            lyDraft.visibility = View.GONE
        }
        
        if(unverifiedData.isNotEmpty() && getMyRole(this) == "ADMINPOLRES"){
            lyUnverified.visibility = View.VISIBLE
            tvUnverified.text = "Terdapat ${unverifiedData.size} Data Lahan belum diverifikasi, silahkan klik peringatan ini untuk membuka list data!"
            lyUnverified.setOnClickListener {
                val i = Intent(this@DataLahan, VerifikasiLahan::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        }else{
            lyUnverified.visibility = View.GONE
        }
        
        if(rejectedData.isNotEmpty() && getMyRole(this) in listOf("ADMINPOLSEK", "BPKP")){
            lyRejected.visibility = View.VISIBLE
            tvRejected.text = "Terdapat ${rejectedData.size} Data Lahan yang ditolak oleh Admin, silahkan klik peringatan ini untuk membuka list data!"
            lyRejected.setOnClickListener {
                val i = Intent(this@DataLahan, RejectedLahan::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        }else{
            lyRejected.visibility = View.GONE
        }

    }

    private fun formatAlamat(vararg parts: String?): String {
        return parts.filterNot { it.isNullOrBlank() }.joinToString(", ")
    }

    private suspend fun loadDataOffline() {
        val lahanList = dbLahan.getLVerifiedLahan(komoditas)
        val data = lahanList.map {
            DataLahanWithTanamanAndOwner(
                id = it.id,
                type = it.type,
                komoditas = it.komoditas,
                owner_id = it.owner_id,
                provinsi_id = it.provinsi_id,
                kabupaten_id = it.kabupaten_id,
                kecamatan_id = it.kecamatan_id,
                desa_id = it.desa_id,
                luas = it.luas,
                latitude = it.latitude,
                longitude = it.longitude,
                status = it.status,
                alasan = it.alasan,
                createAt = it.createAt,
                updateAt = it.updateAt,
                submitter = it.submitter,
                owner = dbOwner.getOwnerById(it.owner_id),
                realisasitanam = dbTanaman.getTanamanByLahanId(komoditas, it.id)
            )
        }

        val totalLuasLahan = data.sumOf { it.luas.toDouble() }
        val totalLuasLahanMonokultur = data
            .filter { it.type  == TypeLahan.MONOKULTUR }
            .sumOf { it.luas.toDoubleOrNull() ?: 0.0 }
        val totalLuasLahanTumpangsari = data
            .filter { it.type  == TypeLahan.TUMPANGSARI }
            .sumOf { it.luas.toDoubleOrNull() ?: 0.0 }
        val totalLuasLahanPerhutananSosial = data
            .filter { it.type  == TypeLahan.PERHUTANANSOSIAL }
            .sumOf { it.luas.toDoubleOrNull() ?: 0.0 }
        val totalLuasLahanPbph = data
            .filter { it.type  == TypeLahan.PBPH }
            .sumOf { it.luas.toDoubleOrNull() ?: 0.0 }
        
        val totalLuasTertanam = data.sumOf { item ->
            item.realisasitanam?.sumOf { it.luastanam.toDoubleOrNull() ?: 0.0 } ?: 0.0
        }
        val totalLuasTertanamMonokultur = data
            .filter { it.type  == TypeLahan.MONOKULTUR }
            .sumOf { item ->
                item.realisasitanam?.sumOf { it.luastanam.toDoubleOrNull() ?: 0.0 } ?: 0.0
            }
        val totalLuasTertanamTumpangsari = data
            .filter { it.type  == TypeLahan.TUMPANGSARI }
            .sumOf { item ->
                item.realisasitanam?.sumOf { it.luastanam.toDoubleOrNull() ?: 0.0 } ?: 0.0
            }
        val totalLuasTertanamPerhutananSosial = data
            .filter { it.type  == TypeLahan.PERHUTANANSOSIAL }
            .sumOf { item ->
                item.realisasitanam?.sumOf { it.luastanam.toDoubleOrNull() ?: 0.0 } ?: 0.0
            }
        val totalLuasTertanamPbph = data
            .filter { it.type  == TypeLahan.PBPH }
            .sumOf { item ->
                item.realisasitanam?.sumOf { it.luastanam.toDoubleOrNull() ?: 0.0 } ?: 0.0
            }
        val persenLuasTertanam = if (totalLuasLahan > 0) {
            angkaIndonesia(totalLuasTertanam / totalLuasLahan * 100)
        } else {
            0
        }
        val persenLuasTertanamMonokultur = if (totalLuasLahanMonokultur > 0) {
            angkaIndonesia(totalLuasTertanamMonokultur / totalLuasLahanMonokultur * 100)
        } else {
            0
        }
        val persenLuasTertanamTumpangsari = if (totalLuasLahanTumpangsari > 0) {
            angkaIndonesia(totalLuasTertanamTumpangsari / totalLuasLahanTumpangsari * 100)
        } else {
            0
        }
        val persenLuasTertanamPerhutananSosial = if (totalLuasLahanPerhutananSosial > 0) {
            angkaIndonesia(totalLuasTertanamPerhutananSosial / totalLuasLahanPerhutananSosial * 100)
        } else {
            0
        }
        val persenLuasTertanamPbph = if (totalLuasLahanPbph > 0) {
            angkaIndonesia(totalLuasTertanamPbph / totalLuasLahanPbph * 100)
        } else {
            0
        }
        tvTotalJumlahLahan.text = data.size.toString()
        tvTotalLuasLahan.text = angkaIndonesia(convertToHektar(totalLuasLahan))
        tvTotalJumlahLahanTertanamPersen.text = "$persenLuasTertanam%"
        tvTotalLuasLahanTertanamHektar.text = angkaIndonesia(convertToHektar(totalLuasTertanam))
        tvTotalJumlahLahanMonokultur.text = data.filter { it.type  == TypeLahan.MONOKULTUR }.size.toString()
        tvTotalLuasLahanMonokultur.text = angkaIndonesia(convertToHektar(totalLuasLahanMonokultur))
        tvTotalJumlahLahanTertanamPersenMonokultur.text = "$persenLuasTertanamMonokultur%"
        tvTotalLuasLahanTertanamHektarMonokultur.text = angkaIndonesia(convertToHektar(totalLuasTertanamMonokultur))
        tvTotalJumlahLahanTumpangsari.text = data.filter { it.type  == TypeLahan.TUMPANGSARI }.size.toString()
        tvTotalLuasLahanTumpangsari.text = angkaIndonesia(convertToHektar(totalLuasLahanTumpangsari))
        tvTotalJumlahLahanTertanamPersenTumpangsari.text = "$persenLuasTertanamTumpangsari%"
        tvTotalLuasLahanTertanamHektarTumpangsari.text = angkaIndonesia(convertToHektar(totalLuasTertanamTumpangsari))
        tvTotalJumlahLahanPerhutananSosial.text = data.filter { it.type  == TypeLahan.PERHUTANANSOSIAL }.size.toString()
        tvTotalLuasLahanPerhutananSosial.text = angkaIndonesia(convertToHektar(totalLuasLahanPerhutananSosial))
        tvTotalJumlahLahanTertanamPersenPerhutananSosial.text = "$persenLuasTertanamPerhutananSosial%"
        tvTotalLuasLahanTertanamHektarPerhutananSosial.text = angkaIndonesia(convertToHektar(totalLuasTertanamPerhutananSosial))
        tvTotalJumlahLahanPbph.text = data.filter { it.type  == TypeLahan.PBPH }.size.toString()
        tvTotalLuasLahanPbph.text = angkaIndonesia(convertToHektar(totalLuasLahanPbph))
        tvTotalJumlahLahanTertanamPersenPbph.text = "$persenLuasTertanamPbph%"
        tvTotalLuasLahanTertanamHektarPbph.text = angkaIndonesia(convertToHektar(totalLuasTertanamPbph))
        svDataLahan.visibility = View.VISIBLE
        swlDataLahan.isRefreshing = false
        fabAddDataLahan.visibility = View.GONE

        if(isCanCRUD(this)){
            fabAddDataLahan.visibility = View.VISIBLE
        }else{
            fabAddDataLahan.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadDataOnline()
        }
    }
}