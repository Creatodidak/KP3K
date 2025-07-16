package id.creatodidak.kp3k.newversion.DataTanaman

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.TanamanAdapter
import id.creatodidak.kp3k.api.newModel.Kuartal
import id.creatodidak.kp3k.api.newModel.MasaTanam
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.DesaEntity
import id.creatodidak.kp3k.database.Entity.KabupatenEntity
import id.creatodidak.kp3k.database.Entity.KecamatanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.ProvinsiEntity
import id.creatodidak.kp3k.database.Entity.SatkerEntity
import id.creatodidak.kp3k.helper.DatePickerMode
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.enableDragAndSnap
import id.creatodidak.kp3k.helper.generateKuartalList
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.getMySatkerId
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.helper.onlyDateEquals
import id.creatodidak.kp3k.helper.onlyMonthYearEquals
import id.creatodidak.kp3k.helper.onlyYearEquals
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.helper.withEndOfDay
import id.creatodidak.kp3k.helper.withStartOfDay
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory.NewLahanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowDataTanamanByCategory : AppCompatActivity() {
    data class NewTanamanEntity(
        val id: Int,
        val lahan_id: Int,
        val showCaseName: String,
        val lahanWithOwner: String,
        val typeLahan: TypeLahan,
        val luasLahan: String,
        val jumlahPanen: String?,
        val masatanam: String,
        val luastanam: String,
        val tanggaltanam: Date,
        val prediksipanen: String,
        val rencanatanggalpanen: Date,
        val komoditas: String,
        val varietas: String,
        val sumber: SumberBibit,
        val keteranganSumber: String,
        val foto1: String,
        val foto2: String,
        val foto3: String,
        val foto4: String,
        val status: String,
        val alasan: String?,
        val createAt: Date,
        val updateAt: Date,
        val submitter: String,
        val tanamanke: String
    )
    
    private lateinit var db : AppDatabase
    private lateinit var dbLahan : LahanDao
    private lateinit var dbTanaman : TanamanDao
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String
    private lateinit var kategori : String
    private lateinit var tvKeteranganKomoditas: TextView

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var optionalFilter: LinearLayout
    private lateinit var datePickers: LinearLayout
    private lateinit var lyTanggalStart: TextInputLayout
    private lateinit var lyTanggalEnd: TextInputLayout
    private lateinit var etTanggalStart: TextInputEditText
    private lateinit var etTanggalEnd: TextInputEditText
    private lateinit var spDataFilterBy: Spinner
    private lateinit var lyMasaTanam: LinearLayout
    private lateinit var spMasaTanam: Spinner
    private lateinit var spDataFilter: Spinner
    private lateinit var tvDataFilter: TextView
    private lateinit var rvDataTanaman: RecyclerView
    private lateinit var tvTotalData: TextView

    private lateinit var fabAddData: FloatingActionButton
    private lateinit var fabDownloadData: FloatingActionButton

    private var defProv = mutableListOf<ProvinsiEntity>()
    private var defKab = mutableListOf<KabupatenEntity>()
    private var defKec = mutableListOf<KecamatanEntity>()
    private var defDesa = mutableListOf<DesaEntity>()
    private var defPol = mutableListOf<SatkerEntity>()
    private var defOwner = mutableListOf<OwnerEntity>()
    private var defLahan = mutableListOf<NewLahanEntity>()

    private var listTanaman = mutableListOf<NewTanamanEntity>()
    private var filteredListTanaman = mutableListOf<NewTanamanEntity>()

    private lateinit var provAdapter: ArrayAdapter<ProvinsiEntity>
    private lateinit var kabAdapter: ArrayAdapter<KabupatenEntity>
    private lateinit var kecAdapter: ArrayAdapter<KecamatanEntity>
    private lateinit var desaAdapter: ArrayAdapter<DesaEntity>
    private lateinit var polAdapter: ArrayAdapter<SatkerEntity>
    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>
    private lateinit var lahanAdapter: ArrayAdapter<NewLahanEntity>
    private lateinit var lyFab: LinearLayout
    private lateinit var tanamanAdapter: TanamanAdapter
    private var defId : Int? = null

    private val filterDataBy = listOf("TAMPILKAN SEMUA", "TANGGAL TANAM", "BULAN TANAM", "TAHUN TANAM", "KUARTAL TANAM", "MASA TANAM", "RENTANG TANGGAL")
    private lateinit var filterDataByAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_data_tanaman_by_category)
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
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()} berdasarkan ${kategori.capitalize()}"
        tvTotalData = findViewById(R.id.tvTotalData)

        swipeRefreshLayout = findViewById(R.id.swlShowDataTanaman)
        scrollView = findViewById(R.id.svShowDataTanaman)
        optionalFilter = findViewById(R.id.optionalFilter)
        datePickers = findViewById(R.id.datePickers)
        lyTanggalStart = findViewById(R.id.lyTanggalStart)
        lyTanggalEnd = findViewById(R.id.lyTanggalEnd)
        etTanggalStart = findViewById(R.id.etTanggalStart)
        etTanggalEnd = findViewById(R.id.etTanggalEnd)
        spDataFilterBy = findViewById(R.id.spDataFilterBy)
        lyMasaTanam = findViewById(R.id.lyMasaTanam)
        spMasaTanam = findViewById(R.id.spMasaTanam)
        spDataFilter = findViewById(R.id.spDataFilter)
        tvDataFilter = findViewById(R.id.tvDataFilter)
        rvDataTanaman = findViewById(R.id.rvDataTanaman)
        fabAddData = findViewById(R.id.fabAddData)
        fabDownloadData = findViewById(R.id.fabDownloadData)
        lyFab = findViewById(R.id.lyFab)
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
        lahanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defLahan)
        lahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterDataByAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterDataBy)
        filterDataByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        lyFab.enableDragAndSnap()
        tanamanAdapter = TanamanAdapter(filteredListTanaman, onRincianClick = { data ->
            val i = Intent(this, DataTanamanDetails::class.java)
            i.putExtra("komoditas", komoditas)
            i.putExtra("id", data.id.toString())
            i.putExtra("status", data.status)
            i.putExtra("kategori", kategori)
            startActivity(i)
        })

        etTanggalStart.inputType = InputType.TYPE_NULL
        etTanggalStart.isFocusable = false
        etTanggalEnd.inputType = InputType.TYPE_NULL
        etTanggalEnd.isFocusable = false

        rvDataTanaman.adapter = tanamanAdapter
        rvDataTanaman.layoutManager = LinearLayoutManager(this)

        spDataFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
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

                            "lahan" -> {
                                val id = defLahan[position].id
                                defId = id
                                loadData(id)
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        spDataFilterBy.adapter = filterDataByAdapter
        spDataFilterBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                filteredListTanaman.clear()
                tanamanAdapter.notifyDataSetChanged()
                tvTotalData.text = "Menunggu data..."

                when(position){
                    0 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Menampilkan semua data!", Toast.LENGTH_SHORT).show()
                        lyMasaTanam.visibility = View.GONE
                        datePickers.visibility = View.GONE

                        filteredListTanaman.addAll(listTanaman)
                        tanamanAdapter.notifyDataSetChanged()
                        tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
                    }
                    1 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Tanggal!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE
                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataTanamanByCategory, DatePickerMode.DAY) { d ->
                                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()

                                val startOfDay = d.withStartOfDay()
                                val endOfDay = d.withEndOfDay()

                                val newFilteredData = listTanaman.filter {
                                    it.tanggaltanam in startOfDay..endOfDay
                                }

                                filteredListTanaman.clear()
                                filteredListTanaman.addAll(newFilteredData)
                                tanamanAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
                            }
                        }
                    }
                    2 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Bulan!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataTanamanByCategory, DatePickerMode.MONTH) { d ->
                                val formattedDate = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()

                                val newFilteredData = listTanaman.filter { it.tanggaltanam.onlyMonthYearEquals(d) }
                                filteredListTanaman.addAll(newFilteredData)
                                tanamanAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
                            }
                        }
                    }
                    3 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Tahun!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataTanamanByCategory, DatePickerMode.YEAR) { d ->
                                val formattedDate = SimpleDateFormat("yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()

                                val newFilteredData = listTanaman.filter { it.tanggaltanam.onlyYearEquals(d) }
                                filteredListTanaman.addAll(newFilteredData)
                                tanamanAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
                            }
                        }
                    }
                    4 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Kuartal!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.GONE
                        lyMasaTanam.visibility = View.VISIBLE
                        spMasaTanam.visibility = View.VISIBLE

                        val kuartalList = mutableListOf<Kuartal>()
                        kuartalList.add(Kuartal(0, Date(), Date(), 0, "PILIH KUARTAL"))
                        kuartalList.addAll(generateKuartalList())
                        val adapter = ArrayAdapter(this@ShowDataTanamanByCategory, android.R.layout.simple_spinner_item, kuartalList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spMasaTanam.adapter = adapter

                        spMasaTanam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                if(position > 0){
                                    filterByRange(kuartalList[position].tanggalStart, kuartalList[position].tanggalEnd)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                TODO("Not yet implemented")
                            }

                        }
                    }
                    5 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Masa Tanam!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.GONE
                        lyMasaTanam.visibility = View.VISIBLE
                        spMasaTanam.visibility = View.VISIBLE

                        val masaTanamList = mutableListOf<MasaTanam>()
                        masaTanamList.add(MasaTanam(0, "0", "PILIH MASA TANAM"))
                        masaTanamList.addAll(generateMasaTanamList())
                        val adapter = ArrayAdapter(this@ShowDataTanamanByCategory, android.R.layout.simple_spinner_item, masaTanamList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spMasaTanam.adapter = adapter

                        spMasaTanam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                if(position > 0){
                                    val newFilteredData = listTanaman.filter { it.masatanam == masaTanamList[position].masatanam }
                                    filteredListTanaman.addAll(newFilteredData)
                                    tanamanAdapter.notifyDataSetChanged()
                                    tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                TODO("Not yet implemented")
                            }

                        }
                    }
                    6 -> {
                        Toast.makeText(this@ShowDataTanamanByCategory, "Pilih Tahun!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.VISIBLE
                        lyMasaTanam.visibility = View.GONE
                        var tanggalStart: Date? = null
                        var tanggalEnd: Date? = null

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataTanamanByCategory, DatePickerMode.DAY) { d ->
                                if (tanggalEnd != null && d.after(tanggalEnd)) {
                                    Toast.makeText(this@ShowDataTanamanByCategory, "Tanggal mulai tidak boleh lebih dari tanggal akhir!", Toast.LENGTH_SHORT).show()
                                    return@showCustomDatePicker
                                }

                                tanggalStart = d
                                val formatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formatted)
                                etTanggalStart.clearFocus()

                                if (tanggalEnd != null) {
                                    filterByRange(tanggalStart!!, tanggalEnd!!)
                                }
                            }
                        }

                        etTanggalEnd.setOnClickListener {
                            showCustomDatePicker(this@ShowDataTanamanByCategory, DatePickerMode.DAY) { d ->
                                if (tanggalStart != null && d.before(tanggalStart)) {
                                    Toast.makeText(this@ShowDataTanamanByCategory, "Tanggal akhir tidak boleh lebih kecil dari tanggal mulai!", Toast.LENGTH_SHORT).show()
                                    return@showCustomDatePicker
                                }

                                tanggalEnd = d
                                val formatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalEnd.setText(formatted)
                                etTanggalEnd.clearFocus()

                                if (tanggalStart != null) {
                                    filterByRange(tanggalStart!!, tanggalEnd!!)
                                }
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        fabDownloadData.setOnClickListener {
            askUser(this, "Konfirmasi","Apakah anda yakin ingin mengunduh data lahan?"){
                exportToCSV()
            }
        }

        fabAddData.setOnClickListener {
            val i = Intent(this, AddTanaman::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                loadFilter()
                if(defId != null) loadData(defId!!)
            }
        }
    }

    private suspend fun loadFilter(){
        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        when (kategori) {
            "provinsi" -> {
                tvDataFilter.text = "Pilih Provinsi"
                defProv.clear()
                defProv.add(ProvinsiEntity(0, "PILIH PROVINSI"))
                defProv.add(dbWilayah.getProvinsiById(RoleHelper(this).id))
                spDataFilter.adapter = provAdapter
                provAdapter.notifyDataSetChanged()
            }
            "kabupaten" -> {
                defKab.clear()
                tvDataFilter.text = "Pilih Provinsi"
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
                tvDataFilter.text = "Pilih Kecamatan"
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
                tvDataFilter.text = "Pilih Desa"
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
                tvDataFilter.text = "Pilih Pemilik"
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
            "lahan" -> {
                defLahan.clear()
                tvDataFilter.text = "Pilih Lahan"
                defLahan.add(NewLahanEntity(0, TypeLahan.MONOKULTUR, komoditas, "PILIH LAHAN", 0, "", "", "", 0, "", 0, "", 0, "", 0, "", "", "", "", "", null, Date(), Date(), "", ""))
                val owner = mutableListOf<OwnerEntity>()
                when(getMyLevel(this)){
                    "provinsi" -> {
                        owner.addAll(dbOwner.getOwnerByProvinsi(komoditas, sh.getInt("satker_provinsiId", 0)))
                    }
                    "kabupaten" -> {
                        val kab = dbWilayah.getKabupatenById(RoleHelper(this).id)
                        owner.addAll(dbOwner.getOwnerByKabupaten(komoditas, kab.id))
                    }
                    "kecamatan" -> {
                        val listKec = dbWilayah.getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            owner.addAll(dbOwner.getOwnerByKecamatan(komoditas, kec.id))
                        }
                    }
                    "desa" -> {
                        owner.addAll(dbOwner.getOwnerByDesa(komoditas, dbWilayah.getDesaById(RoleHelper(this).id).id))
                    }
                }
                owner.forEach { it ->
                    defLahan.addAll(getLahanByOwner(it.id))
                }

                if (defLahan.size == 1) {
                    defLahan.add(NewLahanEntity(0, TypeLahan.MONOKULTUR, komoditas, "BELUM ADA DATA!", 0, "", "", "", 0, "", 0, "", 0, "", 0, "", "", "", "", "", null, Date(), Date(), "", ""))
                }
                spDataFilter.adapter = lahanAdapter
                lahanAdapter.notifyDataSetChanged()
            }
        }
    }

    private suspend fun getLahanByOwner(id: Int): List<NewLahanEntity> {
        val listLahan = mutableListOf<NewLahanEntity>()
        val lahans = dbLahan.getVerifiedLahanByOwner(komoditas, id)
        val allProv = dbWilayah.getProvinsi().associateBy { it.id }
        val allKab = dbWilayah.getKabupaten().associateBy { it.id }
        val allKec = dbWilayah.getKecamatan().associateBy { it.id }
        val allDesa = dbWilayah.getDesa().associateBy { it.id }
        val allOwner = dbOwner.getAll().associateBy { it.id }

        lahans.forEachIndexed { idx, it ->
            val provinsi = allProv[it.provinsi_id]
            val kabupaten = allKab[it.kabupaten_id]
            val kecamatan = allKec[it.kecamatan_id]
            val desa = allDesa[it.desa_id]
            val owner = allOwner[it.owner_id]
            val namaOwner = owner?.nama?: "UNKNOWN"
            val namaPok = owner?.nama_pok?: "UNKNOWN"
            val typeOwner = owner?.type?: TypeOwner.PRIBADI
            listLahan.add(
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

        return listLahan
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(id: Int) {
        Log.i("RECEIVED_LOAD_DATA", "ID $id")
        listTanaman.clear()
        filteredListTanaman.clear()
        fabDownloadData.visibility = View.GONE
        rvDataTanaman.visibility = View.GONE
        tvTotalData.text = "Memuat Data..."
        swipeRefreshLayout.isRefreshing = true
        try {
            val dataTanaman = mutableListOf<NewTanamanEntity>()
            val datalahan = mutableListOf<NewLahanEntity>()
            when (kategori) {
                "provinsi" -> {
                    val owners = dbOwner.getOwnerByProvinsi(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "kabupaten" -> {
                    val owners = dbOwner.getOwnerByKabupaten(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "kecamatan" -> {
                    val owners = dbOwner.getOwnerByKecamatan(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "desa" -> {
                    val owners = dbOwner.getOwnerByDesa(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "polda" -> {
                    val satker = dbWilayah.getSatkerById(id)
                    satker.provinsiId?.let {
                        val owners = dbOwner.getOwnerByProvinsi(komoditas, it)
                        owners.forEach { it ->
                            datalahan.addAll(getLahanByOwner(it.id))
                        }
                    }
                }
                "polres" -> {
                    val satker = dbWilayah.getSatkerById(id)
                    satker.kabupatenId?.let {
                        val owners = dbOwner.getOwnerByKabupaten(komoditas, it)
                        owners.forEach { it ->
                            datalahan.addAll(getLahanByOwner(it.id))
                        }
                    }
                }
                "polsek" -> {
                    val kecamatanIds = dbWilayah.getKecamatanIdByPolsekId(id).map { it.kecamatanId }
                    if (kecamatanIds.isNotEmpty()) {
                        val owners = dbOwner.getOwnerByKecamatans(komoditas, kecamatanIds)
                        owners.forEach { it ->
                            datalahan.addAll(getLahanByOwner(it.id))
                        }
                    }
                }
                "owner" -> {
                    datalahan.addAll(getLahanByOwner(id))
                }
                "lahan" -> {
                    datalahan.add(defLahan.find { it.id == id }!!)
                }
            }

            datalahan.forEach { it ->
                dataTanaman.addAll(getTanaman(it))
            }
            Log.i("RECEIVED_LOAD_DATA", "DATA TANAMAN ${dataTanaman.size}")

            if (dataTanaman.isNotEmpty()) {
                listTanaman.addAll(dataTanaman)
                filteredListTanaman.addAll(dataTanaman)
                fabDownloadData.visibility = View.VISIBLE
                rvDataTanaman.visibility = View.VISIBLE
                tvTotalData.text = "${dataTanaman.size} Data Ditemukan"
                optionalFilter.visibility = View.VISIBLE
            } else {
                optionalFilter.visibility = View.VISIBLE
                spDataFilterBy.setSelection(0)
                tvTotalData.text = "Tidak Ada Data Ditemukan"
                rvDataTanaman.visibility = View.GONE
                fabDownloadData.visibility = View.GONE
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString())
        }finally {
            swipeRefreshLayout.isRefreshing = false
            tanamanAdapter.notifyDataSetChanged()
            if(isCanCRUD(this)){
                fabAddData.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun getTanaman(lahan: NewLahanEntity): List<NewTanamanEntity> {
        val newList = mutableListOf<NewTanamanEntity>()
        val listTanaman = dbTanaman.getTanamanByLahanId(komoditas, lahan.id)

        if (listTanaman.isNullOrEmpty()) return emptyList()

        val groupedByMasaTanam = listTanaman.groupBy { it.masatanam }.toSortedMap()

        groupedByMasaTanam.forEach { (masaTanamKe, tanamanList) ->
            tanamanList.forEachIndexed { index, it ->
                val dataPanen = db.panenDao().getPanenByTanamanId(it.id)
                newList.add(
                    NewTanamanEntity(
                        it.id,
                        lahan.id,
                        "TANAMAN KE-${it.tanamanke} MASA TANAM KE-$masaTanamKe",
                        lahan.showCaseName,
                        lahan.type,
                        lahan.luas,
                        dataPanen?.jumlahpanen.toString(),
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

        return newList
    }

    private fun exportToCSV() {
        Loading.show(this)

        val csvHeader = listOf(
            "No", "Showcase", "Pemilik Lahan", "Luas Lahan (m²)", "Luas Lahan (Ha)", "Luas Tanam (m²)", "Luas Tanam (Ha)", "Persen Tanam",
            "Masa Tanam", "Tanggal Tanam", "Prediksi Panen (KG)", "Prediksi Panen (Ton)", "Rencana Panen",
            "Komoditas", "Varietas", "Sumber Bibit", "Keterangan Sumber",
            "Status", "Alasan", "Submitter", "Tanggal Dibuat", "Tanggal Update"
        )

        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val csvData = listTanaman.mapIndexed { index, t ->
            listOf(
                (index + 1).toString(),
                t.showCaseName,
                t.lahanWithOwner,
                t.luasLahan,
                angkaIndonesia(convertToHektar(t.luasLahan.toDouble())),
                t.luastanam,
                angkaIndonesia(convertToHektar(t.luastanam.toDouble())),
                "${angkaIndonesia((t.luastanam.toDouble() / t.luasLahan.toDouble()) * 100)}%",
                t.masatanam,
                dateFormatter.format(t.tanggaltanam),
                t.prediksipanen,
                angkaIndonesia(convertToTon(t.prediksipanen.toDouble())),
                dateFormatter.format(t.rencanatanggalpanen),
                t.komoditas,
                t.varietas,
                t.sumber.name,
                t.keteranganSumber,
                t.status,
                t.alasan ?: "-",
                t.submitter,
                dateFormatter.format(t.createAt),
                dateFormatter.format(t.updateAt)
            )
        }

        val selectedFilter = spDataFilter.selectedItem?.toString()?.replace("\\s+".toRegex(), "_") ?: "unknown"
        val fileName = "data_tanaman_${kategori}_${selectedFilter}_${System.currentTimeMillis()}.csv"
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "kp3k"
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)

        try {
            FileWriter(file).use { writer ->
                writer.appendLine(csvHeader.joinToString(","))
                csvData.forEach { row ->
                    writer.appendLine(row.joinToString(",") { it.replace(",", " ") })
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

    @SuppressLint("NotifyDataSetChanged")
    private fun filterByRange(start: Date, end: Date) {
        val newFilteredData = listTanaman.filter {
            it.tanggaltanam >= start.withStartOfDay() && it.tanggaltanam <= end.withEndOfDay()
        }
        filteredListTanaman.clear()
        filteredListTanaman.addAll(newFilteredData)
        tanamanAdapter.notifyDataSetChanged()
        tvTotalData.text = "${filteredListTanaman.size} Data Ditemukan"
    }

    private fun generateMasaTanamList(): List<MasaTanam> {
        return listTanaman
            .map { it.masatanam }
            .distinct()
            .sorted() // opsional: sort agar urut
            .mapIndexed { index, mt ->
                MasaTanam(
                    id = index + 1,
                    masatanam = mt,
                    name = "MASA TANAM - ${index + 1}"
                )
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
            if(defId != null) loadData(defId!!)
        }
    }
}