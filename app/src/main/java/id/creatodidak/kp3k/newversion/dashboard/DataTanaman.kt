package id.creatodidak.kp3k.newversion.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
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
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.database.syncDataFromServer
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.enableDragAndSnap
import id.creatodidak.kp3k.helper.getAlamatBerdasarkanRole
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.newversion.DataTanaman.AddTanaman
import id.creatodidak.kp3k.newversion.DataTanaman.DraftTanaman
import id.creatodidak.kp3k.newversion.DataTanaman.RejectedTanaman
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import id.creatodidak.kp3k.newversion.DataTanaman.VerifikasiTanaman
import kotlinx.coroutines.launch
import java.util.Locale

class DataTanaman : AppCompatActivity() {
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
    private lateinit var tvAlamatKomoditas: TextView

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
    private lateinit var svDataRealisasiTanam: ScrollView
    private lateinit var swlDataRealisasiTanam: SwipeRefreshLayout
    private lateinit var lyDraft: LinearLayout
    private lateinit var tvDraft: TextView
    private lateinit var lyUnverified: LinearLayout
    private lateinit var tvUnverified: TextView
    private lateinit var lyRejected: LinearLayout
    private lateinit var tvRejected: TextView
    private lateinit var fabAddDataTanaman: FloatingActionButton

    private lateinit var tvTotalJumlahTanaman: TextView
    private lateinit var tvTotalLuasTanaman: TextView

    private lateinit var tvTotalJumlahTanamanMonokultur: TextView
    private lateinit var tvTotalLuasTanamanMonokultur: TextView

    private lateinit var tvTotalJumlahTanamanTumpangsari: TextView
    private lateinit var tvTotalLuasTanamanTumpangsari: TextView

    private lateinit var tvTotalJumlahTanamanPbph: TextView
    private lateinit var tvTotalLuasTanamanPbph: TextView

