package id.creatodidak.kp3k.newversion.DataPanen

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import id.creatodidak.kp3k.BuildConfig.AI_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.api.RequestClass.InsertDataPanen
import id.creatodidak.kp3k.api.RequestClass.InsertDataTanam
import id.creatodidak.kp3k.api.RequestClass.ProgressRequestBody
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.api.model.ChatMessage
import id.creatodidak.kp3k.api.newModel.DokumentasiUI
import id.creatodidak.kp3k.api.newModel.MasaTanam
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import id.creatodidak.kp3k.database.Entity.MediaEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PanenDraftEntity
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.DatePickerMode
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.LoadAI
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.MediaDraftHelper
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.SumberFoto
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.UploadProgress
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToServerFormat
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.generateMasaTanamList
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory.NewLahanEntity
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddPanen : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView

    private lateinit var spOwner: Spinner
    private lateinit var spTypeLahan: Spinner
    private lateinit var spTanaman: Spinner
    private lateinit var etLuasPanen: TextInputEditText
    private lateinit var tvConvertHektar: TextView
    private lateinit var etTanggalPanen: TextInputEditText
    private lateinit var etJumlahPanen: TextInputEditText
    private lateinit var tvConvertTon: TextView
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnKirimData: Button
    private lateinit var lyForm: LinearLayout

    private val dokumentasiList = mutableListOf<DokumentasiUI>()
    private val galeriLaunchers = mutableListOf<ActivityResultLauncher<String>>()
    private val kameraLaunchers = mutableListOf<ActivityResultLauncher<Intent>>()
    private val listOwner = mutableListOf<OwnerEntity>()
    private val listLahan = mutableListOf<NewLahanEntity>()
    private val listTanaman = mutableListOf<ShowDataTanamanByCategory.NewTanamanEntity>()

    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>
    private lateinit var lahanAdapter: ArrayAdapter<NewLahanEntity>
    private lateinit var tanamanAdapter: ArrayAdapter<ShowDataTanamanByCategory.NewTanamanEntity>

    private var selectedTanaman: ShowDataTanamanByCategory.NewTanamanEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_panen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)

        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()}"

        spOwner = findViewById(R.id.spOwner)
        spTypeLahan = findViewById(R.id.spTypeLahan)
        spTanaman = findViewById(R.id.spTanaman)
        etLuasPanen = findViewById(R.id.etLuasPanen)
        tvConvertHektar = findViewById(R.id.tvConvertHektar)
        etTanggalPanen = findViewById(R.id.etTanggalPanen)
        etJumlahPanen = findViewById(R.id.etJumlahPanen)
        tvConvertTon = findViewById(R.id.tvConvertTon)
        etKeterangan = findViewById(R.id.etKeterangan)
        btnKirimData = findViewById(R.id.btnKirimData)
        lyForm = findViewById(R.id.lyForm)
        lyForm.visibility = View.GONE

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

        ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOwner)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spOwner.adapter = ownerAdapter

        lahanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listLahan)
        lahanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTypeLahan.adapter = lahanAdapter

        tanamanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listTanaman)
        tanamanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTanaman.adapter = tanamanAdapter

        etTanggalPanen.inputType = InputType.TYPE_NULL
        etTanggalPanen.isFocusable = false

        etLuasPanen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val luashektar = angkaIndonesia(convertToHektar(etLuasPanen.text.toString().toDoubleOrNull() ?: 0.0))
                val luas = if(selectedTanaman != null)" dari Luas Tanam ${angkaIndonesia(convertToHektar(selectedTanaman!!.luastanam.toDoubleOrNull() ?: 0.0))}Ha" else ""
                tvConvertHektar.text = "= $luashektar Ha (Pembulatan Desimal 2 Angka Dibelakang Koma)$luas"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etJumlahPanen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val prediksiTon = angkaIndonesia(convertToTon(etJumlahPanen.text.toString().toDoubleOrNull() ?: 0.0))
                val target = if(selectedTanaman != null)" dari Target ${angkaIndonesia(convertToTon(selectedTanaman!!.prediksipanen.toDoubleOrNull() ?: 0.0))}Ton" else ""
                tvConvertTon.text = "= $prediksiTon Ton (Pembulatan Desimal 2 Angka Dibelakang Koma)$target"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etTanggalPanen.setOnClickListener {
            showCustomDatePicker(this, DatePickerMode.DAY){ d ->
                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                etTanggalPanen.setText(formattedDate)
            }
        }

        spOwner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lyForm.visibility = View.GONE
                if(position > 0){
                    val selectedOwner = listOwner[position]
                    spTypeLahan.setSelection(0)
                    spTanaman.setSelection(0)
                    selectedTanaman = null
                    lifecycleScope.launch {
                        loadLahan(selectedOwner)
                    }
                }else{
                    spTypeLahan.setSelection(0)
                    spTanaman.setSelection(0)
                    listLahan.clear()
                    lahanAdapter.notifyDataSetChanged()
                    listTanaman.clear()
                    tanamanAdapter.notifyDataSetChanged()
                    selectedTanaman = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                listLahan.clear()
                lahanAdapter.notifyDataSetChanged()
                listTanaman.clear()
                tanamanAdapter.notifyDataSetChanged()
                lyForm.visibility = View.GONE
            }
        }

        spTypeLahan.onItemSelectedListener  = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lyForm.visibility = View.GONE
                if(position > 0){
                    val selectedLahan = listLahan[position]
                    spTanaman.setSelection(0)
                    selectedTanaman = null
                    lifecycleScope.launch {
                        loadTanaman(selectedLahan)
                    }
                }else{
                    listTanaman.clear()
                    tanamanAdapter.notifyDataSetChanged()
                    spTanaman.setSelection(0)
                    selectedTanaman = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                listLahan.clear()
                lahanAdapter.notifyDataSetChanged()
                listTanaman.clear()
                tanamanAdapter.notifyDataSetChanged()
                lyForm.visibility = View.GONE
            }
        }

        spTanaman.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position > 0){
                    selectedTanaman = listTanaman[position]
                    lyForm.visibility = View.VISIBLE
                }else{
                    selectedTanaman = null
                    lyForm.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                lyForm.visibility = View.GONE
                listTanaman.clear()
                tanamanAdapter.notifyDataSetChanged()
            }

        }

        lifecycleScope.launch {
            loadOwner()
        }

        btnKirimData.setOnClickListener {
            if(isValid()){
                lifecycleScope.launch {
                    prepareData()
                }
            }
        }
    }

    private suspend fun prepareData(){
        Loading.show(this)
        try {
            val lastId = db.draftMediaDao().getLastId() ?: 0
            val result = MediaDraftHelper.saveDokumentasiToDraft(
                context = this@AddPanen,
                dokumentasiList = dokumentasiList,
                currentId = lastId + 1,
                nrp = getMyNrp(this@AddPanen),
                dao = db.draftMediaDao()
            )

            if(result.size == 4){
                val lastDraftId = db.draftPanenDao().getLastId() ?: 0
                val tanamanId = listTanaman[spTanaman.selectedItemPosition].id
                val luasPanen = etLuasPanen.text.toString()
                val tanggalPanen = etTanggalPanen.text.toString().convertToServerFormat()?: Date()
                val jumlahPanen = etJumlahPanen.text.toString()
                val keterangan = etKeterangan.text.toString()
                val foto1 = result[0].url
                val foto2 = result[1].url
                val foto3 = result[2].url
                val foto4 = result[3].url
                val status = "OFFLINECREATE"
                val alasan = null
                val submitter = getMyNrp(this@AddPanen)
                val data = PanenDraftEntity(
                    lastDraftId + 1,
                    null,
                    tanamanId,
                    jumlahPanen,
                    luasPanen,
                    tanggalPanen,
                    keterangan,
                    null,
                    foto1,
                    foto2,
                    foto3,
                    foto4,
                    status,
                    alasan,
                    Date(),
                    Date(),
                    komoditas,
                    submitter
                )

                if(db.draftPanenDao().insertSingle(data) > 0){
                    if(isOnline(this)){
                        tryUploadImage(result, data)
                    }else{
                        showSuccess(this, "Berhasil", "Data berhasil disimpan di draft"){
                            finish()
                        }
                    }
                }else{
                    showError(this@AddPanen, "Error", "Gagal menyimpan data!") {
                        finish()
                    }
                }
            }else{
                showError(this@AddPanen, "Error", "Gagal menyimpan data gambar!") {
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this@AddPanen, "Error", "${e.message}") {
                finish()
            }
        }
    }

    private suspend fun tryUploadImage(media: List<MediaDraftEntity>, data: PanenDraftEntity) {
        try {
            val api = Client.retrofit.create(MediaEndpoint::class.java)

            val fileParts = media.mapIndexed { index, draft ->
                val file = File(draft.url) // asumsi draft.url adalah path lokal file

                val progressBody = ProgressRequestBody(
                    file = file,
                    contentType = "image/*"
                ) { progress, uploadedMb, totalMb ->
                    runOnUiThread {
                        UploadProgress.show(this@AddPanen, progress, uploadedMb, totalMb)
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

                if(db.draftPanenDao().insertSingle(newData) > 0){
                    if(isOnline(this)){
                        getAnalisa(newData)
                    }else{
                        showError(this, "Error", "Gambar berhasil diupload, namun jaringan internet hilang saat akan menyimpan data realisasi panen!"){
                            finish()
                        }
                    }
                }else{
                    showError(this@AddPanen, "Error", "Gagal menyimpan data!") {
                        finish()
                    }
                }
            }

        } catch (e: Exception) {
            showError(this@AddPanen, "Upload Error", e.message ?: "Unknown error") {
                finish()
            }
        } finally {
            runOnUiThread {
                UploadProgress.hide()
            }
        }
    }

    private fun getAnalisa(data: PanenDraftEntity){
        try {
            val req = "Analisa singkat panen ${komoditas} varietas ${selectedTanaman!!.varietas}. Tanam: ${formatTanggalKeIndonesia(selectedTanaman!!.tanggaltanam.toIsoString())}, ${angkaIndonesia(convertToHektar(selectedTanaman!!.luastanam.toDouble()))}Ha. Target: ${angkaIndonesia(convertToTon(selectedTanaman!!.prediksipanen.toDouble()))}t. Panen: ${formatTanggalKeIndonesia(data.tanggalpanen.toIsoString())}, ${angkaIndonesia(convertToHektar(data.luaspanen.toDouble()))}Ha, ${angkaIndonesia(convertToTon(data.jumlahpanen.toDouble()))}t."


            LoadAI.show(this, req) { analisa, status ->
                if (status){
                    val newData = data.copy(
                        analisa = analisa
                    )
                    lifecycleScope.launch { tryUploadData(newData) }
                }else{
                    lifecycleScope.launch { tryUploadData(data) }
                }
            }
        }catch (e: Exception){
            showError(this@AddPanen, "Error", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }

    private suspend fun tryUploadData(data: PanenDraftEntity) {
        Loading.show(this)
        try {
            val result = Client.retrofit.create(PanenEndpoint::class.java).addPanen(
                InsertDataPanen(
                    data.tanaman_id.toString(),
                    data.jumlahpanen,
                    data.luaspanen,
                    data.tanggalpanen.toIsoString(),
                    data.keterangan,
                    data.analisa?.replace("...", ""),
                    data.foto1,
                    data.foto2,
                    data.foto3,
                    data.foto4,
                    "UNVERIFIED",
                    null,
                    komoditas,
                    getMyNrp(this),
                    getMyKabId(this),
                    getMyRole(this),
                )
            )

            if(result.isSuccessful && result.body() != null){
                val newData = result.body()!!.data!!
                val newInsert: PanenEntity = newData.let {
                    PanenEntity(
                        it.id!!,
                        it.tanamanId!!,
                        it.jumlahpanen!!,
                        it.luaspanen!!,
                        parseIsoDate(it.tanggalpanen!!)?: Date(),
                        it.keterangan!!,
                        it.alasan,
                        it.foto1!!,
                        it.foto2!!,
                        it.foto3!!,
                        it.foto4!!,
                        it.status!!,
                        it.alasan,
                        parseIsoDate(it.createAt!!) ?: Date(),
                        parseIsoDate(it.updateAt!!) ?: Date(),
                        it.komoditas!!,
                        it.submitter!!,
                    )
                }
                
                    db.panenDao().insertSingle(newInsert)
                    db.draftPanenDao().delete(data)
                    showSuccess(this, "Berhasil", "Data berhasil disimpan") {
                        finish()
                    }
            }else{
                showError(this@AddPanen, "Error", "Gagal menyimpan data!") {
                    finish()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this@AddPanen, "Error Exception", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }

    private suspend fun loadOwner() {
        Loading.show(this)
        try {
            val roleHelper = RoleHelper(this)
            val level = getMyLevel(this)
            val ownerList: List<OwnerEntity> = when (level) {
                "provinsi" -> db.ownerDao().getOwnerByProvinsi(komoditas, roleHelper.id)
                "kabupaten" -> db.ownerDao().getOwnerByKabupaten(komoditas, roleHelper.id)
                "kecamatan" -> db.ownerDao().getOwnerByKecamatans(komoditas, roleHelper.ids)
                "desa" -> db.ownerDao().getOwnerByDesa(komoditas, roleHelper.id)
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
            val lahanList = db.lahanDao().getVerifiedLahanByOwner(komoditas, owner.id)
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

    private suspend fun loadTanaman(lahan: NewLahanEntity){
        Loading.show(this)
        try {
            listTanaman.clear()
            val tanaman = db.tanamanDao().getTanamanByLahanId(komoditas, lahan.id)
            if (tanaman.isNullOrEmpty()){
                showError(this, "Error", "Belum ada tanaman terdaftar untuk lahan ini, silahkan tambahkan tanaman terlebih dahulu!"){
                    finish()
                }
            }else{
                listTanaman.add(ShowDataTanamanByCategory.NewTanamanEntity(0, lahan.id, "PILIH TANAMAN", "", TypeLahan.MONOKULTUR, "", "", "", "", Date(), "", Date(), "", "",
                    SumberBibit.PEMERINTAH, "", "", "", "", "", "", "", Date(), Date(), "", ""))
                tanaman.forEach {
                    listTanaman.add(
                        ShowDataTanamanByCategory.NewTanamanEntity(
                            it.id,
                            it.lahan_id,
                            "TANAMAN KE - ${it.tanamanke} MT KE ${it.masatanam}",
                            "",
                            TypeLahan.MONOKULTUR,
                            "",
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
        }catch (e: Exception){
            showError(this, "Error", "${e.message}"){
                finish()
            }
            Log.e("LoadMasaTanam", "Gagal load masa tanam", e)
        }finally {
            Loading.hide()
            tanamanAdapter.notifyDataSetChanged()
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

        if (spTanaman.selectedItemPosition == 0) {
            (spTanaman.selectedView as? TextView)?.error = "Tanaman harus dipilih"
            return false
        }

        if (etLuasPanen.text.isNullOrEmpty()){
            etLuasPanen.error = "Luas Panen harus diisi"
            return false
        }

        if(etTanggalPanen.text.isNullOrEmpty()){
            etTanggalPanen.error = "Tanggal Panen harus diisi"
            return false
        }

        if(etJumlahPanen.text.isNullOrEmpty()){
            etJumlahPanen.error = "Jumlah Panen harus diisi"
            return false
        }

        if(etKeterangan.text.isNullOrEmpty()){
            etKeterangan.error = "Keterangan harus diisi"
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

}