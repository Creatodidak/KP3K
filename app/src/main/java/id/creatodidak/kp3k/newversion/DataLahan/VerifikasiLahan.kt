package id.creatodidak.kp3k.newversion.DataLahan

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
import id.creatodidak.kp3k.adapter.NewAdapter.LahanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.RequestClass.LahanAddRequest
import id.creatodidak.kp3k.api.RequestClass.LahanPatchRequest
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
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
import java.util.Date
import java.util.Locale

class VerifikasiLahan : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvVerifikasiDO : RecyclerView
    private var datas = mutableListOf<LahanEntity>()
    private lateinit var adapter : LahanDataDraftVerifikasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verifikasi_lahan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.yellow_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvVerifikasiDO = findViewById(R.id.rvVerifikasiDO)
        adapter = LahanDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch { saveDataToServer(data) }
                }else{
                    showError(this@VerifikasiLahan, "Error", "Tidak ada koneksi internet")
                }
            },
            onDeleteClick = {data -> },
            onKirimDataKeServerUpdateClick = {data -> },
            onKirimDataKeServerCreateClick = {data -> },
            onEdit = {data -> },
            onDeleteOnServer = {data -> },
            lifecycleOwner = this)
        rvVerifikasiDO.adapter = adapter
        rvVerifikasiDO.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun loadData(){
        datas.clear()
        val newData = mutableListOf<LahanEntity>()
        val data = db.lahanDao().getUnverifiedLahan(komoditas)
        data.forEach { it ->
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
        }
        datas.addAll(newData)
        totalData.text = "Total Data : ${datas.size}"
        adapter.notifyDataSetChanged()
    }
    
    private suspend fun saveDataToServer(data: LahanEntity){
        Loading.show(this)
        try {
            val api = Client.retrofit.create(LahanEndpoint::class.java)
            val result = withContext(Dispatchers.IO) {
                    api.verifikasiLahan(data.id.toString(), VerifikasiRequest(
                        data.status,
                        data.alasan!!,
                        komoditas
                    ))
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
                    }

                    withContext(Dispatchers.Main) {
                        showSuccess(this@VerifikasiLahan, "Berhasil", "Data berhasil diverifikasi") {
                            lifecycleScope.launch { loadData() }
                        }
                    }
                }
            } else {
                val errorMsg = result.errorBody()?.string()
                    ?: result.body()?.msg ?: "Terjadi kesalahan tidak diketahui"

                withContext(Dispatchers.Main) {
                    showError(this@VerifikasiLahan, "Error", errorMsg) {
                        lifecycleScope.launch { loadData() }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError(this@VerifikasiLahan, "Error", "Data gagal diverifikasi karena ${e.message}!")
            }
        } finally {
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