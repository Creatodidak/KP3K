package id.creatodidak.kp3k.newversion.DataTanaman

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.RequestClass.UpdateDataTanam
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.DatePickerMode
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToServerFormat
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showCustomDatePicker
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTanaman : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var id: String
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var spSumber: Spinner
    private lateinit var etLuas: TextInputEditText
    private lateinit var etTanggalTanam: TextInputEditText
    private lateinit var etPrediksiPanen: TextInputEditText
    private lateinit var etPerkiraanTanggalPanen: TextInputEditText
    private lateinit var etVarietas: TextInputEditText
    private lateinit var etKeteranganSumber: TextInputEditText
    private lateinit var tvConvertHektar: TextView
    private lateinit var tvConvertTon: TextView
    private lateinit var btnKirimData: Button
    private val listSumber = listOf<String>("PILIH SUMBER BIBIT", "MANDIRI", "POLRI", "PEMERINTAH")
    private lateinit var sumberAdapter: ArrayAdapter<String>

    private var edited: TanamanEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_tanaman)
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
        spSumber = findViewById(R.id.spSumber)
        etLuas = findViewById(R.id.etLuas)
        etTanggalTanam = findViewById(R.id.etTanggalTanam)
        etPrediksiPanen = findViewById(R.id.etPrediksiPanen)
        etPerkiraanTanggalPanen = findViewById(R.id.etPerkiraanTanggalPanen)
        etVarietas = findViewById(R.id.etVarietas)
        etKeteranganSumber = findViewById(R.id.etKeteranganSumber)
        tvConvertHektar = findViewById(R.id.tvConvertHektar)
        tvConvertTon = findViewById(R.id.tvConvertTon)
        btnKirimData = findViewById(R.id.btnKirimData)

        sumberAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listSumber)
        sumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSumber.adapter = sumberAdapter

        etTanggalTanam.inputType = InputType.TYPE_NULL
        etTanggalTanam.isFocusable = false

        etPerkiraanTanggalPanen.inputType = InputType.TYPE_NULL
        etPerkiraanTanggalPanen.isFocusable = false
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

        lifecycleScope.launch {
            loadExistingData()
        }

        btnKirimData.setOnClickListener {
            if(isValid()){
                if(isOnline(this)){
                    lifecycleScope.launch {
                        prepareData(edited!!)
                    }
                }else{
                    showError(this, "Error", "Tidak ada koneksi internet")
                }
            }
        }
    }

    private suspend fun loadExistingData() {
        val data = db.tanamanDao().getById(id.toInt())
        if(data == null){
            showError(this, "Error", "Data tidak ditemukan"){
                finish()
            }
        }else{
            edited = data
            spSumber.setSelection(listSumber.indexOf(data.sumber.name))
            etLuas.setText(data.luastanam)
            etTanggalTanam.setText(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(data.tanggaltanam))
            etPrediksiPanen.setText(data.prediksipanen)
            etPerkiraanTanggalPanen.setText(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(data.rencanatanggalpanen))
            etVarietas.setText(data.varietas)
            etKeteranganSumber.setText(data.keteranganSumber)
            tvConvertHektar.text = "= ${angkaIndonesia(convertToHektar(data.luastanam.toDoubleOrNull() ?: 0.0))} Ha (Pembulatan Desimal 2 Angka Dibelakang Koma)"
            tvConvertTon.text = "= ${angkaIndonesia(convertToTon(data.prediksipanen.toDoubleOrNull() ?: 0.0))} Ton (Pembulatan Desimal 2 Angka Dibelakang Koma)"
        }
    }

    private suspend fun prepareData(edited: TanamanEntity){
        Loading.show(this)
        try {
            val lastDraftId = db.draftTanamanDao().getLastId() ?: 0
            val luastanam = etLuas.text.toString()
            val tanggalTanam = etTanggalTanam.text.toString().convertToServerFormat()?: Date()
            val prediksiPanen = etPrediksiPanen.text.toString()
            val rencanatanggalpanen = etPerkiraanTanggalPanen.text.toString().convertToServerFormat()?: Date()
            val varietas = etVarietas.text.toString()
            val sumber = SumberBibit.valueOf(spSumber.selectedItem.toString())
            val keteranganSumber = etKeteranganSumber.text.toString()
            val status = "OFFLINEUPDATE"
            val alasan = null
            val newData = TanamanDraftEntity(
                lastDraftId + 1,
                edited.id,
                edited.lahan_id,
                edited.masatanam,
                luastanam,
                tanggalTanam,
                prediksiPanen,
                rencanatanggalpanen,
                komoditas,
                varietas,
                sumber,
                keteranganSumber,
                edited.foto1,
                edited.foto2,
                edited.foto3,
                edited.foto4,
                status,
                alasan,
                Date(),
                Date(),
                getMyNrp(this),
                edited.tanamanke
            )
            if(db.draftTanamanDao().insertTanaman(newData) > 0){
                if(isOnline(this)){
                    saveDataToServer(newData)
                }else{
                    showSuccess(this, "Berhasil", "Data berhasil disimpan di draft"){
                        finish()
                    }
                }
            }else{
                showError(this, "Error", "Gagal menyimpan data!") {
                    finish()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", "Data gagal disimpan di server, namun berhasil disimpan di local database sebagai draft Offline!"){
                finish()
            }
        }
    }

    private suspend fun saveDataToServer(d: TanamanDraftEntity){
        try {
            val result = withContext(Dispatchers.IO) {
                Client.retrofit.create(TanamanEndpoint::class.java).updateTanaman(
                    d.currentId!!,
                    UpdateDataTanam(
                        null,
                        null,
                        d.luastanam,
                        d.tanggaltanam.toIsoString(),
                        d.prediksipanen,
                        d.rencanatanggalpanen.toIsoString(),
                        null,
                        d.varietas,
                        d.sumber.name,
                        d.keteranganSumber,
                        null,
                        null,
                        null,
                        null,
                        "UNVERIFIED",
                        d.alasan,
                        getMyNrp(this@EditTanaman),
                        getMyRole(this@EditTanaman),
                        getMyKabId(this@EditTanaman),
                        null
                    )
                )
            }

            if (result.isSuccessful){
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
                db.tanamanDao().insertSingle(newInsert)
                db.draftTanamanDao().delete(d)
                showSuccess(this, "Berhasil", "Data berhasil disimpan!"){
                    finish()
                }
            } else{
                val msg = result.body()?.error?: result.errorBody().toString()?:"Gagal menyimpan data!"
                showError(this, "Error", msg) {
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this, "Error", "${e.message}") {
                finish()
            }
        }finally {
            Loading.hide()
        }
    }

    private fun isValid(): Boolean {
        if (spSumber.selectedItemPosition == 0) {
            (spSumber.selectedView as? TextView)?.error = "Sumber harus dipilih"
            return false
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

        return true
    }

}