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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.database.syncDataFromServer
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.calculateStat
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.enableDragAndSnap
import id.creatodidak.kp3k.helper.getAlamatBerdasarkanRole
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.helper.mapToNewPanenList
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.newversion.DataPanen.AddPanen
import id.creatodidak.kp3k.newversion.DataPanen.DraftPanen
import id.creatodidak.kp3k.newversion.DataPanen.RejectedPanen
import id.creatodidak.kp3k.newversion.DataPanen.ShowDataPanenByCategory
import id.creatodidak.kp3k.newversion.DataPanen.VerifikasiPanen
import kotlinx.coroutines.launch
import java.util.Locale

class DataPanen : AppCompatActivity() {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")

    private lateinit var db : AppDatabase
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String

    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var cvByProvinsi: CardView
    private lateinit var cvByKabupaten: CardView
    private lateinit var cvByKecamatan: CardView
    private lateinit var cvByDesa: CardView
    private lateinit var cvByPolda: CardView
    private lateinit var cvByPolres: CardView
    private lateinit var cvByPolsek: CardView
    private lateinit var cvByLahanOwner: CardView
    private lateinit var cvByLahan: CardView
    private lateinit var cvByTanaman: CardView
    private lateinit var svDataRealisasiPanen: ScrollView
    private lateinit var swlDataRealisasiPanen: SwipeRefreshLayout
    private lateinit var lyDraft: LinearLayout
    private lateinit var tvDraft: TextView
    private lateinit var lyUnverified: LinearLayout
    private lateinit var tvUnverified: TextView
    private lateinit var lyRejected: LinearLayout
    private lateinit var tvRejected: TextView
    private lateinit var fabAddDataPanen: FloatingActionButton

    lateinit var tvTotalJumlahPanen: TextView
    lateinit var tvTotalLuasPanen: TextView
    lateinit var tvTotalTargetPanen: TextView
    lateinit var tvPersenCapaianPanen: TextView

    lateinit var tvTotalJumlahPanenMonokultur: TextView
    lateinit var tvTotalLuasPanenMonokultur: TextView
    lateinit var tvTotalTargetPanenMonokultur: TextView
    lateinit var tvPersenCapaianPanenMonokultur: TextView

    lateinit var tvTotalJumlahPanenTumpangsari: TextView
    lateinit var tvTotalLuasPanenTumpangsari: TextView
    lateinit var tvTotalTargetPanenTumpangsari: TextView
    lateinit var tvPersenCapaianPanenTumpangsari: TextView

    lateinit var tvTotalJumlahPanenPbph: TextView
    lateinit var tvTotalLuasPanenPbph: TextView
    lateinit var tvTotalTargetPanenPbph: TextView
    lateinit var tvPersenCapaianPanenPbph: TextView

    lateinit var tvTotalJumlahPanenPerhutananSosial: TextView
    lateinit var tvTotalLuasPanenPerhutananSosial: TextView
    lateinit var tvTotalTargetPanenPerhutananSosial: TextView
    lateinit var tvPersenCapaianPanenPerhutananSosial: TextView
    lateinit var tvAlamatKomoditas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_panen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        cvByProvinsi = findViewById(R.id.cvDataPanenByProvinsi)
        cvByKabupaten = findViewById(R.id.cvDataPanenByKabupaten)
        cvByKecamatan = findViewById(R.id.cvDataPanenByKecamatan)
        cvByDesa = findViewById(R.id.cvDataPanenByDesa)
        cvByPolda = findViewById(R.id.cvDataPanenByPolda)
        cvByPolres = findViewById(R.id.cvDataPanenByPolres)
        cvByPolsek = findViewById(R.id.cvDataPanenByPolsek)
        cvByLahanOwner = findViewById(R.id.cvDataPanenByLahanOwner)
        cvByLahan = findViewById(R.id.cvDataPanenByLahan)
        cvByTanaman = findViewById(R.id.cvDataPanenByTanaman)
        lyDraft = findViewById(R.id.lyDraft)
        tvDraft = findViewById(R.id.tvDraft)
        lyUnverified = findViewById(R.id.lyUnverified)
        tvUnverified = findViewById(R.id.tvUnverified)
        lyRejected = findViewById(R.id.lyRejected)
        tvRejected = findViewById(R.id.tvRejected)
        fabAddDataPanen = findViewById(R.id.fabAddDataPanen)
        svDataRealisasiPanen = findViewById(R.id.svDataRealisasiPanen)
        swlDataRealisasiPanen = findViewById(R.id.swlDataRealisasiPanen)
        svDataRealisasiPanen.visibility = View.GONE
        tvTotalJumlahPanen = findViewById(R.id.tvTotalJumlahPanen)
        tvTotalLuasPanen = findViewById(R.id.tvTotalLuasPanen)
        tvTotalTargetPanen = findViewById(R.id.tvTotalTargetPanen)
        tvPersenCapaianPanen = findViewById(R.id.tvPersenCapaianPanen)

