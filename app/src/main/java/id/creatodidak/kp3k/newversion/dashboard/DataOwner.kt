package id.creatodidak.kp3k.newversion.dashboard

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.OwnerDataAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.OwnerEndpoint
import id.creatodidak.kp3k.api.RequestClass.OwnerAddRequest
import id.creatodidak.kp3k.api.RequestClass.OwnerPatchRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.DesaEntity
import id.creatodidak.kp3k.database.Entity.KabupatenEntity
import id.creatodidak.kp3k.database.Entity.KecamatanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.ProvinsiEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.newversion.DataOwner.DataOwnerCreateEdit
import id.creatodidak.kp3k.newversion.DataOwner.DataOwnerDetails
import id.creatodidak.kp3k.newversion.DataOwner.DraftOwner
import id.creatodidak.kp3k.newversion.DataOwner.RejectedOwner
import id.creatodidak.kp3k.newversion.DataOwner.VerifikasiOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class DataOwner : AppCompatActivity() {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")

    private lateinit var db : AppDatabase
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var tvTotalOwnerDO : TextView
    private lateinit var swlDataOwnerDO : SwipeRefreshLayout
    private lateinit var rvListOwnerDO : RecyclerView
    private lateinit var fabAddPemilikLahan : FloatingActionButton
    private lateinit var komoditas : String
    private lateinit var sh : SharedPreferences

    private lateinit var spProvOD : Spinner
    private lateinit var spKabOD : Spinner
    private lateinit var spKecOD : Spinner
    private lateinit var spDesaOD : Spinner
    private lateinit var etSearchOD : EditText
    private lateinit var tvDraftDO : TextView
    private lateinit var lyDraftDO : LinearLayout
    private lateinit var lyUnverifiedDO : LinearLayout
    private lateinit var tvUnverifiedDO : TextView
    private lateinit var lyRejectedDO : LinearLayout
    private lateinit var tvRejectedDO : TextView

    private var defProv = mutableListOf<ProvinsiEntity>()
    private var defKab = mutableListOf<KabupatenEntity>()
    private var defKec = mutableListOf<KecamatanEntity>()
    private var defDesa = mutableListOf<DesaEntity>()
    private var ListOwner = mutableListOf<OwnerEntity>()
    private var recentListOwner = mutableListOf<OwnerEntity>()

    private lateinit var provAdapter: ArrayAdapter<String>
    private lateinit var kabAdapter: ArrayAdapter<KabupatenEntity>
    private lateinit var kecAdapter: ArrayAdapter<KecamatanEntity>
    private lateinit var desaAdapter: ArrayAdapter<DesaEntity>
    private lateinit var adapterOwner : OwnerDataAdapter

    private var defSelectedProv : Int = 1
    private var defSelectedKab : Int? = null
    private var defSelectedKec : Int? = null
    private var defSelectedDesa : Int? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_owner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvTotalOwnerDO = findViewById(R.id.tvTotalOwnerDO)
        swlDataOwnerDO = findViewById(R.id.swlDataOwnerDO)
        fabAddPemilikLahan = findViewById(R.id.fabAddPemilikLahan)
        spProvOD = findViewById(R.id.spProvOD)
        spKabOD = findViewById(R.id.spKabOD)
        spKecOD = findViewById(R.id.spKecOD)
        spDesaOD = findViewById(R.id.spDesaOD)
        etSearchOD = findViewById(R.id.etSearchOD)
        rvListOwnerDO = findViewById(R.id.rvListOwnerDO)
        tvDraftDO = findViewById(R.id.tvDraftDO)
        lyDraftDO = findViewById(R.id.lyDraftDO)
        lyUnverifiedDO = findViewById(R.id.lyUnverifiedDO)
        tvUnverifiedDO = findViewById(R.id.tvUnverifiedDO)
        lyRejectedDO = findViewById(R.id.lyRejectedDO)
        tvRejectedDO = findViewById(R.id.tvRejectedDO)

        spProvOD.visibility = View.GONE
        spKabOD.visibility = View.GONE
        spKecOD.visibility = View.GONE
        spDesaOD.visibility = View.GONE
        etSearchOD.visibility = View.GONE

        spProvOD.isEnabled = false
        spKabOD.isEnabled = false
        spKecOD.isEnabled = false
        spDesaOD.isEnabled = false
        etSearchOD.isEnabled = false
        
        defProv.add(ProvinsiEntity(0, "PILIH PROVINSI"))
        defProv.add(ProvinsiEntity(1, "KALIMANTAN BARAT"))

        provAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defProv.map { it.nama })
        provAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProvOD.adapter = provAdapter
        
        kabAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKab)
        kabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKabOD.adapter = kabAdapter
        
        kecAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKec)
        kecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKecOD.adapter = kecAdapter
        
        desaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defDesa)
        desaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDesaOD.adapter = desaAdapter
        
        adapterOwner = OwnerDataAdapter(
            ListOwner,
            onCallClick = { ownerEntity ->
                showCallDialog(ownerEntity)
            },
            onItemClick = { ownerEntity ->
                val i = Intent(this@DataOwner, DataOwnerDetails::class.java)
                i.putExtra("komoditas", komoditas)
                i.putExtra("id", ownerEntity.id.toString())
                startActivity(i)
            },
            onEditClick = { ownerEntity ->
                val i = Intent(this@DataOwner, DataOwnerCreateEdit::class.java)
                i.putExtra("komoditas", komoditas)
                i.putExtra("mode", "edit")
                i.putExtra("id", ownerEntity.id.toString())
                startActivity(i)
            },
            onDeleteClick = { ownerEntity ->
                AlertDialog.Builder(this@DataOwner)
                    .setTitle("Hapus Data Pemilik Lahan")
                    .setMessage("Apakah Anda yakin ingin menghapus data pemilik lahan?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            deleteData(ownerEntity)
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onKirimDataKeServerUpdateClick = { ownerEntity ->
                AlertDialog.Builder(this@DataOwner)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin mengirim data pemilik lahan ke server?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        if(isOnline(this@DataOwner)){
                            lifecycleScope.launch {
                                saveDataToServerUpdate(ownerEntity)
                            }
                        }else{
                            showError(this@DataOwner,"Error", "Tidak ada koneksi internet!")
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            },
            onKirimDataKeServerCreateClick = { ownerEntity ->
                AlertDialog.Builder(this@DataOwner)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin mengirim data pemilik lahan ke server?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        if(isOnline(this@DataOwner)){
                            lifecycleScope.launch {
                                saveDataToServer(ownerEntity)
                            }
                        }else{
                            showError(this@DataOwner,"Error", "Tidak ada koneksi internet!")
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            }
        )

        rvListOwnerDO.adapter = adapterOwner
        rvListOwnerDO.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"

        etSearchOD.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val query = s.toString()
                val tvData = findViewById<TextView>(R.id.tvTotalDataOD)

                if (query.isEmpty()) {
                    ListOwner.clear()
                    ListOwner.addAll(recentListOwner)
                    tvData.text = "Hasil Pencarian (${recentListOwner.size})"
                } else {
                    val filteredList = recentListOwner.filter {
                        it.nama.contains(query, ignoreCase = true) || it.nama_pok.contains(query, ignoreCase = true)
                    }
                    ListOwner.clear()
                    ListOwner.addAll(filteredList)
                    tvData.text = "Hasil Pencarian (${filteredList.size})"
                }

                adapterOwner.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        swlDataOwnerDO.setOnRefreshListener {
            val intent = intent
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }

        val nsvDataOwner = findViewById<NestedScrollView>(R.id.nsvDataOwner)
        nsvDataOwner.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            swlDataOwnerDO.isEnabled = scrollY == 0
        }

        lifecycleScope.launch {
            loadBasicData()
        }

        spProvOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                etSearchOD.visibility = View.GONE
                etSearchOD.clearFocus()
                etSearchOD.text = null
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearchOD.windowToken, 0)
                defSelectedProv = defProv[position].id
                if(position != 0){
                    lifecycleScope.launch {
                        loadKabupaten(defSelectedProv)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spKabOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                etSearchOD.visibility = View.GONE
                etSearchOD.clearFocus()
                etSearchOD.text = null
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearchOD.windowToken, 0)
                defSelectedKab = defKab[position].id
                if(position != 0) {
                    lifecycleScope.launch {
                        loadKecamatan(defSelectedKab!!)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spKecOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                etSearchOD.visibility = View.GONE
                etSearchOD.clearFocus()
                etSearchOD.text = null
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearchOD.windowToken, 0)
                defSelectedKec = defKec[position].id
                if(position != 0) {
                    lifecycleScope.launch {
                        loadDesa(defSelectedKec!!)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spDesaOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                etSearchOD.visibility = View.GONE
                etSearchOD.clearFocus()
                etSearchOD.text = null
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearchOD.windowToken, 0)
                defSelectedDesa = defDesa[position].id
                if(position != 0) {
                    if (isOnline(this@DataOwner)) {
                        lifecycleScope.launch {
                            loadOnlineData(defSelectedDesa!!)
                        }
                    } else {
                        lifecycleScope.launch {
                            loadOfflineData(defSelectedDesa!!)
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private suspend fun loadBasicData(){
        loadKabupaten(defSelectedProv)
        val role = RoleHelper(this@DataOwner)
        val total = when (role.role) {
            in PROV_TYPE -> dbOwner.countByProvinsi(komoditas, role.id)
            in KAB_TYPE -> dbOwner.countByKabupaten(komoditas, role.id)
            in KEC_TYPE -> dbOwner.countByKecamatanIds(komoditas, role.ids)
            in DESA_TYPE -> dbOwner.countByDesa(komoditas, role.id)
            else -> 0
        }

        if(role.role == "ADMINPOLRES"){
            val totalunverified = dbOwner.getUnverifiedData(komoditas).size
            if(totalunverified > 0){
                lyUnverifiedDO.visibility = View.VISIBLE
                tvUnverifiedDO.text = "Terdapat $totalunverified Data Pemilik Lahan belum diverifikasi, silahkan klik peringatan ini untuk membuka list data!"
                lyUnverifiedDO.setOnClickListener {
                    val i = Intent(this@DataOwner, VerifikasiOwner::class.java)
                    i.putExtra("komoditas", komoditas)
                    startActivity(i)
                }
                tvUnverifiedDO.setOnClickListener {
                    val i = Intent(this@DataOwner, VerifikasiOwner::class.java)
                    i.putExtra("komoditas", komoditas)
                    startActivity(i)
                }
            }else{
                lyUnverifiedDO.visibility = View.GONE
            }
        }

        if(role.role in listOf("ADMINPOLSEK", "BPKP")){
            val totalRejected = dbOwner.getRejectedData(komoditas).size
            if(totalRejected > 0){
                lyRejectedDO.visibility = View.VISIBLE
                tvRejectedDO.text = "Terdapat $totalRejected Data Pemilik Lahan yang ditolak oleh Admin, silahkan klik peringatan ini untuk membuka list data!"
            }
            lyRejectedDO.setOnClickListener {
                val i = Intent(this@DataOwner, RejectedOwner::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }

            tvRejectedDO.setOnClickListener {
                val i = Intent(this@DataOwner, RejectedOwner::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        }

        val totalOffline = dbOwner.getOfflineData(komoditas).size
        if(totalOffline > 0){
            lyDraftDO.visibility = View.VISIBLE
            tvDraftDO.text = "Terdapat $totalOffline Data Pemilik Lahan belum anda kirim ke server, silahkan klik peringatan ini untuk membuka list draft!"
            lyDraftDO.setOnClickListener {
                val i = Intent(this@DataOwner, DraftOwner::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
            tvDraftDO.setOnClickListener {
                val i = Intent(this@DataOwner, DraftOwner::class.java)
                i.putExtra("komoditas", komoditas)
                startActivity(i)
            }
        }else{
            lyDraftDO.visibility = View.GONE
        }
        tvTotalOwnerDO.text = total.toString()

        fabAddPemilikLahan.setOnClickListener {
            if (role.role == "BPKP" || role.role.contains("ADMIN", ignoreCase = true)) {
                val i = Intent(this@DataOwner, DataOwnerCreateEdit::class.java)
                i.putExtra("komoditas", komoditas)
                i.putExtra("mode", "create")
                startActivity(i)
            } else {
                AlertDialog.Builder(this@DataOwner)
                    .setTitle("Akses Ditolak")
                    .setMessage("Hanya Bintara Penggerak dan Admin yang dapat menambahkan data!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setIcon(R.drawable.outline_warning_24)
                    .show()
            }
        }
    }

    private suspend fun loadKabupaten(provinsiId: Int) {
        try {
            defSelectedKec = null
            defSelectedDesa = null

            val temp = dbWilayah.getKabupatenByProvinsi(provinsiId)
            Log.d("DataOwner", "Jumlah Kabupaten: ${temp.size}\n${temp.toString()}")

            if (getMyRole(this@DataOwner) in PROV_TYPE) {
                defKab.apply {
                    clear()
                    add(KabupatenEntity(0, "PILIH KABUPATEN", 0))
                    addAll(temp)
                }
                spKabOD.setSelection(0)
                spKabOD.visibility = View.VISIBLE
                spKabOD.isEnabled = true
                kabAdapter.notifyDataSetChanged()
            } else {
                val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                val kabupatenId = sh.getInt("satker_kabupatenId", 0)

                val selectedKabupaten = temp.find { it.id == kabupatenId }
                if (selectedKabupaten != null) {
                    spProvOD.visibility = View.GONE
                    defSelectedKab = selectedKabupaten.id
                    loadKecamatan(selectedKabupaten.id)
                } else {
                    showError(this@DataOwner,"Error", "Kabupaten dengan ID $kabupatenId tidak ditemukan."){
                        finish()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DataOwner", "Gagal load kabupaten: ${e.message}", e)
            showError(this@DataOwner,"Error", "Gagal load kabupaten: ${e.message}"){
                finish()
            }
        }
    }

    private suspend fun loadKecamatan(kabupatenId: Int) {
        try {
            defSelectedDesa = null
            val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
            val myRole = getMyRole(this@DataOwner)
            Log.d("DataOwner", "Role: $myRole")
            val temp = dbWilayah.getKecamatanByKabupaten(kabupatenId)
            Log.d("DataOwner", "Jumlah Kecamatan: ${temp.size}\n${temp.toString()}")

            when (myRole) {
                in PROV_TYPE, in KAB_TYPE -> {
                    Log.d("DataOwner", "Role: $myRole merupakan PROV_TYPE atau KAB_TYPE")
                    defKec.apply {
                        clear()
                        add(KecamatanEntity(0, "PILIH KECAMATAN", 0))
                        addAll(temp)
                    }
                    spKecOD.apply {
                        setSelection(0)
                        visibility = View.VISIBLE
                        isEnabled = true
                    }
                    kecAdapter.notifyDataSetChanged()
                }
                in KEC_TYPE -> {
                    Log.d("DataOwner", "Role: $myRole merupakan KEC_TYPE")
                    val polsekId = sh.getInt("satker_id", 0)
                    val kecList = dbWilayah.getDataKecamatanByPolsekId(polsekId)
                    val selectedKecamatan = temp.filter { data -> kecList.any { it.id == data.id } }

                    defKec.apply {
                        clear()
                        add(KecamatanEntity(0, "PILIH KECAMATAN", 0))
                        addAll(selectedKecamatan)
                    }

                    spKecOD.apply {
                        setSelection(0)
                        visibility = View.VISIBLE
                        isEnabled = true
                    }

                    kecAdapter.notifyDataSetChanged()
                }
                in DESA_TYPE -> {
                    Log.d("DataOwner", "Role: $myRole bukan merupakan PROV_TYPE, KAB_TYPE, KEC_TYPE")
                    val kecamatanId = sh.getInt("kecamatan_id", 0)
                    val selectedKecamatan = temp.find { it.id == kecamatanId }

                    if (selectedKecamatan != null) {
                        defSelectedKec = selectedKecamatan.id
                        spKecOD.visibility = View.GONE
                        loadDesa(selectedKecamatan.id)
                    } else {
                        showError(this@DataOwner, "Error", "Kecamatan dengan ID $kecamatanId tidak ditemukan.") {
                            finish()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("DataOwner", "Gagal load Kecamatan: ${e.message}", e)
            showError(this@DataOwner, "Error", "Gagal load Kecamatan: ${e.message}") {
                finish()
            }
        }
    }

    private suspend fun loadDesa(kecamatanId: Int) {
        try {
            val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
            val myRole = getMyRole(this@DataOwner)
            val temp = dbWilayah.getDesaByKecamatan(kecamatanId)
            Log.d("DataOwner", "Jumlah Desa: ${temp.size}\n${temp.toString()}")

            if (temp.isEmpty()) {
                showError(this@DataOwner, "Data Kosong", "Tidak ada desa yang ditemukan untuk kecamatan ini.") {
                    finish()
                }
                return
            }

            when (myRole) {
                in PROV_TYPE, in KAB_TYPE, in KEC_TYPE -> {
                    defDesa.apply {
                        clear()
                        add(DesaEntity(0, "PILIH DESA", 0))
                        addAll(temp)
                    }

                    spDesaOD.apply {
                        setSelection(0)
                        visibility = View.VISIBLE
                        isEnabled = true
                    }

                    desaAdapter.notifyDataSetChanged()
                }

                else -> {
                    val desaId = sh.getInt("desa_id", 0)
                    val selectedDesa = temp.find { it.id == desaId }

                    if (selectedDesa != null) {
                        defSelectedDesa = selectedDesa.id
                        spDesaOD.visibility = View.GONE
                        if(isOnline(this@DataOwner)){
                            lifecycleScope.launch {
                                loadOnlineData(selectedDesa.id)
                            }
                        }else{
                            lifecycleScope.launch {
                                loadOfflineData(selectedDesa.id)
                            }
                        }
                    } else {
                        showError(this@DataOwner, "Error", "Desa dengan ID $desaId tidak ditemukan.") {
                            finish()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("DataOwner", "Gagal load Desa: ${e.message}", e)
            showError(this@DataOwner, "Error", "Gagal load Desa: ${e.message}") {
                finish()
            }
        }
    }

    private suspend fun loadOnlineData(id: Int){
        swlDataOwnerDO.isRefreshing = true
        try{
            val res = Client.retrofit.create(OwnerEndpoint::class.java).getAllOwner("desa", OwnerEndpoint.RequestIds(listOf(id)))
            if (res.isSuccessful && res.body() != null) {
                val data = res.body()

                val ownerList = data?.map { response ->
                    OwnerEntity(
                        id = response.id!!,
                        komoditas = response.komoditas!!,
                        type = TypeOwner.valueOf(response.type!!),
                        gapki = IsGapki.valueOf(response.gapki!!),
                        nama_pok = response.namaPok!!,
                        nama = response.nama!!,
                        nik = response.nik!!,
                        alamat = response.alamat!!,
                        telepon = response.telepon!!,
                        provinsi_id = response.provinsiId!!,
                        kabupaten_id = response.kabupatenId!!,
                        kecamatan_id = response.kecamatanId!!,
                        desa_id = response.desaId!!,
                        status = response.status!!,
                        alasan = response.alasan,
                        createAt = parseIsoDate(response.createAt!!) ?: Date(),
                        updatedAt = parseIsoDate(response.updatedAt!!) ?: Date(),
                        submitter = response.submitter!!
                    )
                }

                ownerList?.let { dbOwner.updateSomeOwnerData(it) }
            }
        }catch (e: Exception){
            Log.e("DataOwner", "${e.message}")
        }finally {
            loadOfflineData(id)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadOfflineData(id : Int){
        try{
            val data = dbOwner.getOwnerByDesa(komoditas, id)
            ListOwner.clear()
            ListOwner.addAll(data)
            recentListOwner.clear()
            recentListOwner.addAll(data)
            withContext(Dispatchers.Main) {
                val tvData = findViewById<TextView>(R.id.tvTotalDataOD)
                tvData.text = "Hasil Pencarian (${ListOwner.size})"
                etSearchOD.visibility = View.VISIBLE
                etSearchOD.isEnabled = true
                adapterOwner.notifyDataSetChanged()
            }
        }catch (e: Exception){
            Log.e("DataOwner", "${e.message}")
        }finally {
            swlDataOwnerDO.isRefreshing = false
        }
    }

    private fun showCallDialog(owner: OwnerEntity) {
        val nomor = owner.telepon.trim()
        if (nomor.isBlank()) {
            Toast.makeText(this@DataOwner, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this@DataOwner)
            .setTitle("Pilih Metode Panggilan")
            .setItems(arrayOf("Telepon Seluler", "Telepon WhatsApp")) { _, which ->
                when (which) {
                    0 -> {
                        // Telepon Biasa
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = "tel:$nomor".toUri()
                        startActivity(intent)
                    }
                    1 -> {
                        // WhatsApp
                        val uri =
                            "https://wa.me/${nomor.replaceFirst("^0".toRegex(), "62")}".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.whatsapp")
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@DataOwner, "WhatsApp tidak terinstal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setIcon(R.drawable.accept_call_icon)
            .setNegativeButton("Batal", null)
            .show()
    }

    private suspend fun deleteData(ownerEntity: OwnerEntity) {
        Loading.show(this)
        try {
            if(isOnline(this)){
                val res = Client.retrofit.create(OwnerEndpoint::class.java).deleteOwner(ownerEntity.id.toString())
                if(res.isSuccessful && res.body() != null){
                    Loading.hide()
                    dbOwner.delete(ownerEntity)
                    AlertDialog.Builder(this)
                        .setTitle("Berhasil")
                        .setMessage(res.body()?.msg ?: "Data berhasil dihapus")
                        .setIcon(R.drawable.green_checkmark_line_icon)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            val intent = intent
                            overridePendingTransition(0, 0)
                            finish()
                            overridePendingTransition(0, 0)
                            startActivity(intent)
                        }
                        .show()
                }else{
                    Loading.hide()
                    showError(this, "Error", res.body()?.msg ?: "Terjadi Kesalahan"){
                        val intent = intent
                        overridePendingTransition(0, 0)
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                    }
                }
            }else{
                Loading.hide()
                showError(this, "Error", "Tidak ada koneksi internet!"){
                    val intent = intent
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this, "Error",e.message.toString()){
                val intent = intent
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadBasicData()
            if(isOnline(this@DataOwner)){
                defSelectedDesa?.let { loadOnlineData(it) }
            }else{
                defSelectedDesa?.let { loadOfflineData(it) }
            }
        }
    }

    private suspend fun saveDataToServer(owner: OwnerEntity) {
        Loading.show(this@DataOwner)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            val result = api.addOwner(OwnerAddRequest(
                type = owner.type.name,
                gapki = owner.gapki.name,
                namaPok = owner.nama_pok,
                nama = owner.nama,
                nik = owner.nik,
                alamat = owner.alamat,
                telepon = owner.telepon,
                provinsiId = owner.provinsi_id.toString(),
                kabupatenId = owner.kabupaten_id.toString(),
                kecamatanId = owner.kecamatan_id.toString(),
                desaId = owner.desa_id.toString(),
                status = "UNVERIFIED",
                komoditas = owner.komoditas,
                submitter = owner.submitter,
                role = getMyRole(this@DataOwner)
            ))

            if(result.isSuccessful && result.body() != null){
                Loading.hide()
                val data = result.body()?.data
                data?.let { response ->
                    val data = OwnerEntity(
                        id = response.id!!,
                        komoditas = response.komoditas!!,
                        type = TypeOwner.valueOf(response.type!!),
                        gapki = IsGapki.valueOf(response.gapki!!),
                        nama_pok = response.namaPok!!,
                        nama = response.nama!!,
                        nik = response.nik!!,
                        alamat = response.alamat!!,
                        telepon = response.telepon!!,
                        provinsi_id = response.provinsiId!!,
                        kabupaten_id = response.kabupatenId!!,
                        kecamatan_id = response.kecamatanId!!,
                        desa_id = response.desaId!!,
                        status = response.status!!,
                        alasan = response.alasan,
                        createAt = response.createAt?.let { parseIsoDate(it) } ?: Date(),
                        updatedAt = response.updatedAt?.let { parseIsoDate(it) } ?: Date(),
                        submitter = response.submitter!!
                    )
                    val res = dbOwner.updateOwnerDatas(data)
                    if (res > 0) {
                        showSuccess("Berhasil",result.body()?.msg.toString())
                    }else{
                        showError(this@DataOwner, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                            val intent = intent
                            overridePendingTransition(0, 0)
                            finish()
                            overridePendingTransition(0, 0)
                            startActivity(intent)
                        }
                    }
                }
            }else{
                Loading.hide()
                showError(this@DataOwner, "Error", result.body()?.msg.toString()){
                    val intent = intent
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this@DataOwner, "Error", e.message.toString()){
                val intent = intent
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
        }
    }

    private suspend fun saveDataToServerUpdate(owner: OwnerEntity) {
        Loading.show(this@DataOwner)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            val result = api.updateOwner(owner.id.toString(), OwnerPatchRequest(
                type = owner.type.name,
                gapki = owner.gapki.name,
                namaPok = owner.nama_pok,
                nama = owner.nama,
                nik = owner.nik,
                alamat = owner.alamat,
                telepon = owner.telepon,
                provinsiId = owner.provinsi_id.toString(),
                kabupatenId = owner.kabupaten_id.toString(),
                kecamatanId = owner.kecamatan_id.toString(),
                desaId = owner.desa_id.toString(),
                status = "UNVERIFIED",
                komoditas = owner.komoditas,
                submitter = owner.submitter,
                role = getMyRole(this@DataOwner)
            ))

            if(result.isSuccessful && result.body() != null){
                Loading.hide()
                val data = result.body()?.data
                data?.let { response ->
                    val data = OwnerEntity(
                        id = response.id!!,
                        komoditas = response.komoditas!!,
                        type = TypeOwner.valueOf(response.type!!),
                        gapki = IsGapki.valueOf(response.gapki!!),
                        nama_pok = response.namaPok!!,
                        nama = response.nama!!,
                        nik = response.nik!!,
                        alamat = response.alamat!!,
                        telepon = response.telepon!!,
                        provinsi_id = response.provinsiId!!,
                        kabupaten_id = response.kabupatenId!!,
                        kecamatan_id = response.kecamatanId!!,
                        desa_id = response.desaId!!,
                        status = response.status!!,
                        alasan = response.alasan,
                        createAt = response.createAt?.let { parseIsoDate(it) } ?: Date(),
                        updatedAt = response.updatedAt?.let { parseIsoDate(it) } ?: Date(),
                        submitter = response.submitter!!
                    )
                    val res = dbOwner.updateOwnerDatas(data)
                    if (res > 0) {
                        showSuccess("Berhasil",result.body()?.msg.toString())
                    }else{
                        showError(this@DataOwner, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                            val intent = intent
                            overridePendingTransition(0, 0)
                            finish()
                            overridePendingTransition(0, 0)
                            startActivity(intent)
                        }
                    }
                }
            }else{
                Loading.hide()
                showError(this@DataOwner, "Error", result.body()?.msg.toString()){
                    val intent = intent
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this@DataOwner, "Error", e.message.toString()){
                val intent = intent
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
        }
    }

    private fun showSuccess(title: String, msg: String){
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setIcon(R.drawable.green_checkmark_line_icon)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = intent
                overridePendingTransition(0, 0)
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
            }
            .show()
    }
}