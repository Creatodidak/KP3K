package id.creatodidak.kp3k.newversion.DataPanen

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.PanenDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.api.RequestClass.InsertDataPanen
import id.creatodidak.kp3k.api.RequestClass.ProgressRequestBody
import id.creatodidak.kp3k.api.RequestClass.UpdateDataPanen
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import id.creatodidak.kp3k.database.Entity.MediaEntity
import id.creatodidak.kp3k.database.Entity.PanenDraftEntity
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.helper.LoadAI
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.UploadProgress
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Date
import java.util.Locale

class DraftPanen : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvDraft : RecyclerView
    private lateinit var adapter : PanenDataDraftVerifikasiAdapter
    private var datas = mutableListOf<ShowDataPanenByCategory.NewPanenEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_draft_panen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.error_bg)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvDraft = findViewById(R.id.rvDraft)

        adapter = PanenDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data -> },
            onDeleteClick = {data -> 
                lifecycleScope.launch { 
                    deleteData(data)
                }
            },
            onKirimDataKeServerUpdateClick = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch {
                        tryUploadImage(data)
                    }
                }else{
                    showError(this@DraftPanen, "Error", "Jaringan internet tidak tersedia!"){
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            },
            onKirimDataKeServerCreateClick = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch {
                        tryUploadImage(data)
                    }
                }else{
                    showError(this@DraftPanen, "Error", "Jaringan internet tidak tersedia!"){
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            },
            onEdit = {data -> },
            onDeleteOnServer = {data -> }
        )

        rvDraft.adapter = adapter
        rvDraft.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        datas.clear()
        adapter.notifyDataSetChanged()
        totalData.text = "Memuat data..."
        val tanamans = db.tanamanDao().getAll(komoditas).associateBy { it.id }
        val lahans = db.lahanDao().getAll(komoditas).associateBy { it.id }
        val owners = db.ownerDao().getAllByKomoditas(komoditas).associateBy { it.id }
        val panenOffline = db.draftPanenDao().getAll(komoditas)
        panenOffline.forEach {
            val ids = if(it.status == "OFFLINEUPDATE") it.currentId!! else it.id
            val tanaman = tanamans[it.tanaman_id]!!
            val lahan = lahans[tanaman.lahan_id]!!
            val owner = owners[lahan.owner_id]!!
            val data = ShowDataPanenByCategory.NewPanenEntity(
                ids,
                "DATA PANEN BARU",
                tanaman,
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
            datas.add(data)
        }
        adapter.notifyDataSetChanged()
        totalData.text = "Total Data : ${datas.size}"
    }

    private suspend fun deleteData(data: ShowDataPanenByCategory.NewPanenEntity) {
        try {
            if(db.draftPanenDao().deleteDraftById(data.id) > 0){
                showSuccess(this, "Sukses", "Berhasil menghapus data!"){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }else{
                showSuccess(this, "Gagal", "Gagal menghapus data!"){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showSuccess(this, "Gagal", "Gagal menghapus data!\n${e.message}"){
                lifecycleScope.launch {
                    loadData()
                }
            }
        }
    }

    private suspend fun tryUploadImage(data: ShowDataPanenByCategory.NewPanenEntity) {
        try {
            val api = Client.retrofit.create(MediaEndpoint::class.java)
            val media = listOf(data.foto1, data.foto2, data.foto3, data.foto4)

            val fileParts = media.mapIndexed { index, draft ->
                val file = File(draft) // asumsi draft.url adalah path lokal file

                val progressBody = ProgressRequestBody(
                    file = file,
                    contentType = "image/*"
                ) { progress, uploadedMb, totalMb ->
                    runOnUiThread {
                        UploadProgress.show(this@DraftPanen, progress, uploadedMb, totalMb)
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

                db.draftMediaDao().deleteByUrls(media)
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

                if(db.draftPanenDao().updateFoto(data.id, newData.foto1, newData.foto2, newData.foto3, newData.foto4) > 0){
                    if(isOnline(this)){
                        if(data.analisa.isNullOrEmpty()){
                            getAnalisa(newData)
                        }else{
                            tryUploadData(newData)
                        }
                    }else{
                        showError(this, "Error", "Gambar berhasil diupload, namun jaringan internet hilang saat akan menyimpan data realisasi panen!"){
                            lifecycleScope.launch {
                                loadData()
                            }
                        }
                    }
                }else{
                    showError(this@DraftPanen, "Error", "Gagal menyimpan data!") {
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            showError(this@DraftPanen, "Upload Error", e.message ?: "Unknown error") {
                lifecycleScope.launch {
                    loadData()
                }
            }
        } finally {
            runOnUiThread {
                UploadProgress.hide()
            }
        }
    }

    private fun getAnalisa(data: ShowDataPanenByCategory.NewPanenEntity){
        try {
            val req = "Analisa singkat panen ${komoditas} varietas ${data.tanaman?.varietas}. Tanam: ${formatTanggalKeIndonesia(data.tanaman?.tanggaltanam!!.toIsoString())}, ${angkaIndonesia(convertToHektar(
                data.tanaman.luastanam.toDouble()))}Ha. Target: ${angkaIndonesia(convertToTon(
                data.tanaman.prediksipanen.toDouble()))}t. Panen: ${formatTanggalKeIndonesia(data.tanggalpanen.toIsoString())}, ${angkaIndonesia(convertToHektar(data.luaspanen.toDouble()))}Ha, ${angkaIndonesia(convertToTon(data.jumlahpanen.toDouble()))}t."


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
            showError(this@DraftPanen, "Error", "${e.message}") {
                lifecycleScope.launch {
                    loadData()
                }
            }
        }finally {
            Loading.hide()
        }
    }

    private suspend fun tryUploadData(data: ShowDataPanenByCategory.NewPanenEntity) {
        Loading.show(this)
        try {
            val result = if (data.status == "OFFLINECREATE") {
                withContext(Dispatchers.IO){
                    Client.retrofit.create(PanenEndpoint::class.java).addPanen(
                        InsertDataPanen(
                            data.tanaman_id.toString(),
                            data.jumlahpanen,
                            data.luaspanen,
                            data.tanggalpanen.toIsoString(),
                            data.keterangan.toString(),
                            data.analisa?.replace("...", ""),
                            data.foto1,
                            data.foto2,
                            data.foto3,
                            data.foto4,
                            "UNVERIFIED",
                            null,
                            komoditas,
                            getMyNrp(this@DraftPanen),
                            getMyKabId(this@DraftPanen),
                            getMyRole(this@DraftPanen),
                        )
                    )
                }
            }else{
                withContext(Dispatchers.IO){
                    Client.retrofit.create(PanenEndpoint::class.java).updatePanen(
                        data.id,
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
                            getMyNrp(this@DraftPanen),
                            getMyKabId(this@DraftPanen),
                            getMyRole(this@DraftPanen),
                        )
                    )
                }
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
                if(data.status == "OFFLINECREATE"){
                    db.draftPanenDao().deleteDraftById(data.id)
                }else{
                    db.draftPanenDao().deleteDraftByCurrentId(data.id)
                }
                showSuccess(this, "Berhasil", "Data berhasil disimpan") {
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }else{
                showError(this@DraftPanen, "Error", "Gagal menyimpan data!") {
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this@DraftPanen, "Error Exception", "${e.message}") {
                lifecycleScope.launch {
                    loadData()
                }
            }
        }finally {
            Loading.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadData()
        }
    }
}