        tvTotalJumlahPanenMonokultur = findViewById(R.id.tvTotalJumlahPanenMonokultur)
        tvTotalLuasPanenMonokultur = findViewById(R.id.tvTotalLuasPanenMonokultur)
        tvTotalTargetPanenMonokultur = findViewById(R.id.tvTotalTargetPanenMonokultur)
        tvPersenCapaianPanenMonokultur = findViewById(R.id.tvPersenCapaianPanenMonokultur)

        tvTotalJumlahPanenTumpangsari = findViewById(R.id.tvTotalJumlahPanenTumpangsari)
        tvTotalLuasPanenTumpangsari = findViewById(R.id.tvTotalLuasPanenTumpangsari)
        tvTotalTargetPanenTumpangsari = findViewById(R.id.tvTotalTargetPanenTumpangsari)
        tvPersenCapaianPanenTumpangsari = findViewById(R.id.tvPersenCapaianPanenTumpangsari)

        tvTotalJumlahPanenPbph = findViewById(R.id.tvTotalJumlahPanenPbph)
        tvTotalLuasPanenPbph = findViewById(R.id.tvTotalLuasPanenPbph)
        tvTotalTargetPanenPbph = findViewById(R.id.tvTotalTargetPanenPbph)
        tvPersenCapaianPanenPbph = findViewById(R.id.tvPersenCapaianPanenPbph)

        tvTotalJumlahPanenPerhutananSosial = findViewById(R.id.tvTotalJumlahPanenPerhutananSosial)
        tvTotalLuasPanenPerhutananSosial = findViewById(R.id.tvTotalLuasPanenPerhutananSosial)
        tvTotalTargetPanenPerhutananSosial = findViewById(R.id.tvTotalTargetPanenPerhutananSosial)
        tvPersenCapaianPanenPerhutananSosial = findViewById(R.id.tvPersenCapaianPanenPerhutananSosial)

        tvAlamatKomoditas = findViewById(R.id.tvAlamatKomoditas)
        getAlamatBerdasarkanRole(db, sh) { alamat ->
            tvAlamatKomoditas.text = alamat // atau log, atau simpan ke variabel, dll
        }


        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"

        cvByProvinsi.visibility = View.GONE
        cvByKabupaten.visibility = View.GONE
        cvByKecamatan.visibility = View.GONE
        cvByDesa.visibility = View.GONE
        cvByPolda.visibility = View.GONE
        cvByPolres.visibility = View.GONE
        cvByPolsek.visibility = View.GONE

        fabAddDataPanen.enableDragAndSnap()

