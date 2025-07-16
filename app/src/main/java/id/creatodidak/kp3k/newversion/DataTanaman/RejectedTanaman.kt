package id.creatodidak.kp3k.newversion.DataTanaman

import android.annotation.SuppressLint
import android.content.Intent
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
import id.creatodidak.kp3k.adapter.NewAdapter.TanamanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.RequestClass.DeleteMediaRequest
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.newversion.DataLahan.EditLahan
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.collections.forEach

class RejectedTanaman : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvRejected : RecyclerView
    private lateinit var adapter : TanamanDataDraftVerifikasiAdapter
    private var datas = mutableListOf<ShowDataTanamanByCategory.NewTanamanEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rejected_tanaman)
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
        rvRejected = findViewById(R.id.rvRejected)
        adapter = TanamanDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data -> },
            onDeleteClick = {data -> },
            onKirimDataKeServerUpdateClick = {data -> },
            onKirimDataKeServerCreateClick = {data -> },
            onEdit = {data ->
                val i = Intent(this, EditTanaman::class.java)
                i.putExtra("id", data.id.toString())
                i.putExtra("komoditas", data.komoditas)
                startActivity(i)
            },
            onDeleteOnServer = {data ->
                if(isOnline(this)){
                    lifecycleScope.launch {
                        deleteData(data)
                    }
                }else{
                    showError(this, "Error", "Tidak ada koneksi internet")
                }
            },
        )
        rvRejected.adapter = adapter
        rvRejected.layoutManager = LinearLayoutManager(this)
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
        val tanamans = db.tanamanDao().getRejectedTanamanByLahanIds(komoditas, lahanIds)

        if(tanamans.isNullOrEmpty()){
            totalData.text = "Belum ada data!"
        }else{
            datas.addAll(consumeTanaman(tanamans))
            adapter.notifyDataSetChanged()
            totalData.text = "Total Data : ${datas.size}"
        }
    }

    private suspend fun consumeTanaman(tanaman: List<TanamanEntity>): List<NewTanamanEntity>{
        val newTanaman = mutableListOf<NewTanamanEntity>()
        val lahans = db.lahanDao().getAll(komoditas)
        val owners = db.ownerDao().getAllByKomoditas(komoditas)
        tanaman.forEach {
            val lahan = lahans.find { lahan -> lahan.id == it.lahan_id }
            val owner = owners.find{owner -> lahan?.owner_id == owner.id}
            val jumlahPanen = db.panenDao().getPanenByTanamanId(it.id)
            newTanaman.add(
                NewTanamanEntity(
                    it.id,
                    it.lahan_id,
                    "Tanaman Ke - ${it.tanamanke} Masa Tanam Ke - ${it.masatanam}",
                    "Lahan Ke - ${lahan?.lahanke} (${lahan?.type?.name}) Milik ${owner?.nama} - ${owner?.nama_pok}",
                    lahan?.type?: TypeLahan.MONOKULTUR,
                    lahan?.luas.toString(),
                    jumlahPanen?.jumlahpanen.toString(),
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

    private suspend fun deleteData(data: NewTanamanEntity){
        Loading.show(this)
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(TanamanEndpoint::class.java).deleteTanamanById(data.id)
            }
            if(result.isSuccessful){
                db.tanamanDao().deleteById(data.id)
                lifecycleScope.launch {
                    val listFoto = listOf(data.foto1, data.foto2, data.foto3, data.foto4)
                    tryDeleteImage(listFoto)
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
        }
    }

    private suspend fun tryDeleteImage(imgs: List<String>){
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(MediaEndpoint::class.java).deleteMultipleMedia(
                    DeleteMediaRequest(imgs))
            }

            if(result.isSuccessful){
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