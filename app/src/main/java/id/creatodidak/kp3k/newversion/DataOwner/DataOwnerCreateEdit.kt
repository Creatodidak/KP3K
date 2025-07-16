package id.creatodidak.kp3k.newversion.DataOwner

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.creatodidak.kp3k.R
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
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class DataOwnerCreateEdit : AppCompatActivity() {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")
    private lateinit var db : AppDatabase
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var komoditas : String
    private lateinit var mode : String
    private var ownerId : String? = null

    private lateinit var spTypeOD: Spinner
    private lateinit var spGapkiOD: Spinner
    private lateinit var spAddProvOD: Spinner
    private lateinit var spAddKabOD: Spinner
    private lateinit var spAddKecOD: Spinner
    private lateinit var spAddDesaOD: Spinner
    private lateinit var etNamaKelompokDO: TextInputEditText
    private lateinit var etNamaDO: TextInputEditText
    private lateinit var etNikDO: TextInputEditText
    private lateinit var etAlamatDO: TextInputEditText
    private lateinit var etTeleponDO: TextInputEditText
    private lateinit var etlyNamaKelompokDO: TextInputLayout
    private lateinit var etlyNamaDO: TextInputLayout
    private lateinit var etlyNikDO: TextInputLayout
    private lateinit var etlyAlamatDO: TextInputLayout
    private lateinit var etlyTeleponDO: TextInputLayout
    private lateinit var btnAddKirimDataOD: Button

    private var defType = mutableListOf<String>()
    private var defGapki = mutableListOf<String>()
    private var defProv = mutableListOf<ProvinsiEntity>()
    private var defKab = mutableListOf<KabupatenEntity>()
    private var defKec = mutableListOf<KecamatanEntity>()
    private var defDesa = mutableListOf<DesaEntity>()

    private lateinit var typeAdapter: ArrayAdapter<String>
    private lateinit var gapkiAdapter: ArrayAdapter<String>
    private lateinit var provAdapter: ArrayAdapter<String>
    private lateinit var kabAdapter: ArrayAdapter<KabupatenEntity>
    private lateinit var kecAdapter: ArrayAdapter<KecamatanEntity>
    private lateinit var desaAdapter: ArrayAdapter<DesaEntity>

    private var defSelectedType : String? = null
    private var defSelectedGapki : String? = null
    private var defSelectedProv : Int = 1
    private var defSelectedKab : Int? = null
    private var defSelectedKec : Int? = null
    private var defSelectedDesa : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_owner_create_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        mode = intent.getStringExtra("mode").toString()
        ownerId = intent.getStringExtra("id").toString()

        defType.add("PILIH TYPE PEMILIK")
        defType.addAll(TypeOwner.entries.map { it.name })
        defGapki.add("STATUS ANGGOTA GAPKI")
        defGapki.addAll(IsGapki.entries.map { it.name })
        defProv.add(ProvinsiEntity(0, "PILIH PROVINSI"))
        defProv.add(ProvinsiEntity(1, "KALIMANTAN BARAT"))

        // Spinner
        spTypeOD = findViewById(R.id.spTypeOD)
        spGapkiOD = findViewById(R.id.spGapkiOD)
        spAddProvOD = findViewById(R.id.spAddProvOD)
        spAddKabOD = findViewById(R.id.spAddKabOD)
        spAddKecOD = findViewById(R.id.spAddKecOD)
        spAddDesaOD = findViewById(R.id.spAddDesaOD)

// TextInputEditText
        etNamaKelompokDO = findViewById(R.id.etNamaKelompokDO)
        etNamaDO = findViewById(R.id.etNamaDO)
        etNikDO = findViewById(R.id.etNikDO)
        etAlamatDO = findViewById(R.id.etAlamatDO)
        etTeleponDO = findViewById(R.id.etTeleponDO)

// TextInputLayout (opsional)
        etlyNamaKelompokDO = findViewById(R.id.etlyNamaKelompokDO)
        etlyNamaDO = findViewById(R.id.etlyNamaDO)
        etlyNikDO = findViewById(R.id.etlyNikDO)
        etlyAlamatDO = findViewById(R.id.etlyAlamatDO)
        etlyTeleponDO = findViewById(R.id.etlyTeleponDO)

// Button
        btnAddKirimDataOD = findViewById(R.id.btnAddKirimDataOD)

        typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defType)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTypeOD.adapter = typeAdapter

        gapkiAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defGapki)
        gapkiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGapkiOD.adapter = gapkiAdapter

        provAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defProv.map { it.nama })
        provAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAddProvOD.adapter = provAdapter

        kabAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKab)
        kabAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAddKabOD.adapter = kabAdapter

        kecAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defKec)
        kecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAddKecOD.adapter = kecAdapter

        desaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defDesa)
        desaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAddDesaOD.adapter = desaAdapter

        spAddProvOD.isEnabled = false
        spAddKabOD.isEnabled = false
        spAddKecOD.isEnabled = false
        spAddDesaOD.isEnabled = false

        spAddProvOD.setSelection(1)

        etNikDO.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nik = s?.toString().orEmpty()

                if (nik.isEmpty() || nik.length == 16) {
                    etlyNikDO.error = null
                } else {
                    etlyNikDO.error = "Kurang ${16 - nik.length} digit"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        if (mode == "edit" && ownerId != null) {
            lifecycleScope.launch {
                val ownerData = dbOwner.getOwnerById(ownerId!!.toInt())
                ownerData?.let { populateFormWithData(it) }
            }
        }else{
            lifecycleScope.launch {
                loadKabupaten(1)
            }

            spAddKabOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    defSelectedKab = defKab[position].id
                    if(position != 0){
                        lifecycleScope.launch {
                            loadKecamatan(defKab[position].id)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            spAddKecOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    defSelectedKec = defKec[position].id
                    if(position != 0) {
                        lifecycleScope.launch {
                            loadDesa(defKec[position].id)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }


            spAddDesaOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                        defSelectedDesa = defDesa[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        btnAddKirimDataOD.setOnClickListener {
            if(isFormValid()){
                lifecycleScope.launch {
                    saveDataToLocal()
                }
            }else{
                Toast.makeText(this, "Form Invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadKabupaten(provinsiId: Int) {
        try {
            // Reset pilihan
            defSelectedKec = null
            defSelectedDesa = null

            // Clear & add default item
            defKab.apply {
                clear()
                add(KabupatenEntity(0, "PILIH KABUPATEN", 0))
                val temp = dbWilayah.getKabupatenByProvinsi(provinsiId)
                when(getMyRole(this@DataOwnerCreateEdit)){
                    in PROV_TYPE -> addAll(temp)
                    in KAB_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val kabupatenId = sh.getInt("satker_kabupatenId", 0)
                        if(kabupatenId != null){add(temp.find { it.id == kabupatenId }!!)} else {
                            Log.w("RoleCheck", "Kabupaten dengan ID $kabupatenId tidak ditemukan di list temp.")
                        }
                    }
                    in KEC_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val kabupatenId = sh.getInt("satker_kabupatenId", 0)
                        if(kabupatenId != null){add(temp.find { it.id == kabupatenId }!!)} else {
                            Log.w("RoleCheck", "Kabupaten dengan ID $kabupatenId tidak ditemukan di list temp.")
                        }
                    }
                    in DESA_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val kabupatenId = sh.getInt("satker_kabupatenId", 0)
                        if(kabupatenId != null){add(temp.find { it.id == kabupatenId }!!)} else {
                            Log.w("RoleCheck", "Kabupaten dengan ID $kabupatenId tidak ditemukan di list temp.")
                        }
                    }
                }
            }

            defKec.apply {
                clear()
                add(KecamatanEntity(0, "PILIH KECAMATAN", 0))
            }

            defDesa.apply {
                clear()
                add(DesaEntity(0, "PILIH DESA", 0))
            }

            // Reset spinner selection ke awal
            spAddKabOD.setSelection(0)
            spAddKecOD.setSelection(0)
            spAddDesaOD.setSelection(0)

            // Notify semua adapter
            kabAdapter.notifyDataSetChanged()
            kecAdapter.notifyDataSetChanged()
            desaAdapter.notifyDataSetChanged()

            spAddKabOD.isEnabled = true
            Log.d("DataOwnerCreateEdit", "loadKabupaten: ${defKab.map { it.nama }}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadKecamatan(kabupatenId: Int) {
        try {
            defSelectedDesa = null

            defKec.apply {
                clear()
                add(KecamatanEntity(0, "PILIH KECAMATAN", 0))
                val temp = dbWilayah.getKecamatanByKabupaten(kabupatenId)
                when(getMyRole(this@DataOwnerCreateEdit)){
                    in PROV_TYPE -> addAll(temp)
                    in KAB_TYPE -> addAll(temp)
                    in KEC_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val polsekId = sh.getInt("satkerId", 0)
                        val kecamatanIds = dbWilayah.getKecamatanIdByPolsekId(polsekId)
                        val kecIdList = kecamatanIds.map { it.kecamatanId }
                        if (kecIdList.isNotEmpty()) {
                            addAll(temp.filter { it.id in kecIdList })
                        }
                    }
                    in DESA_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val kecamatanId = sh.getInt("kecamatan_id", 0)
                        if(kecamatanId != null){add(temp.find { it.id == kecamatanId }!!)} else {
                            Log.w("RoleCheck", "Kecamatan dengan ID $kecamatanId tidak ditemukan di list temp.")
                        }
                    }
                }
            }

            defDesa.apply {
                clear()
                add(DesaEntity(0, "PILIH DESA", 0))
            }

            spAddKecOD.setSelection(0)
            spAddDesaOD.setSelection(0)

            kecAdapter.notifyDataSetChanged()
            desaAdapter.notifyDataSetChanged()

            spAddKecOD.isEnabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadDesa(kecamatanId: Int) {
        try {
            defDesa.apply {
                clear()
                add(DesaEntity(0, "PILIH DESA", 0))
                val temp = dbWilayah.getDesaByKecamatan(kecamatanId)
                when(getMyRole(this@DataOwnerCreateEdit)){
                    in PROV_TYPE -> addAll(temp)
                    in KAB_TYPE -> addAll(temp)
                    in KEC_TYPE -> addAll(temp)
                    in DESA_TYPE -> {
                        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        val desaId = sh.getInt("desa_id", 0)
                        if(desaId != null){add(temp.find { it.id == desaId }!!)} else {
                            Log.w("RoleCheck", "Desa dengan ID $desaId tidak ditemukan di list temp.")
                        }
                    }
                }
            }

            spAddDesaOD.setSelection(0)

            desaAdapter.notifyDataSetChanged()
            spAddDesaOD.isEnabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isFormValid(): Boolean {
        var isValid = true

        // Validasi Spinner
        if (spTypeOD.selectedItemPosition == 0) {
            isValid = false
            (spTypeOD.selectedView as? TextView)?.error = "Harus dipilih"
        }

        if (spGapkiOD.selectedItemPosition == 0) {
            isValid = false
            (spGapkiOD.selectedView as? TextView)?.error = "Harus dipilih"
        }

        if (spAddKabOD.selectedItemPosition == 0) {
            isValid = false
            (spAddKabOD.selectedView as? TextView)?.error = "Harus dipilih"
        }

        if (spAddKecOD.selectedItemPosition == 0) {
            isValid = false
            (spAddKecOD.selectedView as? TextView)?.error = "Harus dipilih"
        }

        if (spAddDesaOD.selectedItemPosition == 0) {
            isValid = false
            (spAddDesaOD.selectedView as? TextView)?.error = "Harus dipilih"
        }

        // Validasi TextInput
        if (etNamaKelompokDO.text.isNullOrBlank()) {
            etlyNamaKelompokDO.error = "Nama kelompok wajib diisi"
            isValid = false
        } else {
            etlyNamaKelompokDO.error = null
        }

        if (etNamaDO.text.isNullOrBlank()) {
            etlyNamaDO.error = "Nama wajib diisi"
            isValid = false
        } else {
            etlyNamaDO.error = null
        }

        if (etNikDO.text.isNullOrBlank()) {
            etlyNikDO.error = "NIK wajib diisi"
            isValid = false
        } else if (etNikDO.text!!.length != 16) {
            etlyNikDO.error = "NIK harus 16 digit"
            isValid = false
        } else if (!etNikDO.text!!.all { it.isDigit() }) {
            etlyNikDO.error = "NIK hanya boleh berisi angka"
            isValid = false
        } else {
            etlyNikDO.error = null
        }

        if (etAlamatDO.text.isNullOrBlank()) {
            etlyAlamatDO.error = "Alamat wajib diisi"
            isValid = false
        } else {
            etlyAlamatDO.error = null
        }

        if (etTeleponDO.text.isNullOrBlank()) {
            etlyTeleponDO.error = "Nomor telepon wajib diisi"
            isValid = false
        } else {
            etlyTeleponDO.error = null
        }

        return isValid
    }

    private suspend fun populateFormWithData(owner: OwnerEntity) {
        etNamaKelompokDO.setText(owner.nama_pok)
        etNamaDO.setText(owner.nama)
        etNikDO.setText(owner.nik)
        etAlamatDO.setText(owner.alamat)
        etTeleponDO.setText(owner.telepon)

        spTypeOD.setSelection(defType.indexOf(owner.type.name))
        spGapkiOD.setSelection(defGapki.indexOf(owner.gapki.name))

        loadKabupaten(owner.provinsi_id)
        spAddKabOD.setSelection(defKab.indexOfFirst { it.id == owner.kabupaten_id })
        loadKecamatan(owner.kabupaten_id)
        spAddKecOD.setSelection(defKec.indexOfFirst { it.id == owner.kecamatan_id })
        loadDesa(owner.kecamatan_id)
        spAddDesaOD.setSelection(defDesa.indexOfFirst { it.id == owner.desa_id })


        defSelectedKab = owner.kabupaten_id
        defSelectedKec = owner.kecamatan_id
        defSelectedDesa = owner.desa_id

        btnAddKirimDataOD.text = "Update Data"
        spAddKabOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position != 0){
                    defSelectedKab = defKab[position].id
                    lifecycleScope.launch {
                        loadKecamatan(defKab[position].id)
                    }
                }else{
                    spAddKecOD.isEnabled = false
                    spAddDesaOD.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spAddKecOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position != 0) {
                    defSelectedKec = defKec[position].id
                    lifecycleScope.launch {
                        loadDesa(defKec[position].id)
                    }
                }else{
                    spAddDesaOD.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


        spAddDesaOD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position != 0) {
                    defSelectedDesa = defDesa[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private suspend fun saveDataToLocal() {
        try {
            if (mode == "create") {
                val lastId = dbOwner.getLastId() ?: 0
                val data = OwnerEntity(
                    id = lastId+1,
                    type = TypeOwner.valueOf(spTypeOD.selectedItem.toString()),
                    gapki = IsGapki.valueOf(spGapkiOD.selectedItem.toString()),
                    nama_pok = etNamaKelompokDO.text.toString().uppercase(),
                    nama = etNamaDO.text.toString().uppercase(),
                    nik = etNikDO.text.toString(),
                    alamat = etAlamatDO.text.toString().uppercase(),
                    telepon = etTeleponDO.text.toString(),
                    provinsi_id = defSelectedProv,
                    kabupaten_id = defSelectedKab!!,
                    kecamatan_id = defSelectedKec!!,
                    desa_id = defSelectedDesa!!,
                    komoditas = komoditas,
                    createAt = Date(),
                    updatedAt = Date(),
                    status = "OFFLINECREATE",
                    alasan = null,
                    submitter = getMyNrp(this@DataOwnerCreateEdit)
                )

                val result = dbOwner.insertOwnerData(data)
                if (result > 0) {
                    saveDataToServer(data, "create")
                } else {
                    showError(this, "Error", "Gagal menyimpan data ke database.")
                }
            }else{
                val data = OwnerEntity(
                    id = ownerId!!.toInt(),
                    type = TypeOwner.valueOf(spTypeOD.selectedItem.toString()),
                    gapki = IsGapki.valueOf(spGapkiOD.selectedItem.toString()),
                    nama_pok = etNamaKelompokDO.text.toString().uppercase(),
                    nama = etNamaDO.text.toString().uppercase(),
                    nik = etNikDO.text.toString(),
                    alamat = etAlamatDO.text.toString().uppercase(),
                    telepon = etTeleponDO.text.toString(),
                    provinsi_id = defSelectedProv,
                    kabupaten_id = defSelectedKab!!,
                    kecamatan_id = defSelectedKec!!,
                    desa_id = defSelectedDesa!!,
                    komoditas = komoditas,
                    createAt = Date(),
                    updatedAt = Date(),
                    status = "OFFLINEUPDATE",
                    alasan = null,
                    submitter = getMyNrp(this@DataOwnerCreateEdit)
                )

                Log.d("DataOwnerEdit", "saveDataToLocal: $data")

                val result = dbOwner.updateOwnerDatas(data)
                if (result > 0) {
                    saveDataToServer(data, "update")
                } else {
                    showError(this, "Error", "Gagal menyimpan update data ke database.")
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this@DataOwnerCreateEdit, "Error", "Gagal menyimpan data karena ${e.message}")
        }
    }

    private suspend fun saveDataToServer(owner: OwnerEntity, mode: String) {
        Loading.show(this@DataOwnerCreateEdit)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            if(mode == "create"){
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
                    submitter = getMyNrp(this@DataOwnerCreateEdit),
                    role = getMyRole(this@DataOwnerCreateEdit)
                ))

                if(result.isSuccessful && result.body() != null){
                    Loading.hide()
                    val data = result.body()?.data
                    dbOwner.delete(owner)
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
                            submitter = getMyNrp(this@DataOwnerCreateEdit)
                        )
                        val res = dbOwner.upsert(data)
                        if (res > 0) {
                            showSuccess("Berhasil",result.body()?.msg.toString())
                        }else{
                            showError(this@DataOwnerCreateEdit, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                                finish()
                            }
                        }
                    }
                }else{
                    Loading.hide()
                    showError(this@DataOwnerCreateEdit, "Error", result.body()?.msg.toString())
                }
            }else{
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
                    submitter = getMyNrp(this@DataOwnerCreateEdit),
                    role = getMyRole(this@DataOwnerCreateEdit)
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
                            submitter = getMyNrp(this@DataOwnerCreateEdit)
                        )
                        val res = dbOwner.updateOwnerDatas(data)
                        if (res > 0) {
                            showSuccess("Berhasil",result.body()?.msg.toString())
                        }else{
                            showError(this@DataOwnerCreateEdit, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                                finish()
                            }
                        }
                    }
                }else{
                    Loading.hide()
                    showError(this@DataOwnerCreateEdit, "Error", result.body()?.msg.toString())
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this@DataOwnerCreateEdit, "Error", "Data gagal disimpan di server, namun berhasil disimpan di local database sebagai draft Offline!"){
                finish()
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
            finish()
        }
        .show()
    }
}