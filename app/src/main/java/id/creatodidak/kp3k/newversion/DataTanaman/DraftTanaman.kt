package id.creatodidak.kp3k.newversion.DataTanaman

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.TanamanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.RequestClass.InsertDataTanam
import id.creatodidak.kp3k.api.RequestClass.ProgressRequestBody
import id.creatodidak.kp3k.api.RequestClass.UpdateDataTanam
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import id.creatodidak.kp3k.database.Entity.MediaEntity
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.UploadProgress
import id.creatodidak.kp3k.helper.getMyKabId
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Date
import java.util.Locale

class DraftTanaman : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvDraft : RecyclerView
    private lateinit var adapter : TanamanDataDraftVerifikasiAdapter
    private var datas = mutableListOf<NewTanamanEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_draft_tanaman)
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
        adapter = TanamanDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data -> },
            onDeleteClick = {data ->
                lifecycleScope.launch { deleteData(data) }
            },
            onKirimDataKeServerUpdateClick = {data ->
                if(isOnline(this)){
                    val newdata = TanamanDraftEntity(
                        data.id,
                        data.id,
                        data.lahan_id,
                        data.masatanam,
                        data.luastanam,
                        data.tanggaltanam,
                        data.prediksipanen,
                        data.rencanatanggalpanen,
                        data.komoditas,
                        data.varietas,
                        data.sumber,
                        data.keteranganSumber,
                        data.foto1,
                        data.foto2,
                        data.foto3,
                        data.foto4,
                        data.status,
                        data.alasan,
                        data.createAt,
                        data.updateAt,
                        data.submitter,
                        data.tanamanke
                    )
                    lifecycleScope.launch { tryUploadImage("UPDATE", newdata) }
                }else{
                    showError(this@DraftTanaman, "Error", "Tidak ada koneksi internet")
                }
            },
            onKirimDataKeServerCreateClick = {data ->
                if(isOnline(this)) {
                    val newdata = TanamanDraftEntity(
                        data.id,
                        data.id,
                        data.lahan_id,
                        data.masatanam,
                        data.luastanam,
                        data.tanggaltanam,
                        data.prediksipanen,
                        data.rencanatanggalpanen,
                        data.komoditas,
                        data.varietas,
                        data.sumber,
                        data.keteranganSumber,
                        data.foto1,
                        data.foto2,
                        data.foto3,
                        data.foto4,
                        data.status,
                        data.alasan,
                        data.createAt,
                        data.updateAt,
                        data.submitter,
                        data.tanamanke
                    )
                    lifecycleScope.launch { tryUploadImage("CREATE", newdata) }
                }else{
                    showError(this@DraftTanaman, "Error", "Tidak ada koneksi internet")
                }
            },
            onEdit = {data -> },
            onDeleteOnServer = {data -> }
        )
        rvDraft.adapter = adapter
        rvDraft.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

    }
    
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        datas.clear()
        adapter.notifyDataSetChanged()
        totalData.text = "Memuat data..."
        val myLevel = getMyLevel(this)
        val wilayah = RoleHelper(this)
        val lahan = when (myLevel) {
            "provinsi" -> db.lahanDao().getVerifiedLahanByProvinsi(komoditas, wilayah.id)
            "kabupaten" -> db.lahanDao().getVerifiedLahanByKabupaten(komoditas, wilayah.id)
            "kecamatan" -> db.lahanDao().getVerifiedLahanByKecamatans(komoditas, wilayah.ids)
            "desa" -> db.lahanDao().getVerifiedLahanByDesa(komoditas, wilayah.id)
            else -> db.lahanDao().getLVerifiedLahan(komoditas)
        }

        val lahanIds = lahan.map { it.id }
        val tanamanOffline = db.draftTanamanDao().getTanamanByLahanIds(komoditas, lahanIds)

        if(tanamanOffline.isNullOrEmpty()){
            totalData.text = "Belum ada data!"
        }else{
            datas.addAll(consumeTanaman(tanamanOffline))
            adapter.notifyDataSetChanged()
            totalData.text = "Total Data : ${datas.size}"
        }
    }

    private suspend fun consumeTanaman(tanaman: List<TanamanDraftEntity>): List<NewTanamanEntity>{
        val newTanaman = mutableListOf<NewTanamanEntity>()
        val lahans = db.lahanDao().getAll(komoditas)
        val owners = db.ownerDao().getAllByKomoditas(komoditas)
        tanaman.forEach {
            val lahan = lahans.find { lahan -> lahan.id == it.lahan_id }
            val owner = owners.find{owner -> lahan?.owner_id == owner.id}
            val dataPanen = db.panenDao().getPanenByTanamanId(it.id)
            val jumlahPanen = if(dataPanen.isNullOrEmpty()) 0.0 else dataPanen.sumOf { it.jumlahpanen.toDoubleOrNull() ?: 0.0 }
            val id = if(it.status == "OFFLINECREATE") it.id else it.currentId!!
            newTanaman.add(
                NewTanamanEntity(
                    id,
                    it.lahan_id,
                    "Tanaman Ke - ${it.tanamanke} Masa Tanam Ke - ${it.masatanam}",
                    "Lahan Ke - ${lahan?.lahanke} (${lahan?.type?.name}) Milik ${owner?.nama} - ${owner?.nama_pok}",
                    lahan?.type?: TypeLahan.MONOKULTUR,
                    lahan?.luas.toString(),
                    jumlahPanen.toString(),
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
        return newTanaman
    }

    private suspend fun deleteData(data: NewTanamanEntity) {
        try {
            withContext(Dispatchers.IO) {
                val act = if(data.status == "OFFLINECREATE"){
                    db.draftTanamanDao().deleteById(data.id)
                }else{
                    db.draftTanamanDao().deleteByCurrentId(data.id)
                }
                withContext(Dispatchers.Main) {
                    if (act > 0) {
                        val fileList = listOf(data.foto1, data.foto2, data.foto3, data.foto4)
                        fileList.forEach { path ->
                            File(path).takeIf { it.exists() }?.let {
                                val deleted = it.delete()
                                Log.d("FileDelete", "File: $path deleted: $deleted")
                            } ?: Log.d("FileDelete", "File: $path not found")
                        }
                        db.draftMediaDao().deleteByUrls(fileList)
                        showSuccess(this@DraftTanaman, "Berhasil", "Data berhasil dihapus") {
                            lifecycleScope.launch { loadData() }
                        }
                    } else {
                        showError(this@DraftTanaman, "Error", "Data gagal dihapus") {
                            lifecycleScope.launch { loadData() }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError(this@DraftTanaman, "Error", "Terjadi kesalahan saat menghapus data") {
                    lifecycleScope.launch { loadData() }
                }
            }
        }
    }

    private suspend fun tryUploadImage(mode: String, data: TanamanDraftEntity) {
        try {
            if(mode == "CREATE"){
                val api = Client.retrofit.create(MediaEndpoint::class.java)
                val media = listOf(data.foto1, data.foto2, data.foto3, data.foto4)
                val fileParts = media.mapIndexed { index, draft ->
                    val file = File(draft) // asumsi draft.url adalah path lokal file

                    val progressBody = ProgressRequestBody(
                        file = file,
                        contentType = "image/*"
                    ) { progress, uploadedMb, totalMb ->
                        runOnUiThread {
                            UploadProgress.show(this@DraftTanaman, progress, uploadedMb, totalMb)
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

                    if(db.draftTanamanDao().insertTanaman(newData) > 0){
                        if(isOnline(this)){
                            tryUploadData(mode, newData)
                        }else{
                            showError(this, "Error", "Gambar berhasil diupload, namun jaringan internet hilang saat akan menyimpan data realisasi tanam!"){
                                lifecycleScope.launch { 
                        loadData()
                    }
                            }
                        }
                    }else{
                        showError(this@DraftTanaman, "Error", "Gagal menyimpan data!") {
                            lifecycleScope.launch { 
                        loadData()
                    }
                        }
                    }
                }
            }else{
                if(isOnline(this)){
                    tryUploadData(mode, data)
                }else{
                    showError(this, "Error", "Jaringan internet tidak tersedia!")
                }
            }

        } catch (e: Exception) {
            showError(this@DraftTanaman, "Upload Error", e.message ?: "Unknown error") {
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

    private suspend fun tryUploadData(mode: String, data: TanamanDraftEntity) {
        Loading.show(this)
        try {
            val api = Client.retrofit.create(TanamanEndpoint::class.java)
            val result = if(mode == "CREATE"){
                api.addTanaman(
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
            }else{
                api.updateTanaman(
                    data.currentId!!,
                    UpdateDataTanam(
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
            }

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
                db.tanamanDao().insertSingle(newInsert)
                db.draftTanamanDao().delete(data)
                showSuccess(this, "Berhasil", "Data berhasil disimpan!"){
                    lifecycleScope.launch { 
                        loadData()
                    }
                }
            }else{
                showError(this@DraftTanaman, "Error", "Gagal menyimpan data!") {
                    lifecycleScope.launch { 
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            showError(this@DraftTanaman, "Error", "${e.message}") {
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