        fabAddDataPanen.setOnClickListener {
            val i = Intent(this, AddPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        when(getMyRole(this)){
            in PROV_TYPE -> {
                cvByProvinsi.visibility = View.VISIBLE
                cvByKabupaten.visibility = View.VISIBLE
                cvByKecamatan.visibility = View.VISIBLE
                cvByDesa.visibility = View.VISIBLE
                cvByPolda.visibility = View.VISIBLE
                cvByPolres.visibility = View.VISIBLE
                cvByPolsek.visibility = View.VISIBLE
            }
            in KAB_TYPE -> {
                cvByKabupaten.visibility = View.VISIBLE
                cvByKecamatan.visibility = View.VISIBLE
                cvByDesa.visibility = View.VISIBLE
                cvByPolres.visibility = View.VISIBLE
                cvByPolsek.visibility = View.VISIBLE
            }
            in KEC_TYPE -> {
                cvByKecamatan.visibility = View.VISIBLE
                cvByDesa.visibility = View.VISIBLE
                cvByPolsek.visibility = View.VISIBLE
            }
            in DESA_TYPE -> {
                cvByDesa.visibility = View.VISIBLE
            }
        }
        swlDataRealisasiPanen.setOnRefreshListener {
            lifecycleScope.launch {
                loadData()
            }
        }

        cvByProvinsi.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "provinsi")
            startActivity(i)
            startActivity(i)
        }

