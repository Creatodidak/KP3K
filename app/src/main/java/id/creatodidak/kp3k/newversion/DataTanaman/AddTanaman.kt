package id.creatodidak.kp3k.newversion.DataTanaman

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.newModel.DokumentasiUI
import id.creatodidak.kp3k.api.newModel.MasaTanam
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.DatePickerMode
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.SumberFoto
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.generateKuartalList
import id.creatodidak.kp3k.helper.generateMasaTanamList
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory.NewLahanEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.RequestClass.InsertDataTanam
import id.creatodidak.kp3k.api.RequestClass.ProgressRequestBody
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import id.creatodidak.kp3k.database.Entity.MediaEntity
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.MediaDraftHelper
import id.creatodidak.kp3k.helper.MediaType
import id.creatodidak.kp3k.helper.UploadProgress
import id.creatodidak.kp3k.helper.convertToServerFormat
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.helper.withStartOfDay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddTanaman : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbLahan : LahanDao
    private lateinit var dbTanaman : TanamanDao
    private lateinit var dbOwner : OwnerDao
    private lateinit var dbWilayah : WilayahDao
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView

    // Spinner
    private lateinit var spOwner: Spinner
    private lateinit var spTypeLahan: Spinner
    private lateinit var spMasaTanam: Spinner
    private lateinit var spSumber: Spinner

    // EditText
    private lateinit var etLuas: TextInputEditText
    private lateinit var etMasaTanam: TextInputEditText
    private lateinit var etTanggalTanam: TextInputEditText
    private lateinit var etPrediksiPanen: TextInputEditText
    private lateinit var etPerkiraanTanggalPanen: TextInputEditText
    private lateinit var etVarietas: TextInputEditText
    private lateinit var etKeteranganSumber: TextInputEditText

    // TextView
    private lateinit var tvConvertHektar: TextView
    private lateinit var tvConvertTon: TextView

    // Button
    private lateinit var btnKirimData: Button

    private lateinit var lyMasaTanam: LinearLayout
    private val dokumentasiList = mutableListOf<DokumentasiUI>()
    private val galeriLaunchers = mutableListOf<ActivityResultLauncher<String>>()
    private val kameraLaunchers = mutableListOf<ActivityResultLauncher<Intent>>()

    private val listOwner = mutableListOf<OwnerEntity>()
    private val listLahan = mutableListOf<NewLahanEntity>()
    private val listMasaTanam = mutableListOf<MasaTanam>()
    private val listSumber = listOf<String>("PILIH SUMBER BIBIT", "MANDIRI", "POLRI", "PEMERINTAH")

    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>
    private lateinit var lahanAdapter: ArrayAdapter<NewLahanEntity>
    private lateinit var masaTanamAdapter: ArrayAdapter<MasaTanam>
    private lateinit var sumberAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_tanaman)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)
        dbLahan = db.lahanDao()
        dbTanaman = db.tanamanDao()
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()}"
        spOwner = findViewById(R.id.spOwner)
        spTypeLahan = findViewById(R.id.spTypeLahan)
        spMasaTanam = findViewById(R.id.spMasaTanam)
        spSumber = findViewById(R.id.spSumber)

        etLuas = findViewById(R.id.etLuas)
        etMasaTanam = findViewById(R.id.etMasaTanam)
        etTanggalTanam = findViewById(R.id.etTanggalTanam)
        etPrediksiPanen = findViewById(R.id.etPrediksiPanen)
        etPerkiraanTanggalPanen = findViewById(R.id.etPerkiraanTanggalPanen)
        etVarietas = findViewById(R.id.etVarietas)
        etKeteranganSumber = findViewById(R.id.etKeteranganSumber)
        lyMasaTanam = findViewById(R.id.lyMasaTanam)
        tvConvertHektar = findViewById(R.id.tvConvertHektar)
        tvConvertTon = findViewById(R.id.tvConvertTon)

        btnKirimData = findViewById(R.id.btnKirimData)

        etMasaTanam.visibility = View.GONE

        dokumentasiList.addAll(
            listOf(
                DokumentasiUI(
                    findViewById(R.id.imageDok1),
                    findViewById(R.id.errorDok1),
                    findViewById(R.id.watermarks1),
                    findViewById(R.id.dok1),
                    findViewById(R.id.tvNrpCamera1),
                    findViewById(R.id.buttonPilihDok1)
                ),
                DokumentasiUI(
                    findViewById(R.id.imageDok2),
                    findViewById(R.id.errorDok2),
                    findViewById(R.id.watermarks2),
                    findViewById(R.id.dok2),
                    findViewById(R.id.tvNrpCamera2),
                    findViewById(R.id.buttonPilihDok2)
                ),
                DokumentasiUI(
                    findViewById(R.id.imageDok3),
                    findViewById(R.id.errorDok3),
                    findViewById(R.id.watermarks3),
                    findViewById(R.id.dok3),
                    findViewById(R.id.tvNrpCamera3),
                    findViewById(R.id.buttonPilihDok3)
                ),
                DokumentasiUI(
                    findViewById(R.id.imageDok4),
                    findViewById(R.id.errorDok4),
                    findViewById(R.id.watermarks4),
                    findViewById(R.id.dok4),
                    findViewById(R.id.tvNrpCamera),
                    findViewById(R.id.buttonPilihDok4)
                )
            )
        )

        dokumentasiList.forEachIndexed { index, dok ->

            val galeriLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    dok.imageView.setImageURI(it)
                    dok.imageView.visibility = View.VISIBLE
                    dok.watermark.visibility = View.VISIBLE
                    dok.errorText.visibility = View.GONE
                    dok.button.text = "UBAH FOTO"
                    dok.isFromCamera = false
                    dok.imagePath = it.toString()
                    dok.nrpText.text = getMyNrp(this)
                }
            }

            val kameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imagePath = result.data?.getStringExtra("imagePath") ?: return@registerForActivityResult

                    dok.imageView.visibility = View.VISIBLE
                    dok.watermark.visibility = View.GONE
                    dok.errorText.visibility = View.GONE
                    dok.button.text = "UBAH FOTO"
                    dok.isFromCamera = true
                    dok.imagePath = imagePath
                    dok.nrpText.text = getMyNrp(this)

                    Glide.with(this).load(imagePath).into(dok.imageView)
                }
            }

            galeriLaunchers.add(galeriLauncher)
            kameraLaunchers.add(kameraLauncher)

            dok.button.setOnClickListener {
                SumberFoto.show(this,
                    onGaleri = { galeriLauncher.launch("image/*") },
                    onKamera = {
                        val intent = Intent(this, CameraActivity::class.java)
                        kameraLauncher.launch(intent)
                    })
            }
        }

        lifecycleScope.launch {
            loadOwner()
        }

        ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOwner)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spOwner.adapter = ownerAdapter

        lahanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listLahan)
        lahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTypeLahan.adapter = lahanAdapter

        masaTanamAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listMasaTanam)
        masaTanamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMasaTanam.adapter = masaTanamAdapter

        sumberAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listSumber)
        sumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSumber.adapter = sumberAdapter

        etTanggalTanam.inputType = InputType.TYPE_NULL
        etTanggalTanam.isFocusable = false

        etPerkiraanTanggalPanen.inputType = InputType.TYPE_NULL
        etPerkiraanTanggalPanen.isFocusable = false


        spOwner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0){
                    val selectedOwner = listOwner[position]
                    lifecycleScope.launch {
                        loadLahan(selectedOwner)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spTypeLahan.onItemSelectedListener  = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0){
                    val selectedLahan = listLahan[position]
                    lifecycleScope.launch {
                        loadMasaTanam(selectedLahan)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spMasaTanam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val isLastItem = position == (spMasaTanam.adapter.count - 1)

                if (isLastItem) {
                    etMasaTanam.visibility = View.VISIBLE
                }else if(position > 0 && position < (spMasaTanam.adapter.count - 1)){
                    etMasaTanam.visibility = View.GONE
                    etMasaTanam.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etLuas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val luashektar = angkaIndonesia(convertToHektar(etLuas.text.toString().toDoubleOrNull() ?: 0.0))
                tvConvertHektar.text = "= $luashektar Ha (Pembulatan Desimal 2 Angka Dibelakang Koma)"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etPrediksiPanen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val prediksiTon = angkaIndonesia(convertToTon(etPrediksiPanen.text.toString().toDoubleOrNull() ?: 0.0))
                tvConvertTon.text = "= $prediksiTon Ton (Pembulatan Desimal 2 Angka Dibelakang Koma)"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etTanggalTanam.setOnClickListener {
            showCustomDatePicker(this, DatePickerMode.DAY){ d ->
                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                etTanggalTanam.setText(formattedDate)
            }
        }

        etPerkiraanTanggalPanen.setOnClickListener {
            showCustomDatePicker(this, DatePickerMode.DAY){ d ->
                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                etPerkiraanTanggalPanen.setText(formattedDate)
            }
        }

        btnKirimData.setOnClickListener {
            if(isValid()){
                lifecycleScope.launch {
                    prepareData()
                }
            }
        }
    }

    private suspend fun loadOwner() {
        Loading.show(this)
        try {
            val roleHelper = RoleHelper(this)
            val level = getMyLevel(this)
            val ownerList: List<OwnerEntity> = when (level) {
                "provinsi" -> dbOwner.getOwnerByProvinsi(komoditas, roleHelper.id)
                "kabupaten" -> dbOwner.getOwnerByKabupaten(komoditas, roleHelper.id)
                "kecamatan" -> dbOwner.getOwnerByKecamatans(komoditas, roleHelper.ids)
                "desa" -> dbOwner.getOwnerByDesa(komoditas, roleHelper.id)
                else -> emptyList()
            }
            listOwner.clear()
            if (ownerList.isNotEmpty()) {
                listOwner.add(
                    OwnerEntity(
                        0, TypeOwner.PRIBADI, IsGapki.TIDAK, "", "PILIH PEMILIK LAHAN",
                        "", "", "", 0, 0, 0, 0, "", "", Date(), Date(), "", ""
                    )
                )
                listOwner.addAll(ownerList.map {
                    it.copy(nama = "${it.nama} - ${it.nama_pok}")
                })
            } else {
                showError(this, "Error", "Belum ada pemilik lahan terdaftar di wilayah anda untuk komoditas $komoditas, silahkan tambahkan data pemilik lahan terlebih dahulu") {
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.e("LoadOwner", "Gagal load owner", e)
            showError(this, "Error", "${e.message}") { finish() }
        } finally {
            Loading.hide()
            ownerAdapter.notifyDataSetChanged()
            Log.i("LoadOwner", listOwner.toString())
        }
    }

    private suspend fun loadLahan(owner: OwnerEntity){
        Loading.show(this)
        try {
            val lahanList = dbLahan.getVerifiedLahanByOwner(komoditas, owner.id)
            listLahan.clear()
            if(lahanList.isNotEmpty()){
                listLahan.add(NewLahanEntity(0, TypeLahan.MONOKULTUR, komoditas, "PILIH LAHAN", 0, "", "", "", 0, "", 0, "", 0, "", 0, "", "", "", "", "", null, Date(), Date(), "", ""))
                lahanList.forEachIndexed { index, it ->
                    listLahan.add(
                        NewLahanEntity(
                            it.id,
                            it.type,
                            it.komoditas,
                            "LAHAN KE - ${it.lahanke} (${it.type.name})",
                            it.owner_id,
                            owner.nama,
                            owner.nama_pok,
                            owner.type.name,
                            it.provinsi_id,
                            "",
                            it.kabupaten_id,
                            "",
                            it.kecamatan_id,
                            "",
                            it.desa_id,
                            "",
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
            }else{
                showError(this, "Error", "Belum ada lahan terdaftar untuk pemilik lahan ini, silahkan tambahkan lahan terlebih dahulu!") {
                    finish()
                }
            }
        }catch (e: Exception){
            Log.e("LoadLahan", "Gagal load lahan", e)
            showError(this, "Error", "${e.message}") { finish() }
        }finally {
            lahanAdapter.notifyDataSetChanged()
            Loading.hide()
        }
    }

    private suspend fun loadMasaTanam(lahan: NewLahanEntity){
        Loading.show(this)
        try {
            listMasaTanam.clear()
            val tanaman = dbTanaman.getTanamanByLahanId(komoditas, lahan.id)
            if (tanaman.isNullOrEmpty()){
                etMasaTanam.visibility = View.VISIBLE
                lyMasaTanam.visibility = View.GONE
                etMasaTanam.setText("1")
                etMasaTanam.isEnabled = false
            }else{
                spMasaTanam.visibility = View.VISIBLE
                etMasaTanam.visibility = View.GONE
                listMasaTanam.add(MasaTanam(0, "0", "PILIH MASA TANAM"))
                listMasaTanam.addAll(generateMasaTanamList(tanaman))
                listMasaTanam.add(MasaTanam(tanaman.size+1, "", "MASA TANAM BARU"))
            }
        }catch (e: Exception){
            showError(this, "Error", "${e.message}"){
                finish()
            }
            Log.e("LoadMasaTanam", "Gagal load masa tanam", e)
        }finally {
            Loading.hide()
            masaTanamAdapter.notifyDataSetChanged()
        }
    }

    private fun isValid(): Boolean {
        if (spOwner.selectedItemPosition == 0) {
            (spOwner.selectedView as? TextView)?.error = "Pemilik Lahan harus dipilih"
            return false
        }

        if (spTypeLahan.selectedItemPosition == 0) {
            (spTypeLahan.selectedView as? TextView)?.error = "Lahan harus dipilih"
            return false
        }

        if (spSumber.selectedItemPosition == 0) {
            (spSumber.selectedView as? TextView)?.error = "Sumber harus dipilih"
            return false
        }

        if (spMasaTanam.isVisible) {
            val lastPosition = spMasaTanam.adapter.count - 1
            when (spMasaTanam.selectedItemPosition) {
                0 -> {
                    (spMasaTanam.selectedView as? TextView)?.error = "Masa Tanam harus dipilih"
                    return false
                }
                lastPosition -> {
                    if (etMasaTanam.text.isNullOrEmpty()) {
                        etMasaTanam.error = "Masa Tanam harus diisi"
                        return false
                    }
                }
            }
        }

        if (etLuas.text.isNullOrEmpty()){
            etLuas.error = "Luas harus diisi"
            return false
        }

        if(etTanggalTanam.text.isNullOrEmpty()){
            etTanggalTanam.error = "Tanggal Tanam harus diisi"
            return false
        }

        if(etPrediksiPanen.text.isNullOrEmpty()){
            etPrediksiPanen.error = "Prediksi Panen harus diisi"
            return false
        }

        if(etPerkiraanTanggalPanen.text.isNullOrEmpty()){
            etPerkiraanTanggalPanen.error = "Perkiraan Tanggal Panen harus diisi"
            return false
        }

        if(etVarietas.text.isNullOrEmpty()){
            etVarietas.error = "Varietas harus diisi"
            return false
        }

        if(etKeteranganSumber.text.isNullOrEmpty()){
            etKeteranganSumber.error = "Keterangan Sumber harus diisi"
            return false
        }

        dokumentasiList.forEach {
            if (it.imageView.visibility != View.VISIBLE) {
                it.errorText.visibility = View.VISIBLE
                return false
            }
        }

        return true
    }

    private suspend fun prepareData(){
        Loading.show(this)
        try {
            val lastId = db.draftMediaDao().getLastId() ?: 0
            val result = MediaDraftHelper.saveDokumentasiToDraft(
                context = this@AddTanaman,
                dokumentasiList = dokumentasiList,
                currentId = lastId + 1,
                nrp = getMyNrp(this@AddTanaman),
                dao = db.draftMediaDao()
            )

            if(result.size == 4){
                val lastDraftId = db.draftTanamanDao().getLastId() ?: 0
                val lahanId = listLahan[spTypeLahan.selectedItemPosition].id

                val masaTanam = if (spMasaTanam.isVisible) {
                    val lastPosition = spMasaTanam.adapter.count - 1
                    when (spMasaTanam.selectedItemPosition) {
                        lastPosition -> {
                            etMasaTanam.text.toString()
                        }
                        else -> listMasaTanam[spMasaTanam.selectedItemPosition].masatanam
                    }
                }else{
                    etMasaTanam.text.toString()
                }
                val luastanam = etLuas.text.toString()
                val tanggalTanam = etTanggalTanam.text.toString().convertToServerFormat()?: Date()
                val prediksiPanen = etPrediksiPanen.text.toString()
                val rencanatanggalpanen = etPerkiraanTanggalPanen.text.toString().convertToServerFormat()?: Date()
                val komoditas = komoditas
                val varietas = etVarietas.text.toString()
                val sumber = SumberBibit.valueOf(spSumber.selectedItem.toString())
                val keteranganSumber = etKeteranganSumber.text.toString()
                val foto1 = result[0].url
                val foto2 = result[1].url
                val foto3 = result[2].url
                val foto4 = result[3].url
                val status = "OFFLINECREATE"
                val alasan = null
                val submitter = getMyNrp(this@AddTanaman)
                val tanamanByLahan = db.tanamanDao().getVerifiedTanamanByLahanId(lahanId, masaTanam)
                val draftTanamanByLahan = db.draftTanamanDao().getDraftTanamanByLahanId(lahanId, masaTanam)
                val tanamanke = tanamanByLahan.size + draftTanamanByLahan.size + 1
                val data = TanamanDraftEntity(
                    lastDraftId + 1,
                    null,
                    lahanId,
                    masaTanam,
                    luastanam,
                    tanggalTanam,
                    prediksiPanen,
                    rencanatanggalpanen,
                    komoditas,
                    varietas,
                    sumber,
                    keteranganSumber,
                    foto1,
                    foto2,
                    foto3,
                    foto4,
                    status,
                    alasan,
                    Date(),
                    Date(),
                    submitter,
                    tanamanke.toString()
                )

                if(db.draftTanamanDao().insertTanaman(data) > 0){
                    if(isOnline(this)){
                        tryUploadImage(result, data)
                    }else{
                        showSuccess(this, "Berhasil", "Data berhasil disimpan di draft"){
                            finish()
                        }
                    }
                }else{
                    showError(this@AddTanaman, "Error", "Gagal menyimpan data!") {
                        finish()
                    }
                }
            }else{
                showError(this@AddTanaman, "Error", "Gagal menyimpan data gambar!") {
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this@AddTanaman, "Error", "${e.message}") {
                finish()
            }
        }
    }

    private suspend fun tryUploadImage(media: List<MediaDraftEntity>, data: TanamanDraftEntity) {
        try {
            val api = Client.retrofit.create(MediaEndpoint::class.java)

            val fileParts = media.mapIndexed { index, draft ->
                val file = File(draft.url) // asumsi draft.url adalah path lokal file

                val progressBody = ProgressRequestBody(
                    file = file,
                    contentType = "image/*"
                ) { progress, uploadedMb, totalMb ->
                    runOnUiThread {
                        UploadProgress.show(this@AddTanaman, progress, uploadedMb, totalMb)
                    }
                }

                MultipartBody.Part.createFormData("files", file.name, progressBody)
            }

            val nrpBody = data.submitter.toRequestBody("text/plain".toMediaTypeOrNull())

            val result = api.uploadMedia(fileParts, nrpBody)

            if(result.isSuccessful && result.body() != null && result.body()?.size == 4){
                val newData = data.copy(
                    foto1 = result.body()!![0].url,
                    foto2 = result.body()!![1].url,
                    foto3 = result.body()!![2].url,
                    foto4 = result.body()!![3].url
                )

                db.draftMediaDao().delete(media)
                result.body()!!.forEach {
                    db.mediaDao().insert(
                        MediaEntity(
                            it.id,
                            it.nrp,
                            it.filename,
                            it.url,
                            it.type,
                            parseIsoDate(it.createdAt) ?: Date(),
                        )
                    )
                }

                if(db.draftTanamanDao().insertTanaman(newData) > 0){
                    if(isOnline(this)){
                        tryUploadData(newData)
                    }else{
                        showError(this, "Error", "Gambar berhasil diupload, namun jaringan internet hilang saat akan menyimpan data realisasi tanam!"){
                            finish()
                        }
                    }
                }else{
                    showError(this@AddTanaman, "Error", "Gagal menyimpan data!") {
                        finish()
                    }
                }
            }

        } catch (e: Exception) {
            showError(this@AddTanaman, "Upload Error", e.message ?: "Unknown error") {
                finish()
            }
        } finally {
            runOnUiThread {
                UploadProgress.hide()
            }
        }
    }

    private suspend fun tryUploadData(data: TanamanDraftEntity) {
        Loading.show(this)
        try {
            val result = Client.retrofit.create(TanamanEndpoint::class.java).addTanaman(
                InsertDataTanam(
                    data.lahan_id.toString(),
                    data.masatanam,
                    data.luastanam,
                    data.tanggaltanam.toIsoString(),
                    data.prediksipanen,
                    data.rencanatanggalpanen.toIsoString(),
                    data.komoditas,
                    data.varietas,
                    data.sumber.name,
                    data.keteranganSumber,
                    data.foto1,
                    data.foto2,
                    data.foto3,
                    data.foto4,
                    "UNVERIFIED",
                    null,
                    data.submitter,
                    getMyRole(this),
                    getMyKabId(this),
                    data.tanamanke
                )
            )

            if(result.isSuccessful && result.body() != null){
                val newData = result.body()!!.data!!
                val newInsert: TanamanEntity = newData.let {
                    TanamanEntity(
                        it.id!!,
                        it.lahanId!!,
                        it.masatanam!!,
                        it.luastanam!!,
                        parseIsoDate(it.tanggaltanam!!)?: Date(),
                        it.prediksipanen!!,
                        parseIsoDate(it.rencanatanggalpanen!!)?: Date(),
                        it.komoditas!!,
                        it.varietas!!,
                        SumberBibit.valueOf(it.sumber!!),
                        it.keteranganSumber!!,
                        it.foto1!!,
                        it.foto2!!,
                        it.foto3!!,
                        it.foto4!!,
                        it.status!!,
                        it.alasan,
                        parseIsoDate(it.createAt!!) ?: Date(),
                        parseIsoDate(it.updateAt!!) ?: Date(),
                        it.submitter!!,
                        it.tanamanke!!
                    )
                }
                dbTanaman.insertSingle(newInsert)
                db.draftTanamanDao().delete(data)
                showSuccess(this, "Berhasil", "Data berhasil disimpan!"){
                    finish()
                }
            }else{
                showError(this@AddTanaman, "Error", "Gagal menyimpan data!") {
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this@AddTanaman, "Error", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }
}