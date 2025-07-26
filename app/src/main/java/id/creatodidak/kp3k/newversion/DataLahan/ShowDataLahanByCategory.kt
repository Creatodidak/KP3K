package id.creatodidak.kp3k.newversion.DataLahan

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.PrimaryKey
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.ListLahanAdapter
import id.creatodidak.kp3k.api.newModel.ByEntity.DataLahanWithTanamanAndOwner
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.DesaEntity
import id.creatodidak.kp3k.database.Entity.KabupatenEntity
import id.creatodidak.kp3k.database.Entity.KecamatanEntity
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.ProvinsiEntity
import id.creatodidak.kp3k.database.Entity.SatkerEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.enableDragAndSnap
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.getMySatkerId
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.newversion.Maps.ShowSingleDataOnMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date
import kotlin.collections.map

class ShowDataLahanByCategory : AppCompatActivity() {
    data class NewLahanEntity(
        val id: Int,
        val type: TypeLahan,
        val komoditas: String,
        val showCaseName: String,
        val owner_id: Int,
        val owner_name: String,
        val owner_pok: String,
        val owner_type: String,
        val provinsi_id: Int,
        val provinsi: String,
        val kabupaten_id: Int,
        val kabupaten: String,
        val kecamatan_id: Int,
        val kecamatan: String,
        val desa_id: Int,
        val desa: String,
        val luas: String,
        val latitude: String,
        val longitude: String,
        val status: String = "VERIFIED",
        val alasan: String?,
        val createAt: Date = Date(),
        val updateAt: Date = Date(),
        val submitter: String,
        val lahanke: String,
    ){
        override fun toString(): String = showCaseName
    }
    private lateinit var db : AppDatabase
    private lateinit var dbLahan : LahanDao
    private lateinit var dbTanaman : TanamanDao
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String
    private lateinit var kategori : String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var tvDataFilter: TextView
    private lateinit var spDataFilter: Spinner
    private lateinit var etSearchLayout: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvData: RecyclerView
    private lateinit var tvTotalData: TextView
    private lateinit var fabAddData: FloatingActionButton
    private lateinit var fabDownloadData: FloatingActionButton
    private lateinit var lyFilterBy: LinearLayout
    private lateinit var spFilterBy: Spinner
    private lateinit var lyFab: LinearLayout
    private lateinit var allCard: LinearLayout
    private lateinit var allCardCategory: LinearLayout
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

    private var defProv = mutableListOf<ProvinsiEntity>()
    private var defKab = mutableListOf<KabupatenEntity>()
    private var defKec = mutableListOf<KecamatanEntity>()
    private var defDesa = mutableListOf<DesaEntity>()
    private var defPol = mutableListOf<SatkerEntity>()
    private var defOwner = mutableListOf<OwnerEntity>()

    private var listLahan = mutableListOf<NewLahanEntity>()
    private var filteredListLahan = mutableListOf<NewLahanEntity>()

    private lateinit var provAdapter: ArrayAdapter<ProvinsiEntity>
    private lateinit var kabAdapter: ArrayAdapter<KabupatenEntity>
    private lateinit var kecAdapter: ArrayAdapter<KecamatanEntity>
    private lateinit var desaAdapter: ArrayAdapter<DesaEntity>
    private lateinit var polAdapter: ArrayAdapter<SatkerEntity>
    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>

    private lateinit var lahanAdapter : ListLahanAdapter