        cvByKabupaten.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kabupaten")
            startActivity(i)
        }

        cvByKecamatan.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kecamatan")
            startActivity(i)
        }
        cvByDesa.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "desa")
            startActivity(i)
        }
        cvByPolda.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polda")
            startActivity(i)
        }
        cvByPolres.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polres")
            startActivity(i)
        }
        cvByPolsek.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polsek")
            startActivity(i)
        }
        cvByLahanOwner.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "owner")
            startActivity(i)
        }
        cvByLahan.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "lahan")
            startActivity(i)
        }
        cvByTanaman.setOnClickListener {
            val i = Intent(this, ShowDataPanenByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "tanaman")
            startActivity(i)
        }

        lyDraft.setOnClickListener {
            val i = Intent(this, DraftPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
        lyUnverified.setOnClickListener {
            val i = Intent(this, VerifikasiPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
        lyRejected.setOnClickListener {
            val i = Intent(this, RejectedPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
    }

    private suspend fun loadData(){
        swlDataRealisasiPanen.isRefreshing = true
        svDataRealisasiPanen.visibility = View.GONE
        fabAddDataPanen.visibility = View.GONE
        try {
            syncDataFromServer(this)
        }catch (e: Exception){
            showError(this, "Error",e.message.toString())
        }finally {
            loadDataOffline()
        }
    }

    private suspend fun loadDataOffline(){
        lyRejected.visibility = View.GONE
        lyUnverified.visibility = View.GONE
        lyDraft.visibility = View.GONE
        try {
            val myRole = getMyRole(this)
            val myLevel = getMyLevel(this)
            val wilayah = RoleHelper(this)

            val lahan = when (myLevel) {
                "provinsi" -> db.lahanDao().getVerifiedLahanByProvinsi(komoditas, wilayah.id)
                "kabupaten" -> db.lahanDao().getVerifiedLahanByKabupaten(komoditas, wilayah.id)
                "kecamatan" -> db.lahanDao().getVerifiedLahanByKecamatans(komoditas, wilayah.ids)
                "desa" -> db.lahanDao().getVerifiedLahanByDesa(komoditas, wilayah.id)
                else -> db.lahanDao().getLVerifiedLahan(komoditas)
            }

            val lahanIds = lahan.map { it.id }
            val tanamanIds = db.tanamanDao().getTanamanByLahanIds(komoditas, lahanIds)
            val panens = if(tanamanIds.isNullOrEmpty()) emptyList() else db.panenDao().getPanenByTanamanIds(komoditas, tanamanIds.map { it.id })
            val panenRejected = if(panens.isEmpty()) emptyList() else panens.filter { it.status == "REJECTED" }
            val panenUnverified = if (panens.isEmpty()) emptyList() else panens.filter { it.status == "UNVERIFIED" }
            val panenDraft = if(tanamanIds.isNullOrEmpty()) emptyList() else db.draftPanenDao().getPanenDraftByTanamanIds(komoditas, tanamanIds.map { it.id })

            if(panenRejected.isEmpty()){
                lyRejected.visibility = View.GONE
            }else{
                if (myRole in listOf("ADMINPOLSEK", "BPKP")) {
                    val rejected = panenRejected.filter { it.submitter == getMyNrp(this) }
                    if(rejected.isEmpty()){
                        lyRejected.visibility = View.GONE
                    }else{
                        lyRejected.visibility = View.VISIBLE
                        tvRejected.text = "Terdapat ${rejected.size} Data Panen yang ditolak oleh Admin, silahkan klik peringatan ini untuk membuka list data!"
                    }
                }
            }

            if(myRole == "ADMINPOLRES"){
                if(panenUnverified.isEmpty()){
                    lyUnverified.visibility = View.GONE
                }else{
                    lyUnverified.visibility = View.VISIBLE
                    tvUnverified.text = "Terdapat ${panenUnverified.size} Data Panen yang belum diverifikasi oleh Admin, silahkan klik peringatan ini untuk membuka list data!"
                }
            }else{
                lyUnverified.visibility = View.GONE
            }

            if(panenDraft.isEmpty()){
                lyDraft.visibility = View.GONE
            }else{
                lyDraft.visibility = View.VISIBLE
                tvDraft.text = "Terdapat ${panenDraft.size} Data Panen yang belum dikirim ke server, silahkan klik peringatan ini untuk membuka list data!"
            }

            lifecycleScope.launch {
                updateDataCard(panens.filter { it.status == "VERIFIED" })
            }
        } catch (e: Exception) {
            Log.e("loadDataOffline", "Gagal memuat data offline", e)
        }finally {
            svDataRealisasiPanen.visibility = View.VISIBLE
            swlDataRealisasiPanen.isRefreshing = false

            if(isCanCRUD(this)){
                fabAddDataPanen.visibility = View.VISIBLE
            }else{
                fabAddDataPanen.visibility = View.GONE
            }
        }
    }

    private suspend fun updateDataCard(list: List<PanenEntity>?) {
        val newList = mapToNewPanenList(list, db)

        val total = calculateStat(newList)
        val mono = calculateStat(newList.filter { it.lahan?.type == TypeLahan.MONOKULTUR })
        val tumpang = calculateStat(newList.filter { it.lahan?.type == TypeLahan.TUMPANGSARI })
        val pbph = calculateStat(newList.filter { it.lahan?.type == TypeLahan.PBPH })
        val sosial = calculateStat(newList.filter { it.lahan?.type == TypeLahan.PERHUTANANSOSIAL })

        // Set TextView
        tvTotalJumlahPanen.text = total.jumlah
        tvTotalLuasPanen.text = total.luas
        tvTotalTargetPanen.text = total.target
        tvPersenCapaianPanen.text = total.capaian

        tvTotalJumlahPanenMonokultur.text = mono.jumlah
        tvTotalLuasPanenMonokultur.text = mono.luas
        tvTotalTargetPanenMonokultur.text = mono.target
        tvPersenCapaianPanenMonokultur.text = mono.capaian

        tvTotalJumlahPanenTumpangsari.text = tumpang.jumlah
        tvTotalLuasPanenTumpangsari.text = tumpang.luas
        tvTotalTargetPanenTumpangsari.text = tumpang.target
        tvPersenCapaianPanenTumpangsari.text = tumpang.capaian

        tvTotalJumlahPanenPbph.text = pbph.jumlah
        tvTotalLuasPanenPbph.text = pbph.luas
        tvTotalTargetPanenPbph.text = pbph.target
        tvPersenCapaianPanenPbph.text = pbph.capaian

        tvTotalJumlahPanenPerhutananSosial.text = sosial.jumlah
        tvTotalLuasPanenPerhutananSosial.text = sosial.luas
        tvTotalTargetPanenPerhutananSosial.text = sosial.target
        tvPersenCapaianPanenPerhutananSosial.text = sosial.capaian
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadData()
        }
    }}