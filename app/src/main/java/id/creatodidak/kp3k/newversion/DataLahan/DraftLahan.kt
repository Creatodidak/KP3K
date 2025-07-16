package id.creatodidak.kp3k.newversion.DataLahan

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.LahanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.adapter.NewAdapter.OwnerDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.RequestClass.LahanAddRequest
import id.creatodidak.kp3k.api.RequestClass.LahanPatchRequest
import id.creatodidak.kp3k.api.newModel.LahanCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.LahanResponseItem
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date
import java.util.Locale

class DraftLahan : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvDraftDO : RecyclerView
    private lateinit var adapter : LahanDataDraftVerifikasiAdapter

    private var datas = mutableListOf<LahanEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_draft_lahan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.error_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvDraftDO = findViewById(R.id.rvDraftDO)
        adapter = LahanDataDraftVerifikasiAdapter(
            lahans = datas,
            onVerifikasi = {data -> },
            onDeleteClick = {data ->
                lifecycleScope.launch { deleteData(data) }
            },
            onKirimDataKeServerUpdateClick = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch { saveDataToServer("UPDATE", data) }
                }else{
                    showError(this@DraftLahan, "Error", "Tidak ada koneksi internet")
                }
            },
            onKirimDataKeServerCreateClick = {data ->
                if(isOnline(this)) {
                    lifecycleScope.launch { saveDataToServer("CREATE", data) }
                }else{
                    showError(this@DraftLahan, "Error", "Tidak ada koneksi internet")
                }
            },
            onEdit = {data -> },
            onDeleteOnServer = {data -> },
            lifecycleOwner = this)
        rvDraftDO.adapter = adapter
        rvDraftDO.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        datas.clear()
        val newData = mutableListOf<LahanEntity>()
        val data = db.draftLahanDao().getOfflineLahan(komoditas)
        data.forEach { it ->
            if(it.status == "OFFLINECREATE"){
                newData.add(
                    LahanEntity(
                        id = it.id,
                        type = it.type,
                        komoditas = it.komoditas,
                        owner_id = it.owner_id,
                        provinsi_id = it.provinsi_id,
                        kabupaten_id = it.kabupaten_id,
                        kecamatan_id = it.kecamatan_id,
                        desa_id = it.desa_id,
                        luas = it.luas,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        status = it.status,
                        alasan = it.alasan,
                        createAt = it.createAt,
                        updateAt = it.updateAt,
                        submitter = it.submitter,
                        lahanke = it.lahanke
                    )
                )
            }else{
                newData.add(
                    LahanEntity(
                        id = it.currentId!!,
                        type = it.type,
                        komoditas = it.komoditas,
                        owner_id = it.owner_id,
                        provinsi_id = it.provinsi_id,
                        kabupaten_id = it.kabupaten_id,
                        kecamatan_id = it.kecamatan_id,
                        desa_id = it.desa_id,
                        luas = it.luas,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        status = it.status,
                        alasan = it.alasan,
                        createAt = it.createAt,
                        updateAt = it.updateAt,
                        submitter = it.submitter,
                        lahanke = it.lahanke
                    )
                )
            }
        }
        datas.addAll(newData)
        totalData.text = "Total Data : ${datas.size}"
        adapter.notifyDataSetChanged()
    }

    private suspend fun saveDataToServer(type: String, data: LahanEntity){
        Loading.show(this)
        try {
            val api = Client.retrofit.create(LahanEndpoint::class.java)
            val submitter = getMyNrp(this)
            val role = getMyRole(this)
            val result = withContext(Dispatchers.IO) {
                if (type == "CREATE") {
                    api.addLahan(
                        LahanAddRequest(
                            data.type.toString(),
                            data.komoditas,
                            data.owner_id,
                            data.provinsi_id,
                            data.kabupaten_id,
                            data.kecamatan_id,
                            data.desa_id,
                            data.luas,
                            data.latitude,
                            data.longitude,
                            "UNVERIFIED",
                            data.alasan,
                            submitter,
                            role,
                            data.lahanke
                        )
                    )
                } else {
                    api.updateLahan(
                        data.id.toString(), LahanPatchRequest(
                            data.type.toString(),
                            data.komoditas,
                            data.owner_id,
                            data.provinsi_id,
                            data.kabupaten_id,
                            data.kecamatan_id,
                            data.desa_id,
                            data.luas,
                            data.latitude,
                            data.longitude,
                            "UNVERIFIED",
                            data.alasan,
                            submitter,
                            role,
                            data.lahanke
                        )
                    )
                }
            }

            if (result.isSuccessful && result.body() != null) {
                val resdata = result.body()!!.data
                if (resdata != null) {
                    val newData = LahanEntity(
                        resdata.id!!,
                        TypeLahan.valueOf(resdata.type!!),
                        resdata.komoditas!!,
                        resdata.ownerId!!,
                        resdata.provinsiId!!,
                        resdata.kabupatenId!!,
                        resdata.kecamatanId!!,
                        resdata.desaId!!,
                        resdata.luas!!,
                        resdata.latitude!!,
                        resdata.longitude!!,
                        resdata.status!!,
                        resdata.alasan,
                        resdata.createAt?.let { parseIsoDate(it) } ?: Date(),
                        resdata.updateAt?.let { parseIsoDate(it) } ?: Date(),
                        resdata.submitter!!,
                        resdata.lahanke!!
                    )

                    withContext(Dispatchers.IO) {
                        db.lahanDao().insert(newData)
                        if (type == "CREATE") {
                            db.draftLahanDao().deleteById(data.id)
                        }else{
                            db.draftLahanDao().deleteByCurrentId(data.id)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        showSuccess(this@DraftLahan, "Berhasil", "Data berhasil dikirim ke server") {
                            lifecycleScope.launch { loadData() }
                        }
                    }
                }
            } else {
                val errorMsg = result.errorBody()?.string()
                    ?: result.body()?.msg ?: "Terjadi kesalahan tidak diketahui"

                withContext(Dispatchers.Main) {
                    showError(this@DraftLahan, "Error", errorMsg) {
                        lifecycleScope.launch { loadData() }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError(this@DraftLahan, "Error", "Data gagal disimpan di server!")
            }
        } finally {
            Loading.hide()
        }
    }

    private suspend fun deleteData(data: LahanEntity) {
        try {
            withContext(Dispatchers.IO) {
                val act = if(data.status == "OFFLINECREATE"){
                    db.draftLahanDao().deleteById(data.id)
                }else{
                    db.draftLahanDao().deleteByCurrentId(data.id)
                }
                withContext(Dispatchers.Main) {
                    if (act > 0) {
                        showSuccess(this@DraftLahan, "Berhasil", "Data berhasil dihapus") {
                            lifecycleScope.launch { loadData() }
                        }
                    } else {
                        showError(this@DraftLahan, "Error", "Data gagal dihapus") {
                            lifecycleScope.launch { loadData() }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError(this@DraftLahan, "Error", "Terjadi kesalahan saat menghapus data") {
                    lifecycleScope.launch { loadData() }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadData()
        }
    }
}