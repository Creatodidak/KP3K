package id.creatodidak.kp3k.newversion.DataTanaman

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
import id.creatodidak.kp3k.adapter.NewAdapter.LahanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.adapter.NewAdapter.TanamanDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

class VerifikasiTanaman : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvVerifikasi : RecyclerView
    private var datas = mutableListOf<ShowDataTanamanByCategory.NewTanamanEntity>()
    private lateinit var adapter : TanamanDataDraftVerifikasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verifikasi_tanaman)
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

        adapter = TanamanDataDraftVerifikasiAdapter(
            datas,
            onVerifikasi = {data ->
                if(isOnline(this)){
                    val newdata = TanamanEntity(
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
                    lifecycleScope.launch { saveDataToServer(newdata) }
                }else{
                    showError(this, "Error", "Tidak ada koneksi internet")
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
        val tanamans = db.tanamanDao().getUnverifiedTanamanByLahanIds(komoditas, lahanIds)

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

    private suspend fun saveDataToServer(data: TanamanEntity){
        Loading.show(this)
        try {
            val api = Client.retrofit.create(TanamanEndpoint::class.java)
            val result = withContext(Dispatchers.IO) {
                api.verifikasiTanaman(data.id.toString(), VerifikasiRequest(
                    data.status,
                    data.alasan!!,
                    komoditas
                ))
            }

            if (result.isSuccessful && result.body() != null) {
                val resdata = result.body()!!.data
                if (resdata != null) {
                    val newData = TanamanEntity(
                        resdata.id!!,
                        resdata.lahanId!!,
                        resdata.masatanam!!,
                        resdata.luastanam!!,
                        resdata.tanggaltanam?.let { parseIsoDate(it) }?: Date(),
                        resdata.prediksipanen!!,
                        resdata.rencanatanggalpanen?.let { parseIsoDate(it) }?: Date(),
                        resdata.komoditas!!,
                        resdata.varietas!!,
                        SumberBibit.valueOf(resdata.sumber!!),
                        resdata.keteranganSumber!!,
                        resdata.foto1!!,
                        resdata.foto2!!,
                        resdata.foto3!!,
                        resdata.foto4!!,
                        resdata.status!!,
                        resdata.alasan,
                        resdata.createAt?.let { parseIsoDate(it) } ?: Date(),
                        resdata.updateAt?.let { parseIsoDate(it) } ?: Date(),
                        resdata.submitter!!,
                        resdata.tanamanke!!
                    )

                    withContext(Dispatchers.IO) {
                        db.tanamanDao().insertSingle(newData)
                    }

                    withContext(Dispatchers.Main) {
                        showSuccess(this@VerifikasiTanaman, "Berhasil", "Data berhasil diverifikasi") {
                            lifecycleScope.launch { loadData() }
                        }
                    }
                }
            } else {
                val errorMsg = result.errorBody()?.string()
                    ?: result.body()?.msg ?: "Terjadi kesalahan tidak diketahui"

                withContext(Dispatchers.Main) {
                    showError(this@VerifikasiTanaman, "Error", errorMsg) {
                        lifecycleScope.launch { loadData() }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError(this@VerifikasiTanaman, "Error", "Data gagal diverifikasi karena ${e.message}!")
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