    private var defId : Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_data_lahan_by_category)
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
        kategori = intent.getStringExtra("kategori").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text =
            "pada Komoditas ${komoditas.capitalize()} berdasarkan ${kategori.capitalize()}"
        tvDataFilter = findViewById(R.id.tvDataFilter)
        spDataFilter = findViewById(R.id.spDataFilter)
        tvDataFilter.text = "Filter berdasarkan ${kategori.capitalize()}"
        etSearchLayout = findViewById(R.id.etSearchLayout)
        etSearch = findViewById(R.id.etSearch)
        rvData = findViewById(R.id.rvData)
        tvTotalData = findViewById(R.id.tvTotalData)
        fabAddData = findViewById(R.id.fabAddData)
        fabDownloadData = findViewById(R.id.fabDownloadData)
        lyFilterBy = findViewById(R.id.lyFilterBy)
        spFilterBy = findViewById(R.id.spFilterBy)
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

        allCard = findViewById(R.id.allCard)
        allCardCategory = findViewById(R.id.allCardCategory)
        allCard.visibility = View.GONE

        lyFab = findViewById(R.id.lyFab)
        lyFab.enableDragAndSnap()

        val jenisLahanList = listOf("SEMUA", "MONOKULTUR", "TUMPANGSARI", "PBPH", "PERHUTANAN SOSIAL")
        val adapter = ArrayAdapter(this@ShowDataLahanByCategory, android.R.layout.simple_spinner_item, jenisLahanList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterBy.adapter = adapter
        lyFilterBy.visibility = View.GONE

        provAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defProv)
        provAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kabAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKab)
        kabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kecAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKec)
        kecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        desaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defDesa)
        desaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        polAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defPol)
        polAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defOwner)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        etSearchLayout.visibility = View.GONE

        spDataFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
                    lyFilterBy.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        when (kategori) {
                            "provinsi" -> {
                                val id = defProv[position].id
                                defId = id
                                loadData(id)
                            }

                            "kabupaten" -> {
                                val id = defKab[position].id
                                defId = id
                                loadData(id)
                            }

                            "kecamatan" -> {
                                val id = defKec[position].id
                                defId = id
                                loadData(id)
                            }

                            "desa" -> {
                                val id = defDesa[position].id
                                defId = id
                                loadData(id)
                            }

                            in listOf("polda", "polres", "polsek") -> {
                                val id = defPol[position].id
                                defId = id
                                loadData(id)
                            }

                            "owner" -> {
                                val id = defOwner[position].id
                                defId = id
                                loadData(id)
                            }
                        }
                    }
                }else{
                    lyFilterBy.visibility = View.GONE
                    spFilterBy.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        spFilterBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> {
                        filteredListLahan.clear()
                        lahanAdapter.notifyDataSetChanged()
                        filteredListLahan.addAll(listLahan)
                        lahanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
                    }
                    1 -> {
                        lyFilterBy.visibility = View.VISIBLE
                        filteredListLahan.clear()
                        lahanAdapter.notifyDataSetChanged()
                        filteredListLahan.addAll(listLahan.filter { it.type == TypeLahan.MONOKULTUR })
                        lahanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
                    }
                    2 -> {
                        lyFilterBy.visibility = View.VISIBLE
                        filteredListLahan.clear()
                        lahanAdapter.notifyDataSetChanged()
                        filteredListLahan.addAll(listLahan.filter { it.type == TypeLahan.TUMPANGSARI })
                        lahanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
                    }
                    3 -> {
                        lyFilterBy.visibility = View.VISIBLE
                        filteredListLahan.clear()
                        lahanAdapter.notifyDataSetChanged()
                        filteredListLahan.addAll(listLahan.filter { it.type == TypeLahan.PBPH })
                        lahanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
                    }
                    4 -> {
                        lyFilterBy.visibility = View.VISIBLE
                        filteredListLahan.clear()
                        lahanAdapter.notifyDataSetChanged()
                        filteredListLahan.addAll(listLahan.filter { it.type == TypeLahan.PERHUTANANSOSIAL })
                        lahanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
                    }
                }
                lifecycleScope.launch { updateDataCard(filteredListLahan) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        lahanAdapter = ListLahanAdapter(
            filteredListLahan,
            onRincianClick = { data ->
                val i = Intent(this, DataLahanDetails::class.java)
                i.putExtra("komoditas", komoditas)
                i.putExtra("id", data.id.toString())
                i.putExtra("status", data.status)
                i.putExtra("kategori", kategori)
                startActivity(i)
            },
            onMapsClick = { data ->
                val i = Intent(this, ShowSingleDataOnMap::class.java)
                i.putExtra("latitude", data.latitude)
                i.putExtra("longitude", data.longitude)
                i.putExtra("komoditas", komoditas)
                i.putExtra("judul", data.showCaseName)
                startActivity(i)
            }
        )

        rvData.adapter = lahanAdapter
        rvData.layoutManager = LinearLayoutManager(this)
        etSearch.addTextChangedListener { editable ->
            val keyword = editable.toString().trim().lowercase()
            val keys = spFilterBy.selectedItem.toString()
            filteredListLahan.clear()

            if (keyword.isEmpty()) {
                if(keys == "SEMUA"){
                    filteredListLahan.addAll(listLahan)
                }else{
                    filteredListLahan.addAll(
                        listLahan.filter { it.type == TypeLahan.valueOf(keys) }
                    )
                }
            } else {
                if(keys == "SEMUA"){
                    filteredListLahan.addAll(
                        listLahan.filter {
                            it.showCaseName.lowercase().contains(keyword) ||
                                    it.owner_name.lowercase().contains(keyword) ||
                                    it.kabupaten.lowercase().contains(keyword) ||
                                    it.kecamatan.lowercase().contains(keyword) ||
                                    it.desa.lowercase().contains(keyword)
                        }
                    )
                }else{
                    filteredListLahan.addAll(
                        listLahan.filter { it.type == TypeLahan.valueOf(keys) }.filter {
                            it.showCaseName.lowercase().contains(keyword) ||
                                    it.owner_name.lowercase().contains(keyword) ||
                                    it.kabupaten.lowercase().contains(keyword) ||
                                    it.kecamatan.lowercase().contains(keyword) ||
                                    it.desa.lowercase().contains(keyword)
                        }
                    )
                }
            }

            lahanAdapter.notifyDataSetChanged()
            tvTotalData.text = "Total Data: ${filteredListLahan.size} Lahan"
        }

        fabDownloadData.visibility = View.GONE

        fabDownloadData.setOnClickListener {
            if(listLahan.isNotEmpty()){
                askUser(this, "Konfirmasi","Apakah anda yakin ingin mengunduh data lahan?"){
                    exportToCSV()
                }
            }else{
                showError(this, "ERROR", "Tidak ada data yang tersedia!")
            }
        }

        if(isCanCRUD(this)){
            fabAddData.visibility = View.VISIBLE
        }else{
            fabAddData.visibility = View.GONE
        }

        fabAddData.setOnClickListener {
                val i = Intent(this, AddLahan::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
        }

    }

    private suspend fun loadFilter(){
        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        when (kategori) {
            "provinsi" -> {
                tvDataFilter.text = "Filter Data berdasarkan Provinsi"
                defProv.clear()
                defProv.add(ProvinsiEntity(0, "PILIH PROVINSI"))
                defProv.add(dbWilayah.getProvinsiById(RoleHelper(this).id))
                spDataFilter.adapter = provAdapter
                provAdapter.notifyDataSetChanged()
            }
            "kabupaten" -> {
                defKab.clear()
                tvDataFilter.text = "Filter Data berdasarkan Provinsi"
                defKab.add(KabupatenEntity(0, "PILIH KABUPATEN", 0))
                when(getMyLevel(this)){
                    "provinsi" -> {
                        defKab.addAll(dbWilayah.getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0)))
                    }
                    "kabupaten" -> {
                        defKab.add(dbWilayah.getKabupatenById(RoleHelper(this).id))
                    }
                }
                if (defKab.size == 1) {
                    defKab.add(KabupatenEntity(-1, "TIDAK ADA DATA", 0))
                }
                spDataFilter.adapter = kabAdapter
                kabAdapter.notifyDataSetChanged()
            }
            "kecamatan" -> {
                defKec.clear()
                tvDataFilter.text = "Filter Data berdasarkan Kecamatan"
                defKec.add(KecamatanEntity(0, "PILIH KECAMATAN", 0))
                when(getMyLevel(this)){
                    "provinsi" -> {
                        val listKab = dbWilayah.getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0))
                        listKab.forEach { kab ->
                            val kecList = dbWilayah.getKecamatanByKabupaten(kab.id)
                            kecList.forEach { kec ->
                                defKec.add(
                                    KecamatanEntity(
                                        kec.id,
                                        "KEC. ${kec.nama} - KAB. ${kab.nama}",
                                        kab.id
                                    )
                                )
                            }
                        }
                    }
                    "kabupaten" -> {
                        val kab = dbWilayah.getKabupatenById(RoleHelper(this).id)
                        defKec.addAll(dbWilayah.getKecamatanByKabupaten(kab.id))

                    }
                    "kecamatan" -> {
                        defKec.addAll(dbWilayah.getKecamatanByIds(RoleHelper(this).ids))
                    }
                }
                if (defKec.size == 1) {
                    defKec.add(KecamatanEntity(-1, "TIDAK ADA DATA", 0))
                }
                spDataFilter.adapter = kecAdapter
                kecAdapter.notifyDataSetChanged()
            }
            "desa" -> {
                defDesa.clear()
                tvDataFilter.text = "Filter Data berdasarkan Desa"
                defDesa.add(DesaEntity(0, "PILIH DESA", 0))
                when(getMyLevel(this)){
                    "provinsi" -> {
                        val listKab = dbWilayah.getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0))
                        listKab.forEach { kab ->
                            val kecList = dbWilayah.getKecamatanByKabupaten(kab.id)
                            kecList.forEach { kec ->
                                val desaList = dbWilayah.getDesaByKecamatan(kec.id)
                                desaList.forEach { desa ->
                                    defDesa.add(
                                        DesaEntity(
                                            desa.id,
                                            "DESA ${desa.nama} - KEC. ${kec.nama} - KAB. ${kab.nama}",
                                            kec.id
                                        )
                                    )
                                }
                            }
                        }
                    }
                    "kabupaten" -> {
                        val kab = dbWilayah.getKabupatenById(RoleHelper(this).id)
                        val listKec = dbWilayah.getKecamatanByKabupaten(kab.id)
                        listKec.forEach { kec ->
                            val desaList = dbWilayah.getDesaByKecamatan(kec.id)
                            desaList.forEach { desa ->
                                defDesa.add(
                                    DesaEntity(
                                        desa.id,
                                        "DESA ${desa.nama} - KEC. ${kec.nama}",
                                        kec.id
                                    )
                                )
                            }
                        }
                    }
                    "kecamatan" -> {
                        val listKec = dbWilayah.getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            val desaList = dbWilayah.getDesaByKecamatan(kec.id)
                            desaList.forEach { desa ->
                                defDesa.add(
                                    DesaEntity(
                                        desa.id,
                                        "DESA ${desa.nama} - KEC. ${kec.nama}",
                                        kec.id
                                    )
                                )
                            }
                        }
                    }
                    "desa" -> {
                        defDesa.add(dbWilayah.getDesaById(RoleHelper(this).id))
                    }
                }
                if (defDesa.size == 1) {
                    defDesa.add(DesaEntity(-1, "TIDAK ADA DATA", 0))
                }
                spDataFilter.adapter = desaAdapter
                desaAdapter.notifyDataSetChanged()
            }
            "polda" -> {
                defPol.clear()
                tvDataFilter.text = "Pilih Polda"
                val result = withContext(Dispatchers.IO){
                    loadSatker()
                }
                if (result.isEmpty()) {
                    defPol.add(SatkerEntity(-1, "TIDAK ADA DATA", "TIDAK ADA DATA", "POLDA", null, null, null))
                }else{
                    defPol.add(SatkerEntity(0, "PILIH POLDA", "POLDA", "POLDA", null, null, null))
                    defPol.addAll(result)
                }
                spDataFilter.adapter = polAdapter
                polAdapter.notifyDataSetChanged()
            }
            "polres" -> {
                defPol.clear()
                tvDataFilter.text = "Pilih Polres"
                val result = withContext(Dispatchers.IO){
                    loadSatker()
                }
                if (result.isEmpty()) {
                    defPol.add(SatkerEntity(-1, "TIDAK ADA DATA", "TIDAK ADA DATA", "POLSEK", null, null, null))
                }else{
                    defPol.add(SatkerEntity(-1, "TIDAK ADA DATA", "PILIH POLRES", "POLSEK", null, null, null))
                    defPol.addAll(result)
                }
                spDataFilter.adapter = polAdapter
                polAdapter.notifyDataSetChanged()
            }
            "polsek" -> {
                defPol.clear()
                tvDataFilter.text = "Pilih Polsek"
                val result = withContext(Dispatchers.IO){
                    loadSatker()
                }
                if(result.isEmpty()) {
                    defPol.add(SatkerEntity(0, "PILIH POLSEK", "TIDAK ADA DATA", "POLSEK", null, null, null))
                }else{
                    defPol.add(SatkerEntity(0, "PILIH POLSEK", "PILIH POLSEK", "POLSEK", null, null, null))
                    defPol.addAll(result)
                }
                spDataFilter.adapter = polAdapter
                polAdapter.notifyDataSetChanged()
            }
            "owner" -> {
                defOwner.clear()
                tvDataFilter.text = "Filter Data berdasarkan Pemilik"
                defOwner.add(OwnerEntity(0, TypeOwner.PRIBADI, IsGapki.TIDAK, "", "PILIH PEMILIK", "", "", "", 0, 0, 0, 0, "", "",
                    Date(), Date(), "", ""))
                when(getMyLevel(this)){
                    "provinsi" -> {
                        val owner = dbOwner.getOwnerByProvinsi(komoditas, sh.getInt("satker_provinsiId", 0))
                        owner.forEach { it ->
                            defOwner.add(
                                OwnerEntity(
                                    it.id,
                                    it.type,
                                    it.gapki,
                                    it.nama_pok,
                                    "${it.nama} - ${it.nama_pok}",
                                    it.nik,
                                    it.alamat,
                                    it.telepon,
                                    it.provinsi_id,
                                    it.kabupaten_id,
                                    it.kecamatan_id,
                                    it.desa_id,
                                    it.status,
                                    it.alasan,
                                    it.createAt,
                                    it.updatedAt,
                                    it.komoditas,
                                    it.submitter
                                )
                            )
                        }
                    }
                    "kabupaten" -> {
                        val kab = dbWilayah.getKabupatenById(RoleHelper(this).id)
                        val owner = dbOwner.getOwnerByKabupaten(komoditas, kab.id)
                        owner.forEach { it ->
                            defOwner.add(
                                OwnerEntity(
                                    it.id,
                                    it.type,
                                    it.gapki,
                                    it.nama_pok,
                                    "${it.nama} - ${it.nama_pok}",
                                    it.nik,
                                    it.alamat,
                                    it.telepon,
                                    it.provinsi_id,
                                    it.kabupaten_id,
                                    it.kecamatan_id,
                                    it.desa_id,
                                    it.status,
                                    it.alasan,
                                    it.createAt,
                                    it.updatedAt,
                                    it.komoditas,
                                    it.submitter
                                )
                            )
                        }
                    }
                    "kecamatan" -> {
                        val listKec = dbWilayah.getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            val owner = dbOwner.getOwnerByKecamatan(komoditas, kec.id)
                            Log.d("JUMLAH OWNER", owner.size.toString())
                            owner.forEach { it ->
                                defOwner.add(
                                    OwnerEntity(
                                        it.id,
                                        it.type,
                                        it.gapki,
                                        it.nama_pok,
                                        "${it.nama} - ${it.nama_pok}",
                                        it.nik,
                                        it.alamat,
                                        it.telepon,
                                        it.provinsi_id,
                                        it.kabupaten_id,
                                        it.kecamatan_id,
                                        it.desa_id,
                                        it.status,
                                        it.alasan,
                                        it.createAt,
                                        it.updatedAt,
                                        it.komoditas,
                                        it.submitter
                                    )
                                )
                            }
                        }
                    }
                    "desa" -> {
                        val owner = dbOwner.getOwnerByDesa(komoditas, dbWilayah.getDesaById(RoleHelper(this).id).id)
                        Log.d("JUMLAH OWNER", owner.size.toString())
                        owner.forEach { it ->
                            defOwner.add(
                                OwnerEntity(
                                    it.id,
                                    it.type,
                                    it.gapki,
                                    it.nama_pok,
                                    "${it.nama} - ${it.nama_pok}",
                                    it.nik,
                                    it.alamat,
                                    it.telepon,
                                    it.provinsi_id,
                                    it.kabupaten_id,
                                    it.kecamatan_id,
                                    it.desa_id,
                                    it.status,
                                    it.alasan,
                                    it.createAt,
                                    it.updatedAt,
                                    it.komoditas,
                                    it.submitter
                                )
                            )
                        }
                    }
                }
                if (defOwner.size == 1) {
                    defOwner.add(OwnerEntity(-1, TypeOwner.PRIBADI, IsGapki.TIDAK, "", "TIDAK ADA DATA", "", "", "", 0, 0, 0, 0, "", "",
                        Date(), Date(), "", ""))
                }
                spDataFilter.adapter = ownerAdapter
                ownerAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(id: Int) {
        etSearchLayout.visibility = View.GONE
        rvData.visibility = View.GONE
        tvTotalData.text = "Belum Ada Data Terkait!"
        listLahan.clear()
        filteredListLahan.clear()

        val data: List<NewLahanEntity> = when (kategori) {
            "provinsi" -> {
                val owners = dbOwner.getOwnerByProvinsi(komoditas, id)
                consumeData("OWNER", null, owners)
            }
            "kabupaten" -> {
                val owners = dbOwner.getOwnerByKabupaten(komoditas, id)
                consumeData("OWNER", null, owners)
            }
            "kecamatan" -> {
                val owners = dbOwner.getOwnerByKecamatan(komoditas, id)
                consumeData("OWNER", null, owners)
            }
            "desa" -> {
                val owners = dbOwner.getOwnerByDesa(komoditas, id)
                consumeData("OWNER", null, owners)
            }
            "polda" -> {
                val satker = dbWilayah.getSatkerById(id)
                satker.provinsiId?.let {
                    val owners = dbOwner.getOwnerByProvinsi(komoditas, it)
                    consumeData("OWNER", null, owners)
                } ?: emptyList()
            }
            "polres" -> {
                val satker = dbWilayah.getSatkerById(id)
                satker.kabupatenId?.let {
                    val owners = dbOwner.getOwnerByKabupaten(komoditas, it)
                    consumeData("OWNER", null, owners)
                } ?: emptyList()
            }
            "polsek" -> {
                val kecamatanIds = dbWilayah.getKecamatanIdByPolsekId(id).map { it.kecamatanId }
                if (kecamatanIds.isNotEmpty()) {
                    val owners = dbOwner.getOwnerByKecamatans(komoditas, kecamatanIds)
                    consumeData("OWNER", null, owners)
                } else emptyList()
            }
            "owner" -> {
                val lahanList = dbLahan.getVerifiedLahanByOwner(komoditas, id)
                consumeData("LAHAN", lahanList, null)
            }
            else -> emptyList()
        }

        if (data.isNotEmpty()) {
            listLahan.addAll(data)
            filteredListLahan.addAll(data)
            lahanAdapter.notifyDataSetChanged()
            rvData.visibility = View.VISIBLE
            etSearchLayout.visibility = View.VISIBLE
            tvTotalData.text = "Total Data: ${data.size} Lahan"
            etSearch.setText("")
            fabDownloadData.visibility = View.VISIBLE
        } else {
            tvTotalData.text = "Belum Ada Data Terkait!"
            rvData.visibility = View.GONE
            etSearchLayout.visibility = View.GONE
        }
        updateDataCard(data)
    }

    private suspend fun updateDataCard(d: List<NewLahanEntity>) {
        if(d.isEmpty()){
            allCard.visibility = View.GONE
        }else{
            allCard.visibility = View.VISIBLE
            if(spFilterBy.selectedItemPosition > 0){
                allCardCategory.visibility = View.GONE
            }else{
                allCardCategory.visibility = View.VISIBLE
            }
            val data = d.map {
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
        }
    }


    private suspend fun consumeData(
        type: String,
        Lahan: List<LahanEntity>?,
        Owner: List<OwnerEntity>?
    ): List<NewLahanEntity> {
        return when {
            type == "LAHAN" && Lahan != null -> consumeLahan(Lahan)
            type == "OWNER" && Owner != null -> {
                val allLahan = Owner.flatMap {
                    dbLahan.getVerifiedLahanByOwner(komoditas, it.id)
                }
                consumeLahan(allLahan)
            }
            else -> emptyList()
        }
    }

    private suspend fun consumeLahan(Lahan: List<LahanEntity>): List<NewLahanEntity>{
        val list = mutableListOf<NewLahanEntity>()
        val allProv = dbWilayah.getProvinsi().associateBy { it.id }
        val allKab = dbWilayah.getKabupaten().associateBy { it.id }
        val allKec = dbWilayah.getKecamatan().associateBy { it.id }
        val allDesa = dbWilayah.getDesa().associateBy { it.id }
        val allOwner = dbOwner.getAll().associateBy { it.id }

        Lahan.forEachIndexed { index, it ->
            val provinsi = allProv[it.provinsi_id]
            val kabupaten = allKab[it.kabupaten_id]
            val kecamatan = allKec[it.kecamatan_id]
            val desa = allDesa[it.desa_id]
            val owner = allOwner[it.owner_id]
            val namaOwner = owner?.nama?: "UNKNOWN"
            val namaPok = owner?.nama_pok?: "UNKNOWN"
            val typeOwner = owner?.type?: TypeOwner.PRIBADI

            list.add(
                NewLahanEntity(
                    it.id,
                    it.type,
                    it.komoditas,
                    "LAHAN KE-${it.lahanke} MILIK $namaOwner",
                    it.owner_id,
                    namaOwner,
                    namaPok,
                    typeOwner.toString(),
                    it.provinsi_id,
                    provinsi?.nama?:"UNKNOWN",
                    it.kabupaten_id,
                    kabupaten?.nama?:"UNKNOWN",
                    it.kecamatan_id,
                    kecamatan?.nama?:"UNKNOWN",
                    it.desa_id,
                    desa?.nama?:"UNKNOWN",
                    it.luas,
                    it.latitude,
                    it.longitude,
                    it.status,
                    it.alasan,
                    it.createAt,
                    it.updateAt,
                    it.submitter,
                    it.lahanke
                )
            )
        }
        return list
    }

    private fun exportToCSV() {
        Loading.show(this)

        val csvHeader = listOf(
            "No", "lahan ke", "Komoditas", "Type", "Pemilik", "Kelompok", "Luas (m2)", "Luas (hektar)",
            "Provinsi", "Kabupaten", "Kecamatan", "Desa", "Latitude", "Longitude"
        )

        val csvData = listLahan.mapIndexed { index, lahan ->
            val luasM2 = lahan.luas.toDoubleOrNull() ?: 0.0
            listOf(
                (index + 1).toString(),
                lahan.lahanke,
                lahan.komoditas,
                lahan.type.name,
                lahan.owner_name,
                lahan.owner_pok,
                luasM2.toString(),
                convertToHektar(luasM2),
                lahan.provinsi,
                lahan.kabupaten,
                lahan.kecamatan,
                lahan.desa,
                lahan.latitude,
                lahan.longitude
            )
        }

        val selectedFilter = spDataFilter.selectedItem?.toString()?.replace("\\s+".toRegex(), "_") ?: "unknown"
        val fileName = "data_lahan_${kategori}_${selectedFilter}_${System.currentTimeMillis()}.csv"

        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kp3k")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, fileName)

        try {
            FileWriter(file).use { writer ->
                writer.appendLine(csvHeader.joinToString(","))
                csvData.forEach { row ->
                    writer.appendLine(row.joinToString(",") { it.toString().replace(",", " ") })
                }
            }

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

            AlertDialog.Builder(this)
                .setTitle("Export Selesai")
                .setItems(arrayOf("Lihat Data", "Kirim ke WhatsApp")) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "text/csv")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(intent, "Buka file dengan..."))
                        }

                        1 -> {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                setPackage("com.whatsapp")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(this, "WhatsApp tidak ditemukan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setIcon(R.drawable.download_csv_icon)
                .setNegativeButton("Batal", null)
                .show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan CSV: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            Loading.hide()
        }
    }

    private suspend fun loadSatker(): List<SatkerEntity>{
        when(kategori){
            "polda" -> {
                return when (getMyLevel(this)) {
                    "provinsi" -> dbWilayah.getSatkerOnListById(getMySatkerId(this))
                    else -> emptyList()
                }
            }
            "polres" -> {
                return when (getMyLevel(this)) {
                    "provinsi" -> dbWilayah.getPolresOnMyPolda(getMySatkerId(this))
                    "kabupaten" -> dbWilayah.getSatkerOnListById(getMySatkerId(this))
                    else -> emptyList()
                }
            }
            "polsek" -> {
                when(getMyLevel(this)){
                    "provinsi" -> {
                        val listPolres = dbWilayah.getPolresOnMyPolda(getMySatkerId(this)).map { it.id }
                        return dbWilayah.getPolsekOnMyPolda(listPolres)
                    }
                    "kabupaten" -> return dbWilayah.getPolsekOnMyPolres(getMySatkerId(this))
                    "kecamatan" -> return dbWilayah.getSatkerOnListById(getMySatkerId(this))
                    else -> return emptyList()
                }
            }
            else -> return emptyList()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadFilter()
            if(defId != null){
                loadData(defId!!)
            }
        }
    }

}