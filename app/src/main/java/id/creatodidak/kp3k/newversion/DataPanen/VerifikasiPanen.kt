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
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.api.RequestClass.UpdateDataPanen
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class VerifikasiPanen : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvVerifikasi : RecyclerView
    private var datas = mutableListOf<ShowDataPanenByCategory.NewPanenEntity>()
    private lateinit var adapter : PanenDataDraftVerifikasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verifikasi_panen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.yellow_bg)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        totalData = findViewById(R.id.totalData)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"
        rvVerifikasi = findViewById(R.id.rvVerifikasi)

        adapter = PanenDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch {
                        saveDataToServer(data)
                    }
                }else{
                    showError(this, "Error", "Tidak ada koneksi internet"){
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                }
            },
            onDeleteClick = {data -> },
            onKirimDataKeServerUpdateClick = {data -> },
            onKirimDataKeServerCreateClick = {data -> },
            onEdit = {data -> },
            onDeleteOnServer = {data -> }
        )
        rvVerifikasi.adapter = adapter
        rvVerifikasi.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        datas.clear()
        adapter.notifyDataSetChanged()
        totalData.text = "Memuat data..."
        val tanamans = db.tanamanDao().getAll(komoditas).associateBy { it.id }
        val lahans = db.lahanDao().getAll(komoditas).associateBy { it.id }
        val owners = db.ownerDao().getAllByKomoditas(komoditas).associateBy { it.id }
        val panenVerifikasi = db.panenDao().getPanenUnverified(komoditas)

        panenVerifikasi?.forEach {
            val tanaman = tanamans[it.tanaman_id]!!
            val lahan = lahans[tanaman.lahan_id]!!
            val owner = owners[lahan.owner_id]!!
            val data = ShowDataPanenByCategory.NewPanenEntity(
                it.id,
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

    private suspend fun saveDataToServer(data: ShowDataPanenByCategory.NewPanenEntity){
        Loading.show(this)
        try {
            val result = withContext(Dispatchers.IO) {
                Client.retrofit.create(PanenEndpoint::class.java).verifikasiPanen(
                    data.id.toString(),
                    VerifikasiRequest(
                        data.status,
                        data.alasan!!,
                        data.komoditas,
                    )
                )
            }

            if(result.isSuccessful){
                val newdata = result.body()?.data
                if(newdata !== null){
                    db.panenDao().updateVerified(
                        newdata.id!!,
                        newdata.status!!,
                        newdata.alasan.toString(),
                        newdata.updateAt!!
                    )
                }
                showSuccess(this, "Berhasil", "Data berhasil diverifikasi!"){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }else{
                val msg = result.body()?.msg?:result.errorBody().toString()
                showError(this, "Error", msg){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString()){
                lifecycleScope.launch {
                    loadData()
                }
            }
        }finally {
            Loading.hide()
            lifecycleScope.launch {
                loadData()
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