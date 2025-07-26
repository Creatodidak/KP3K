package id.creatodidak.kp3k.newversion.DataPanen

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.api.RequestClass.UpdateDataPanen
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.PanenDraftEntity
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.DatePickerMode
import id.creatodidak.kp3k.helper.LoadAI
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToServerFormat
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditPanen : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var sh : SharedPreferences
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var id: String

    private lateinit var etLuasPanen: TextInputEditText
    private lateinit var tvConvertHektar: TextView
    private lateinit var etTanggalPanen: TextInputEditText
    private lateinit var etJumlahPanen: TextInputEditText
    private lateinit var tvConvertTon: TextView
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnKirimData: Button

    private var edited: PanenEntity? = null
    private var selectedTanaman: TanamanEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_panen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        id = intent.getStringExtra("id").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()}"

        etLuasPanen = findViewById(R.id.etLuasPanen)
        tvConvertHektar = findViewById(R.id.tvConvertHektar)
        etTanggalPanen = findViewById(R.id.etTanggalPanen)
        etJumlahPanen = findViewById(R.id.etJumlahPanen)
        tvConvertTon = findViewById(R.id.tvConvertTon)
        etKeterangan = findViewById(R.id.etKeterangan)
        btnKirimData = findViewById(R.id.btnKirimData)
        btnKirimData.visibility = View.GONE

        etTanggalPanen.inputType = InputType.TYPE_NULL
        etTanggalPanen.isFocusable = false

        etTanggalPanen.setOnClickListener {
            showCustomDatePicker(this, DatePickerMode.DAY){ d ->
                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(d)
                etTanggalPanen.setText(formattedDate)
            }
        }
        lifecycleScope.launch {
            loadExistingData()
        }
    }

    private suspend fun loadExistingData(){
        val panen = db.panenDao().getPanenById(id.toInt())
        if(panen !== null){
            edited = panen
            etLuasPanen.setText(panen.luaspanen)
            etTanggalPanen.setText(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(panen.tanggalpanen))
            etJumlahPanen.setText(panen.jumlahpanen)
            etKeterangan.setText(panen.keterangan)
            selectedTanaman = db.tanamanDao().getById(panen.tanaman_id)
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

            btnKirimData.visibility = View.VISIBLE
            btnKirimData.setOnClickListener {
                lifecycleScope.launch {
                    if(isOnline(this@EditPanen)){
                        if(isChanged()){
                            if(isValid()){
                                prepareData()
                            }
                        }else{
                            showError(this@EditPanen, "Error", "Tidak ada perubahan data")
                        }
                    }else{
                        showError(this@EditPanen, "Error", "Tidak ada koneksi internet")
                    }
                }
            }
        }else{
            showError(this, "Error", "Data tidak ditemukan"){
                finish()
            }
        }
    }

    private suspend fun prepareData(){
        Loading.show(this)
        try {
            val luasPanen = etLuasPanen.text.toString()
            val tanggalPanen = etTanggalPanen.text.toString().convertToServerFormat()?: Date()
            val jumlahPanen = etJumlahPanen.text.toString()
            val keterangan = etKeterangan.text.toString()
            val status = "OFFLINEUPDATE"
            val lastId = db.draftPanenDao().getLastId() ?: 0
            val data = PanenDraftEntity(
                lastId + 1,
                edited!!.id,
                edited!!.tanaman_id,
                jumlahPanen,
                luasPanen,
                tanggalPanen,
                keterangan,
                edited!!.analisa.toString(),
                edited!!.foto1,
                edited!!.foto2,
                edited!!.foto3,
                edited!!.foto4,
                status,
                null,
                edited!!.createAt,
                Date(),
                edited!!.komoditas,
                getMyNrp(this)
            )

            if(db.draftPanenDao().insertSingle(data) > 0){
                getAnalisa(data)
            }else{
                showError(this, "Error", "Data gagal disiapkan!")
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString())
        }finally {
            Loading.hide()
        }
    }

    private fun getAnalisa(data: PanenDraftEntity){
        try {
            val req = "Analisa singkat panen ${komoditas} varietas ${selectedTanaman!!.varietas}. Tanam: ${formatTanggalKeIndonesia(selectedTanaman!!.tanggaltanam.toIsoString())}, ${angkaIndonesia(convertToHektar(selectedTanaman!!.luastanam.toDouble()))}Ha. Target: ${angkaIndonesia(convertToTon(selectedTanaman!!.prediksipanen.toDouble()))}t. Panen: ${formatTanggalKeIndonesia(data.tanggalpanen.toIsoString())}, ${angkaIndonesia(convertToHektar(data.luaspanen.toDouble()))}Ha, ${angkaIndonesia(convertToTon(data.jumlahpanen.toDouble()))}t. Keterangan: ${data.keterangan}"


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
            showError(this@EditPanen, "Error", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }

    private suspend fun tryUploadData(data: PanenDraftEntity) {
        Loading.show(this)
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(PanenEndpoint::class.java).updatePanen(
                    data.currentId!!,
                    UpdateDataPanen(
                        null,
                        data.jumlahpanen,
                        data.luaspanen,
                        data.tanggalpanen.toIsoString(),
                        data.keterangan.toString(),
                        data.analisa?.replace("...", ""),
                        null,
                        null,
                        null,
                        null,
                        "UNVERIFIED",
                        null,
                        komoditas,
                        null,
                        getMyNrp(this@EditPanen),
                        getMyKabId(this@EditPanen),
                        getMyRole(this@EditPanen),
                    )
                )
            }

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
                db.draftPanenDao().deleteDraftByCurrentId(data.currentId!!)
                showSuccess(this, "Berhasil", "Data berhasil disimpan") {
                    finish()
                }
            }else{
                showError(this@EditPanen, "Error", "Gagal menyimpan data!") {
                    finish()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this@EditPanen, "Error Exception", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }


    private fun isValid(): Boolean {
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

        return true
    }

    private fun isChanged(): Boolean {
        if (etLuasPanen.text.toString() != edited!!.luaspanen){
            return true
        }
        if (etTanggalPanen.text.toString() != SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(edited!!.tanggalpanen)){
            return true
        }
        if (etJumlahPanen.text.toString() != edited!!.jumlahpanen){
            return true
        }
        if (etKeterangan.text.toString() != edited!!.keterangan){
            return true
        }
        return false
    }

}