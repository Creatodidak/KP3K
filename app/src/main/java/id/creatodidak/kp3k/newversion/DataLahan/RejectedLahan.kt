package id.creatodidak.kp3k.newversion.DataLahan

import android.content.Intent
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
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RejectedLahan : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvRejectedDO : RecyclerView
    private lateinit var adapter : LahanDataDraftVerifikasiAdapter
    private var datas = mutableListOf<LahanEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rejected_lahan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.gray_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvRejectedDO = findViewById(R.id.rvRejectedDO)
        adapter = LahanDataDraftVerifikasiAdapter(
            lahans = datas,
            onVerifikasi = {data -> },
            onDeleteClick = {data -> },
            onKirimDataKeServerUpdateClick = {data -> },
            onKirimDataKeServerCreateClick = {data -> },
            onEdit = {data ->
                val i = Intent(this, EditLahan::class.java)
                i.putExtra("id", data.id.toString())
                i.putExtra("komoditas", data.komoditas)
                startActivity(i)
            },
            onDeleteOnServer = {data ->
                askUser(this, "Konfirmasi", "Anda yakin ingin menghapus data ini?"){
                    if(isOnline(this)){
                        lifecycleScope.launch {
                            deleteData(data)
                        }
                    }else{
                        showError(this, "Error", "Tidak ada koneksi internet")
                    }
                }
            },
            lifecycleOwner = this)
        rvRejectedDO.adapter = adapter
        rvRejectedDO.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private suspend fun loadData(){
        datas.clear()
        val newData = mutableListOf<LahanEntity>()
        val data = when(getMyLevel(this)){
            "kecamatan" -> db.lahanDao().getRejectedLahanKecamatan(komoditas, RoleHelper(this).ids)
            "desa" -> db.lahanDao().getRejectedLahanDesa(komoditas, RoleHelper(this).id)
            else -> emptyList()
        }
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

    private suspend fun deleteData(data: LahanEntity){
        Loading.show(this)
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(LahanEndpoint::class.java).deleteLahan(data.id.toString())
            }
            if(result.isSuccessful){
                db.lahanDao().delete(data)
                showSuccess(this, "Success", "Data berhasil dihapus"){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }else{
                val msg = result.body()?.msg ?: result.errorBody().toString()
                showError(this, "Error", msg){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            showError(this, "Error", e.message.toString()){
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