    private lateinit var tvTotalJumlahTanamanPerhutananSosial: TextView
    private lateinit var tvTotalLuasTanamanPerhutananSosial: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_tanaman)
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
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        cvByProvinsi = findViewById(R.id.cvDataTanamanByProvinsi)
        cvByKabupaten = findViewById(R.id.cvDataTanamanByKabupaten)
        cvByKecamatan = findViewById(R.id.cvDataTanamanByKecamatan)
        cvByDesa = findViewById(R.id.cvDataTanamanByDesa)
        cvByPolda = findViewById(R.id.cvDataTanamanByPolda)
        cvByPolres = findViewById(R.id.cvDataTanamanByPolres)
        cvByPolsek = findViewById(R.id.cvDataTanamanByPolsek)
        cvByLahanOwner = findViewById(R.id.cvDataTanamanByLahanOwner)
        cvByLahan = findViewById(R.id.cvDataTanamanByLahan)
        lyDraft = findViewById(R.id.lyDraft)
        tvDraft = findViewById(R.id.tvDraft)
        lyUnverified = findViewById(R.id.lyUnverified)
        tvUnverified = findViewById(R.id.tvUnverified)
        lyRejected = findViewById(R.id.lyRejected)
        tvRejected = findViewById(R.id.tvRejected)
        fabAddDataTanaman = findViewById(R.id.fabAddDataTanaman)
        svDataRealisasiTanam = findViewById(R.id.svDataRealisasiTanam)
        swlDataRealisasiTanam = findViewById(R.id.swlDataRealisasiTanam)
        svDataRealisasiTanam.visibility = View.GONE
        tvAlamatKomoditas = findViewById(R.id.tvAlamatKomoditas)

        tvTotalJumlahTanaman = findViewById(R.id.tvTotalJumlahTanaman)
        tvTotalLuasTanaman = findViewById(R.id.tvTotalLuasTanaman)

        tvTotalJumlahTanamanMonokultur = findViewById(R.id.tvTotalJumlahTanamanMonokultur)
        tvTotalLuasTanamanMonokultur = findViewById(R.id.tvTotalLuasTanamanMonokultur)

        tvTotalJumlahTanamanTumpangsari = findViewById(R.id.tvTotalJumlahTanamanTumpangsari)
        tvTotalLuasTanamanTumpangsari = findViewById(R.id.tvTotalLuasTanamanTumpangsari)

        tvTotalJumlahTanamanPbph = findViewById(R.id.tvTotalJumlahTanamanPbph)
        tvTotalLuasTanamanPbph = findViewById(R.id.tvTotalLuasTanamanPbph)

        tvTotalJumlahTanamanPerhutananSosial = findViewById(R.id.tvTotalJumlahTanamanPerhutananSosial)
        tvTotalLuasTanamanPerhutananSosial = findViewById(R.id.tvTotalLuasTanamanPerhutananSosial)
        
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"

        getAlamatBerdasarkanRole(db, sh) { alamat ->
            tvAlamatKomoditas.text = alamat // atau log, atau simpan ke variabel, dll
        }

        cvByProvinsi.visibility = View.GONE
        cvByKabupaten.visibility = View.GONE
        cvByKecamatan.visibility = View.GONE
        cvByDesa.visibility = View.GONE
        cvByPolda.visibility = View.GONE
        cvByPolres.visibility = View.GONE
        cvByPolsek.visibility = View.GONE

        fabAddDataTanaman.enableDragAndSnap()

        fabAddDataTanaman.setOnClickListener {
            val i = Intent(this, AddTanaman::class.java)
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
        swlDataRealisasiTanam.setOnRefreshListener {
            lifecycleScope.launch {
                loadData()
            }
        }

        cvByProvinsi.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "provinsi")
            startActivity(i)
            startActivity(i)
        }

        cvByKabupaten.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kabupaten")
            startActivity(i)
        }

        cvByKecamatan.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "kecamatan")
            startActivity(i)
        }
        cvByDesa.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "desa")
            startActivity(i)
        }
        cvByPolda.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polda")
            startActivity(i)
        }
        cvByPolres.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polres")
            startActivity(i)
        }
        cvByPolsek.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "polsek")
            startActivity(i)
        }
        cvByLahanOwner.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "owner")
            startActivity(i)
        }
        cvByLahan.setOnClickListener {
            val i = Intent(this, ShowDataTanamanByCategory::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("kategori", "lahan")
            startActivity(i)
        }
        lyDraft.setOnClickListener {
            val i = Intent(this, DraftTanaman::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
        lyUnverified.setOnClickListener {
            val i = Intent(this, VerifikasiTanaman::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
        lyRejected.setOnClickListener {
            val i = Intent(this, RejectedTanaman::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }
    }

    private suspend fun loadData(){
        swlDataRealisasiTanam.isRefreshing = true
        svDataRealisasiTanam.visibility = View.GONE
        fabAddDataTanaman.visibility = View.GONE
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
                "provinsi" -> dbLahan.getVerifiedLahanByProvinsi(komoditas, wilayah.id)
                "kabupaten" -> dbLahan.getVerifiedLahanByKabupaten(komoditas, wilayah.id)
                "kecamatan" -> dbLahan.getVerifiedLahanByKecamatans(komoditas, wilayah.ids)
                "desa" -> dbLahan.getVerifiedLahanByDesa(komoditas, wilayah.id)
                else -> dbLahan.getLVerifiedLahan(komoditas)
            }

            val lahanIds = lahan.map { it.id }
            val tanamanOnline = dbTanaman.getTanamanByLahanIds(komoditas, lahanIds)
            val tanamanOffline = db.draftTanamanDao().getTanamanByLahanIds(komoditas, lahanIds)

            if (tanamanOnline.isNullOrEmpty()) {
                lyRejected.visibility = View.GONE
                lyUnverified.visibility = View.GONE
            } else {
                if (myRole in listOf("ADMINPOLSEK", "BPKP")) {
                    val rejected = tanamanOnline.count { it.status == "REJECTED" }
                    if(rejected > 0){
                        lyRejected.visibility = View.VISIBLE
                        tvRejected.text = "Terdapat $rejected Data Realisasi Tanam yang ditolak oleh Admin, silahkan klik peringatan ini untuk membuka list data!"
                    }else{
                        lyRejected.visibility = View.GONE
                    }
                }

                if (myRole == "ADMINPOLRES") {
                    val unverified = tanamanOnline.count { it.status == "UNVERIFIED" }
                    if(unverified > 0) {
                        lyUnverified.visibility = View.VISIBLE
                        tvUnverified.text =
                            "Terdapat $unverified Data Realisasi Tanam yang belum diverifikasi, silahkan klik peringatan ini untuk membuka list data!"
                    }else{
                        lyUnverified.visibility = View.GONE
                    }
                }
            }

            if (tanamanOffline.isNullOrEmpty()) {
                lyDraft.visibility = View.GONE
            } else {
                lyDraft.visibility = View.VISIBLE
                tvDraft.text = "Terdapat ${tanamanOffline.size} Data Realisasi Tanam yang belum dikirim ke server, silahkan klik peringatan ini untuk membuka list data!"
            }

            lifecycleScope.launch {
                updateDataCard(tanamanOnline?.filter { it.status == "VERIFIED" })
            }

        } catch (e: Exception) {
            Log.e("loadDataOffline", "Gagal memuat data offline", e)
        }finally {
            svDataRealisasiTanam.visibility = View.VISIBLE
            swlDataRealisasiTanam.isRefreshing = false

            if(isCanCRUD(this)){
                fabAddDataTanaman.visibility = View.VISIBLE
            }else{
                fabAddDataTanaman.visibility = View.GONE
            }
        }
    }

    private suspend fun updateDataCard(list: List<TanamanEntity>?){
        val newList = mutableListOf<NewTanamanEntity>()

        if(!list.isNullOrEmpty()){
            val groupedByMasaTanam = list.groupBy { it.masatanam }.toSortedMap()
            groupedByMasaTanam.forEach { (masaTanamKe, tanamanList) ->
                tanamanList.forEachIndexed { index, it ->
                    val lahan = db.lahanDao().getLahanById(it.lahan_id)
                    val dataPanen = db.panenDao().getPanenByTanamanId(it.id)
                    val jumlahPanen = if (dataPanen.isNullOrEmpty()) 0.0 else dataPanen.sumOf {
                        it.jumlahpanen.toDoubleOrNull() ?: 0.0
                    }
                    newList.add(
                        NewTanamanEntity(
                            it.id,
                            0,
                            "TANAMAN KE-${it.tanamanke} MASA TANAM KE-$masaTanamKe",
                            "",
                            lahan.type,
                            "0",
                            jumlahPanen.toString(),
                            it.masatanam,
                            it.luastanam,
                            it.tanggaltanam,
                            it.prediksipanen,
                            it.rencanatanggalpanen,
                            it.komoditas,
                            it.varietas,
                            it.sumber,
                            it.keteranganSumber,
                            it.foto1,
                            it.foto2,
                            it.foto3,
                            it.foto4,
                            it.status,
                            it.alasan,
                            it.createAt,
                            it.updateAt,
                            it.submitter,
                            it.tanamanke
                        )
                    )
                }
            }
        }

        val totalTanaman = newList.size
        val totalLuasTanaman = angkaIndonesia(convertToHektar(newList.sumOf { it.luastanam.toDouble() }))
        val totalTanamanMonokultur = newList.filter { it.typeLahan == TypeLahan.MONOKULTUR }.size
        val totalLuasTanamanMonokultur = angkaIndonesia(convertToHektar(newList.filter { it.typeLahan == TypeLahan.MONOKULTUR }.sumOf { it.luastanam.toDouble() }))
        val totalTanamanTumpangsari = newList.filter { it.typeLahan == TypeLahan.TUMPANGSARI }.size
        val totalLuasTanamanTumpangsari = angkaIndonesia(convertToHektar(newList.filter { it.typeLahan == TypeLahan.TUMPANGSARI }.sumOf { it.luastanam.toDouble() }))
        val totalTanamanPbph = newList.filter { it.typeLahan == TypeLahan.PBPH }.size
        val totalLuasTanamanPbph = angkaIndonesia(convertToHektar(newList.filter { it.typeLahan == TypeLahan.PBPH }.sumOf { it.luastanam.toDouble() }))
        val totalTanamanPerhutananSosial = newList.filter { it.typeLahan == TypeLahan.PERHUTANANSOSIAL }.size
        val totalLuasTanamanPerhutananSosial = angkaIndonesia(convertToHektar(newList.filter { it.typeLahan == TypeLahan.PERHUTANANSOSIAL }.sumOf { it.luastanam.toDouble() }))


        tvTotalJumlahTanaman.text = totalTanaman.toString()
        tvTotalLuasTanaman.text = totalLuasTanaman
        tvTotalJumlahTanamanMonokultur.text = totalTanamanMonokultur.toString()
        tvTotalLuasTanamanMonokultur.text = totalLuasTanamanMonokultur
        tvTotalJumlahTanamanTumpangsari.text = totalTanamanTumpangsari.toString()
        tvTotalLuasTanamanTumpangsari.text = totalLuasTanamanTumpangsari
        tvTotalJumlahTanamanPbph.text = totalTanamanPbph.toString()
        tvTotalLuasTanamanPbph.text = totalLuasTanamanPbph
        tvTotalJumlahTanamanPerhutananSosial.text = totalTanamanPerhutananSosial.toString()
        tvTotalLuasTanamanPerhutananSosial.text = totalLuasTanamanPerhutananSosial
    }
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadData()
        }
    }
}