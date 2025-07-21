package id.creatodidak.kp3k.newversion.DataPanen

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
import id.creatodidak.kp3k.adapter.NewAdapter.PanenAdapter
import id.creatodidak.kp3k.api.newModel.Kuartal
import id.creatodidak.kp3k.api.newModel.MasaTanam
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.DesaEntity
import id.creatodidak.kp3k.database.Entity.KabupatenEntity
import id.creatodidak.kp3k.database.Entity.KecamatanEntity
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.ProvinsiEntity
import id.creatodidak.kp3k.database.Entity.SatkerEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
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
import id.creatodidak.kp3k.helper.generateMasaTanamList
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMySatkerId
import id.creatodidak.kp3k.helper.isCanCRUD
import id.creatodidak.kp3k.helper.onlyMonthYearEquals
import id.creatodidak.kp3k.helper.onlyYearEquals
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.withEndOfDay
import id.creatodidak.kp3k.helper.withStartOfDay
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory.NewLahanEntity
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowDataPanenByCategory : AppCompatActivity() {
    data class NewPanenEntity(
        val id: Int,
        val showCaseName: String,
        val tanaman: TanamanEntity?,
        val lahan: LahanEntity?,
        val owner: OwnerEntity?,
        val tanaman_id: Int,
        val jumlahpanen: String,
        val luaspanen: String,
        val tanggalpanen: Date,
        val keterangan: String?,
        val analisa: String?,
        val foto1: String = "/media/default.jpg",
        val foto2: String = "/media/default.jpg",
        val foto3: String = "/media/default.jpg",
        val foto4: String = "/media/default.jpg",
        val status: String = "VERIFIED",
        val alasan: String?,
        val createAt: Date = Date(),
        val updateAt: Date = Date(),
        val komoditas: String,
        val submitter: String
    ){
        override fun toString(): String = showCaseName
    }

    private lateinit var db : AppDatabase
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
    private lateinit var rvDataPanen: RecyclerView
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
    private var defTanaman = mutableListOf<NewTanamanEntity>()

    private var listPanen = mutableListOf<NewPanenEntity>()
    private var filteredListPanen = mutableListOf<NewPanenEntity>()

    private lateinit var provAdapter: ArrayAdapter<ProvinsiEntity>
    private lateinit var kabAdapter: ArrayAdapter<KabupatenEntity>
    private lateinit var kecAdapter: ArrayAdapter<KecamatanEntity>
    private lateinit var desaAdapter: ArrayAdapter<DesaEntity>
    private lateinit var polAdapter: ArrayAdapter<SatkerEntity>
    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>
    private lateinit var lahanAdapter: ArrayAdapter<NewLahanEntity>
    private lateinit var tanamanAdapter: ArrayAdapter<ShowDataTanamanByCategory.NewTanamanEntity>
    
    private lateinit var lyFab: LinearLayout
    private lateinit var panenAdapter: PanenAdapter
    private var defId : Int? = null

    private val filterDataBy = listOf("TAMPILKAN SEMUA", "TANGGAL PANEN", "BULAN PANEN", "TAHUN PANEN", "KUARTAL PANEN", "MASA TANAM", "RENTANG TANGGAL")
    private lateinit var filterDataByAdapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_data_panen_by_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        kategori = intent.getStringExtra("kategori").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()} berdasarkan ${kategori.capitalize()}"
        tvTotalData = findViewById(R.id.tvTotalData)

        swipeRefreshLayout = findViewById(R.id.swlShowDataPanen)
        scrollView = findViewById(R.id.svShowDataPanen)
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
        rvDataPanen = findViewById(R.id.rvDataPanen)
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
        tanamanAdapter = ArrayAdapter(this, R.layout.item_spinner_multiline, defTanaman)
        tanamanAdapter.setDropDownViewResource(R.layout.item_spinner_multiline)
        filterDataByAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterDataBy)
        filterDataByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        lyFab.enableDragAndSnap()

        etTanggalStart.inputType = InputType.TYPE_NULL
        etTanggalStart.isFocusable = false
        etTanggalEnd.inputType = InputType.TYPE_NULL
        etTanggalEnd.isFocusable = false

        panenAdapter = PanenAdapter(
            filteredListPanen,
            onRincianClick = {
                val i = Intent(this, DataPanenDetails::class.java)
                i.putExtra("id", it.id.toString())
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        )
        rvDataPanen.adapter = panenAdapter
        rvDataPanen.layoutManager = LinearLayoutManager(this)

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

                            "tanaman" -> {
                                val id = defTanaman[position].lahan_id
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
                filteredListPanen.clear()
                panenAdapter.notifyDataSetChanged()
                tvTotalData.text = "Menunggu data.."

                when(position){
                    0 -> {
                        filteredListPanen.clear()
                        panenAdapter.notifyDataSetChanged()
                        Toast.makeText(this@ShowDataPanenByCategory, "Menampilkan semua data!", Toast.LENGTH_SHORT).show()
                        lyMasaTanam.visibility = View.GONE
                        datePickers.visibility = View.GONE

                        filteredListPanen.addAll(listPanen)
                        panenAdapter.notifyDataSetChanged()
                        tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
                    }
                    1 -> {

                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Tanggal!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE
                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataPanenByCategory, DatePickerMode.DAY) { d ->
                                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()

                                val startOfDay = d.withStartOfDay()
                                val endOfDay = d.withEndOfDay()

                                val newFilteredData = listPanen.filter {
                                    it.tanggalpanen in startOfDay..endOfDay
                                }

                                filteredListPanen.clear()
                                filteredListPanen.addAll(newFilteredData)
                                panenAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
                            }
                        }
                    }
                    2 -> {
                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Bulan!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataPanenByCategory, DatePickerMode.MONTH) { d ->
                                val formattedDate = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()
                                filteredListPanen.clear()
                                panenAdapter.notifyDataSetChanged()
                                val newFilteredData = listPanen.filter { it.tanggalpanen.onlyMonthYearEquals(d) }
                                filteredListPanen.addAll(newFilteredData)
                                panenAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
                            }
                        }
                    }
                    3 -> {
                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Tahun!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.GONE
                        lyMasaTanam.visibility = View.GONE

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataPanenByCategory, DatePickerMode.YEAR) { d ->
                                val formattedDate = SimpleDateFormat("yyyy", Locale.getDefault()).format(d)
                                etTanggalStart.setText(formattedDate)
                                etTanggalStart.clearFocus()
                                filteredListPanen.clear()
                                panenAdapter.notifyDataSetChanged()
                                val newFilteredData = listPanen.filter { it.tanggalpanen.onlyYearEquals(d) }
                                filteredListPanen.addAll(newFilteredData)
                                panenAdapter.notifyDataSetChanged()
                                tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
                            }
                        }
                    }
                    4 -> {
                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Kuartal!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.GONE
                        lyMasaTanam.visibility = View.VISIBLE
                        spMasaTanam.visibility = View.VISIBLE
                        filteredListPanen.clear()
                        panenAdapter.notifyDataSetChanged()
                        val kuartalList = mutableListOf<Kuartal>()
                        kuartalList.add(Kuartal(0, Date(), Date(), 0, "PILIH KUARTAL"))
                        kuartalList.addAll(generateKuartalList())
                        val adapter = ArrayAdapter(this@ShowDataPanenByCategory, android.R.layout.simple_spinner_item, kuartalList)
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
                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Masa Tanam!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.GONE
                        lyMasaTanam.visibility = View.VISIBLE
                        spMasaTanam.visibility = View.VISIBLE

                        val masaTanamList = mutableListOf<MasaTanam>()
                        masaTanamList.add(MasaTanam(0, "0", "PILIH MASA TANAM"))
                        masaTanamList.addAll(generateMasaTanamList(listPanen.mapNotNull { it.tanaman }))
                        val adapter = ArrayAdapter(this@ShowDataPanenByCategory, android.R.layout.simple_spinner_item, masaTanamList)
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
                                    filteredListPanen.clear()
                                    panenAdapter.notifyDataSetChanged()
                                    val newFilteredData = listPanen.filter { it.tanaman?.masatanam == masaTanamList[position].masatanam }
                                    filteredListPanen.addAll(newFilteredData)
                                    panenAdapter.notifyDataSetChanged()
                                    tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                TODO("Not yet implemented")
                            }

                        }
                    }
                    6 -> {
                        Toast.makeText(this@ShowDataPanenByCategory, "Pilih Tahun!", Toast.LENGTH_SHORT).show()
                        datePickers.visibility = View.VISIBLE
                        lyTanggalStart.visibility = View.VISIBLE
                        lyTanggalEnd.visibility = View.VISIBLE
                        lyMasaTanam.visibility = View.GONE
                        var tanggalStart: Date? = null
                        var tanggalEnd: Date? = null

                        etTanggalStart.setOnClickListener {
                            showCustomDatePicker(this@ShowDataPanenByCategory, DatePickerMode.DAY) { d ->
                                if (tanggalEnd != null && d.after(tanggalEnd)) {
                                    Toast.makeText(this@ShowDataPanenByCategory, "Tanggal mulai tidak boleh lebih dari tanggal akhir!", Toast.LENGTH_SHORT).show()
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
                            showCustomDatePicker(this@ShowDataPanenByCategory, DatePickerMode.DAY) { d ->
                                if (tanggalStart != null && d.before(tanggalStart)) {
                                    Toast.makeText(this@ShowDataPanenByCategory, "Tanggal akhir tidak boleh lebih kecil dari tanggal mulai!", Toast.LENGTH_SHORT).show()
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
//                exportToCSV()
            }
        }

        fabAddData.setOnClickListener {
            val i = Intent(this, AddPanen::class.java)
            i.putExtra("komoditas", komoditas)
            startActivity(i)
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                if(defId != null) {
                    loadData(defId!!)
                }else{
                    loadFilter()
                }
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
                defProv.add(db.wilayahDao().getProvinsiById(RoleHelper(this).id))
                spDataFilter.adapter = provAdapter
                provAdapter.notifyDataSetChanged()
            }
            "kabupaten" -> {
                defKab.clear()
                tvDataFilter.text = "Pilih Provinsi"
                defKab.add(KabupatenEntity(0, "PILIH KABUPATEN", 0))
                when(getMyLevel(this)){
                    "provinsi" -> {
                        defKab.addAll(db.wilayahDao().getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0)))
                    }
                    "kabupaten" -> {
                        defKab.add(db.wilayahDao().getKabupatenById(RoleHelper(this).id))
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
                        val listKab = db.wilayahDao().getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0))
                        listKab.forEach { kab ->
                            val kecList = db.wilayahDao().getKecamatanByKabupaten(kab.id)
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
                        val kab = db.wilayahDao().getKabupatenById(RoleHelper(this).id)
                        defKec.addAll(db.wilayahDao().getKecamatanByKabupaten(kab.id))

                    }
                    "kecamatan" -> {
                        defKec.addAll(db.wilayahDao().getKecamatanByIds(RoleHelper(this).ids))
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
                        val listKab = db.wilayahDao().getKabupatenByProvinsi(sh.getInt("satker_provinsiId", 0))
                        listKab.forEach { kab ->
                            val kecList = db.wilayahDao().getKecamatanByKabupaten(kab.id)
                            kecList.forEach { kec ->
                                val desaList = db.wilayahDao().getDesaByKecamatan(kec.id)
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
                        val kab = db.wilayahDao().getKabupatenById(RoleHelper(this).id)
                        val listKec = db.wilayahDao().getKecamatanByKabupaten(kab.id)
                        listKec.forEach { kec ->
                            val desaList = db.wilayahDao().getDesaByKecamatan(kec.id)
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
                        val listKec = db.wilayahDao().getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            val desaList = db.wilayahDao().getDesaByKecamatan(kec.id)
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
                        defDesa.add(db.wilayahDao().getDesaById(RoleHelper(this).id))
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
                        val owner = db.ownerDao().getOwnerByProvinsi(komoditas, sh.getInt("satker_provinsiId", 0))
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
                        val kab = db.wilayahDao().getKabupatenById(RoleHelper(this).id)
                        val owner = db.ownerDao().getOwnerByKabupaten(komoditas, kab.id)
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
                        val listKec = db.wilayahDao().getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            val owner = db.ownerDao().getOwnerByKecamatan(komoditas, kec.id)
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
                        val owner = db.ownerDao().getOwnerByDesa(komoditas, db.wilayahDao().getDesaById(RoleHelper(this).id).id)
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
                        owner.addAll(db.ownerDao().getOwnerByProvinsi(komoditas, sh.getInt("satker_provinsiId", 0)))
                    }
                    "kabupaten" -> {
                        val kab = db.wilayahDao().getKabupatenById(RoleHelper(this).id)
                        owner.addAll(db.ownerDao().getOwnerByKabupaten(komoditas, kab.id))
                    }
                    "kecamatan" -> {
                        val listKec = db.wilayahDao().getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            owner.addAll(db.ownerDao().getOwnerByKecamatan(komoditas, kec.id))
                        }
                    }
                    "desa" -> {
                        owner.addAll(db.ownerDao().getOwnerByDesa(komoditas, db.wilayahDao().getDesaById(RoleHelper(this).id).id))
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
            "tanaman" -> {
                defTanaman.clear()
                tvDataFilter.text = "Pilih Tanaman"
                defTanaman.add(NewTanamanEntity(0, 0, "PILIH TANAMAN", "", TypeLahan.MONOKULTUR, "", "", "", "",Date(), "", Date(), "", "",
                    SumberBibit.PEMERINTAH, "", "", "", "", "", "", null, Date(), Date(), "", ""))
                val owner = mutableListOf<OwnerEntity>()
                when(getMyLevel(this)){
                    "provinsi" -> {
                        owner.addAll(db.ownerDao().getOwnerByProvinsi(komoditas, sh.getInt("satker_provinsiId", 0)))
                    }
                    "kabupaten" -> {
                        val kab = db.wilayahDao().getKabupatenById(RoleHelper(this).id)
                        owner.addAll(db.ownerDao().getOwnerByKabupaten(komoditas, kab.id))
                    }
                    "kecamatan" -> {
                        val listKec = db.wilayahDao().getKecamatanByIds(RoleHelper(this).ids)
                        listKec.forEach { kec ->
                            owner.addAll(db.ownerDao().getOwnerByKecamatan(komoditas, kec.id))
                        }
                    }
                    "desa" -> {
                        owner.addAll(db.ownerDao().getOwnerByDesa(komoditas, db.wilayahDao().getDesaById(RoleHelper(this).id).id))
                    }
                }

                val lahan = mutableListOf<NewLahanEntity>()
                owner.forEach { it ->
                    lahan.addAll(getLahanByOwner(it.id))
                }

                lahan.forEach {
                    defTanaman.addAll(getTanamanByLahan(it.id))
                }

                if (defTanaman.size == 1) {
                    defTanaman.add(NewTanamanEntity(-1, 0, "BELUM ADA DATA", "", TypeLahan.MONOKULTUR, "", "", "", "",Date(), "", Date(), "", "",
                        SumberBibit.PEMERINTAH, "", "", "", "", "", "", null, Date(), Date(), "", ""))
                }

                spDataFilter.adapter = tanamanAdapter
                tanamanAdapter.notifyDataSetChanged()
            }
        }
    }

    private suspend fun getLahanByOwner(id: Int): List<NewLahanEntity> {
        val listLahan = mutableListOf<NewLahanEntity>()
        val lahans = db.lahanDao().getVerifiedLahanByOwner(komoditas, id)
        val allProv = db.wilayahDao().getProvinsi().associateBy { it.id }
        val allKab = db.wilayahDao().getKabupaten().associateBy { it.id }
        val allKec = db.wilayahDao().getKecamatan().associateBy { it.id }
        val allDesa = db.wilayahDao().getDesa().associateBy { it.id }
        val allOwner = db.ownerDao().getAll().associateBy { it.id }

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

    private suspend fun getLahanById(id: Int): NewLahanEntity {
        val lahan = db.lahanDao().getLahanById(id)
        val Prov = db.wilayahDao().getProvinsiById(lahan.provinsi_id)
        val Kab = db.wilayahDao().getKabupatenById(lahan.kabupaten_id)
        val Kec = db.wilayahDao().getKecamatanById(lahan.kecamatan_id)
        val Desa = db.wilayahDao().getDesaById(lahan.desa_id)
        val Owner = db.ownerDao().getOwnerById(lahan.owner_id)

        return NewLahanEntity(
            lahan.id,
            lahan.type,
            lahan.komoditas,
            "LAHAN KE-${lahan.lahanke} MILIK ${Owner?.nama}",
            lahan.owner_id,
            Owner?.nama!!,
            Owner.nama_pok,
            Owner.type.name,
            lahan.provinsi_id,
            Prov.nama,
            lahan.kabupaten_id,
            Kab.nama,
            lahan.kecamatan_id,
            Kec.nama,
            lahan.desa_id,
            Desa.nama,
            lahan.luas,
            lahan.latitude,
            lahan.longitude,
            lahan.status,
            lahan.alasan,
            lahan.createAt,
            lahan.updateAt,
            lahan.submitter,
            lahan.lahanke
        )
    }

    private suspend fun getTanamanByLahan(id: Int): List<NewTanamanEntity>{
        val newList = mutableListOf<NewTanamanEntity>()
        val tanaman = db.tanamanDao().getTanamanByLahanId(komoditas, id)
        val lahan = db.lahanDao().getLahanById(id)
        val allKab = db.wilayahDao().getKabupaten().associateBy { it.id }
        val allKec = db.wilayahDao().getKecamatan().associateBy { it.id }
        val allDesa = db.wilayahDao().getDesa().associateBy { it.id }
        val allOwner = db.ownerDao().getAll().associateBy { it.id }

        if(!tanaman.isNullOrEmpty()){
            tanaman.forEach {
                val owner = allOwner[lahan.owner_id]
                val kabupaten = allKab[lahan.kabupaten_id]
                val kecamatan = allKec[lahan.kecamatan_id]
                val desa = allDesa[lahan.desa_id]

                newList.add(
                    NewTanamanEntity(
                        it.id,
                        lahan.id,
                        "TANAMAN-${it.tanamanke} MT-${it.masatanam} | ${owner?.nama} - ${owner?.nama_pok} | ${desa?.nama}-${kecamatan?.nama}-${kabupaten?.nama}",
                        "",
                        lahan.type,
                        lahan.luas,
                        "",
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

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(id: Int) {
        Log.i("RECEIVED_LOAD_DATA", "ID $id")
        listPanen.clear()
        filteredListPanen.clear()
        fabDownloadData.visibility = View.GONE
        rvDataPanen.visibility = View.GONE
        tvTotalData.text = "Memuat Data.."
        swipeRefreshLayout.isRefreshing = true
        try {
            val dataTanaman = mutableListOf<NewTanamanEntity>()
            val datalahan = mutableListOf<NewLahanEntity>()
            when (kategori) {
                "provinsi" -> {
                    val owners = db.ownerDao().getOwnerByProvinsi(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "kabupaten" -> {
                    val owners = db.ownerDao().getOwnerByKabupaten(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "kecamatan" -> {
                    val owners = db.ownerDao().getOwnerByKecamatan(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "desa" -> {
                    val owners = db.ownerDao().getOwnerByDesa(komoditas, id)
                    owners.forEach { it ->
                        datalahan.addAll(getLahanByOwner(it.id))
                    }
                }
                "polda" -> {
                    val satker = db.wilayahDao().getSatkerById(id)
                    satker.provinsiId?.let {
                        val owners = db.ownerDao().getOwnerByProvinsi(komoditas, it)
                        owners.forEach { it ->
                            datalahan.addAll(getLahanByOwner(it.id))
                        }
                    }
                }
                "polres" -> {
                    val satker = db.wilayahDao().getSatkerById(id)
                    satker.kabupatenId?.let {
                        val owners = db.ownerDao().getOwnerByKabupaten(komoditas, it)
                        owners.forEach { it ->
                            datalahan.addAll(getLahanByOwner(it.id))
                        }
                    }
                }
                "polsek" -> {
                    val kecamatanIds = db.wilayahDao().getKecamatanIdByPolsekId(id).map { it.kecamatanId }
                    if (kecamatanIds.isNotEmpty()) {
                        val owners = db.ownerDao().getOwnerByKecamatans(komoditas, kecamatanIds)
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
                "tanaman" -> {
                    datalahan.add(getLahanById(id))
                }
            }

            datalahan.forEach { it ->
                if(kategori == "tanaman"){
                    val d = getTanamanByLahan(it.id).filter { it.masatanam == defTanaman[spDataFilter.selectedItemPosition].masatanam }
                    dataTanaman.addAll(d)
                }else{
                    dataTanaman.addAll(getTanamanByLahan(it.id))
                }
            }

            val dataPanen = mutableListOf<NewPanenEntity>()
            dataTanaman.forEach {
                dataPanen.addAll(getDataPanen(it))
            }

            if (dataPanen.isNotEmpty()) {
                listPanen.addAll(dataPanen)
                filteredListPanen.addAll(dataPanen)
                fabDownloadData.visibility = View.VISIBLE
                rvDataPanen.visibility = View.VISIBLE
                tvTotalData.text = "${dataPanen.size} Data Ditemukan"
                optionalFilter.visibility = View.VISIBLE
            } else {
                optionalFilter.visibility = View.VISIBLE
                spDataFilterBy.setSelection(0)
                tvTotalData.text = "Tidak Ada Data Ditemukan"
                rvDataPanen.visibility = View.GONE
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

    private suspend fun getDataPanen(d: NewTanamanEntity): List<NewPanenEntity>{
        val data = mutableListOf<NewPanenEntity>()
        val panens = db.panenDao().getPanenByTanamanId(d.id)
        val tanamans = db.tanamanDao().getById(d.id)
        val lahan = if(tanamans != null) db.lahanDao().getLahanById(tanamans.lahan_id) else null
        val owner = if(lahan != null) db.ownerDao().getOwnerById(lahan.owner_id) else null
        if(!panens.isNullOrEmpty()){
            panens.forEachIndexed { idx, it ->
                data.add(
                    NewPanenEntity(
                        it.id,
                        "PANEN KE - ${idx+1}",
                        tanamans,
                        lahan,
                        owner,
                        it.tanaman_id,
                        it.jumlahpanen,
                        it.luaspanen,
                        it.tanggalpanen,
                        it.keterangan,
                        it.analisa,
                        it.foto1,
                        it.foto2,
                        it.foto3,
                        it.foto4,
                        it.status,
                        it.alasan,
                        it.createAt,
                        it.updateAt,
                        it.komoditas,
                        it.submitter
                    )

                )
            }
        }
        return data
    }
    private suspend fun loadSatker(): List<SatkerEntity>{
        when(kategori){
            "polda" -> {
                return when (getMyLevel(this)) {
                    "provinsi" -> db.wilayahDao().getSatkerOnListById(getMySatkerId(this))
                    else -> emptyList()
                }
            }
            "polres" -> {
                return when (getMyLevel(this)) {
                    "provinsi" -> db.wilayahDao().getPolresOnMyPolda(getMySatkerId(this))
                    "kabupaten" -> db.wilayahDao().getSatkerOnListById(getMySatkerId(this))
                    else -> emptyList()
                }
            }
            "polsek" -> {
                when(getMyLevel(this)){
                    "provinsi" -> {
                        val listPolres = db.wilayahDao().getPolresOnMyPolda(getMySatkerId(this)).map { it.id }
                        return db.wilayahDao().getPolsekOnMyPolda(listPolres)
                    }
                    "kabupaten" -> return db.wilayahDao().getPolsekOnMyPolres(getMySatkerId(this))
                    "kecamatan" -> return db.wilayahDao().getSatkerOnListById(getMySatkerId(this))
                    else -> return emptyList()
                }
            }
            else -> return emptyList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterByRange(start: Date, end: Date) {
        filteredListPanen.clear()
        val newFilteredData = listPanen.filter {
            it.tanggalpanen >= start.withStartOfDay() && it.tanggalpanen <= end.withEndOfDay()
        }
        filteredListPanen.addAll(newFilteredData)
        panenAdapter.notifyDataSetChanged()
        tvTotalData.text = "${filteredListPanen.size} Data Ditemukan"
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if(defId != null) {
                loadData(defId!!)
            }else{
                loadFilter()
            }
        }
    }
}