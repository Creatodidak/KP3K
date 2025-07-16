package id.creatodidak.kp3k.newversion.DataOwner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.NewAdapter.OwnerDataDraftVerifikasiAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.OwnerEndpoint
import id.creatodidak.kp3k.api.RequestClass.OwnerAddRequest
import id.creatodidak.kp3k.api.RequestClass.OwnerPatchRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class DraftOwner : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvDraftDO : RecyclerView
    private lateinit var adapter : OwnerDataDraftVerifikasiAdapter

    private var datas = mutableListOf<OwnerEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_draft_owner)
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
        adapter = OwnerDataDraftVerifikasiAdapter(
            datas,
            onDisetujui = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDitolak = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDeleteClick = {owner ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Data Pemilik Lahan")
                    .setMessage("Apakah Anda yakin ingin menghapus data pemilik lahan?")
                    .setIcon(R.drawable.query_what_how_why_icon)
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            deleteData(owner)
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onKirimDataKeServerUpdateClick = {owner ->
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin mengirim data pemilik lahan ke server?")
                    .setIcon(R.drawable.query_what_how_why_icon)
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        if(isOnline(this)){
                            lifecycleScope.launch {
                                saveDataToServerUpdate(owner)
                            }
                        }else{
                            showError(this,"Error", "Tidak ada koneksi internet!")
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onKirimDataKeServerCreateClick = {owner ->
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin mengirim data pemilik lahan ke server?")
                    .setIcon(R.drawable.query_what_how_why_icon)
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        if(isOnline(this)){
                            lifecycleScope.launch {
                                saveDataToServer(owner)
                            }
                        }else{
                            showError(this,"Error", "Tidak ada koneksi internet!")
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onEdit = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDeleteOnServer = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            lifecycleOwner = this
        )

        rvDraftDO.adapter = adapter
        rvDraftDO.layoutManager = LinearLayoutManager(this)


        lifecycleScope.launch {
            loadData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        try{
            datas.clear()
            val data = dbOwner.getOfflineData(komoditas)
            datas.addAll(data)
            adapter.notifyDataSetChanged()
            totalData.text = "${data.size} Data Ditemukan!"
        }catch (e: Exception){
            showError(this, "Error",e.message.toString())
        }
    }

    private suspend fun deleteData(ownerEntity: OwnerEntity) {
        Loading.show(this)
        try {
            val result = dbOwner.delete(ownerEntity)
            if (result > 0) {
                Loading.hide()
                AlertDialog.Builder(this)
                    .setTitle("Berhasil")
                    .setMessage("Data berhasil dihapus")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            loadData()
                        }
                    }
                    .setIcon(R.drawable.green_checkmark_line_icon)
                    .show()
            }else{
                Loading.hide()
                showError(this, "Error", "Gagal menghapus data")
            }
        }catch (e: Exception){
            Loading.hide()
            Log.e("DataOwner", "${e.message}")
            showError(this, "Error", "Gagal menghapus data: ${e.message}")
        }
    }

    private suspend fun saveDataToServer(owner: OwnerEntity) {
        Loading.show(this)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            val result = api.addOwner(OwnerAddRequest(
                type = owner.type.name,
                gapki = owner.gapki.name,
                namaPok = owner.nama_pok,
                nama = owner.nama,
                nik = owner.nik,
                alamat = owner.alamat,
                telepon = owner.telepon,
                provinsiId = owner.provinsi_id.toString(),
                kabupatenId = owner.kabupaten_id.toString(),
                kecamatanId = owner.kecamatan_id.toString(),
                desaId = owner.desa_id.toString(),
                status = "UNVERIFIED",
                komoditas = owner.komoditas,
                submitter = owner.submitter,
                role = getMyRole(this)
            ))

            if(result.isSuccessful && result.body() != null){
                Loading.hide()
                val data = result.body()?.data

                dbOwner.delete(owner)
                data?.let { response ->
                    val data = OwnerEntity(
                        id = response.id!!,
                        komoditas = response.komoditas!!,
                        type = TypeOwner.valueOf(response.type!!),
                        gapki = IsGapki.valueOf(response.gapki!!),
                        nama_pok = response.namaPok!!,
                        nama = response.nama!!,
                        nik = response.nik!!,
                        alamat = response.alamat!!,
                        telepon = response.telepon!!,
                        provinsi_id = response.provinsiId!!,
                        kabupaten_id = response.kabupatenId!!,
                        kecamatan_id = response.kecamatanId!!,
                        desa_id = response.desaId!!,
                        status = response.status!!,
                        alasan = response.alasan,
                        createAt = response.createAt?.let { parseIsoDate(it) } ?: Date(),
                        updatedAt = response.updatedAt?.let { parseIsoDate(it) } ?: Date(),
                        submitter = response.submitter!!
                    )
                    val res = dbOwner.upsert(data)
                    if (res > 0) {
                        showSuccess("Berhasil",result.body()?.msg.toString())
                    }else{
                        showError(this, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                            lifecycleScope.launch {
                                loadData()
                            }
                        }
                    }
                }
            }else{
                Loading.hide()
                showError(this, "Error", result.body()?.msg.toString()){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this, "Error", e.message.toString()){
                lifecycleScope.launch {
                    loadData()
                }
            }
        }
    }

    private suspend fun saveDataToServerUpdate(owner: OwnerEntity) {
        Loading.show(this)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            val result = api.updateOwner(owner.id.toString(), OwnerPatchRequest(
                type = owner.type.name,
                gapki = owner.gapki.name,
                namaPok = owner.nama_pok,
                nama = owner.nama,
                nik = owner.nik,
                alamat = owner.alamat,
                telepon = owner.telepon,
                provinsiId = owner.provinsi_id.toString(),
                kabupatenId = owner.kabupaten_id.toString(),
                kecamatanId = owner.kecamatan_id.toString(),
                desaId = owner.desa_id.toString(),
                status = "UNVERIFIED",
                komoditas = owner.komoditas,
                submitter = owner.submitter,
                role = getMyRole(this)
            ))

            if(result.isSuccessful && result.body() != null){
                Loading.hide()
                val data = result.body()?.data
                data?.let { response ->
                    val data = OwnerEntity(
                        id = response.id!!,
                        komoditas = response.komoditas!!,
                        type = TypeOwner.valueOf(response.type!!),
                        gapki = IsGapki.valueOf(response.gapki!!),
                        nama_pok = response.namaPok!!,
                        nama = response.nama!!,
                        nik = response.nik!!,
                        alamat = response.alamat!!,
                        telepon = response.telepon!!,
                        provinsi_id = response.provinsiId!!,
                        kabupaten_id = response.kabupatenId!!,
                        kecamatan_id = response.kecamatanId!!,
                        desa_id = response.desaId!!,
                        status = response.status!!,
                        alasan = response.alasan,
                        createAt = response.createAt?.let { parseIsoDate(it) } ?: Date(),
                        updatedAt = response.updatedAt?.let { parseIsoDate(it) } ?: Date(),
                        submitter = response.submitter!!
                    )
                    val res = dbOwner.updateOwnerDatas(data)
                    if (res > 0) {
                        showSuccess("Berhasil",result.body()?.msg.toString())
                    }else{
                        showError(this, "Error", "Data berhasil disimpan di server, namun gagal disimpan di local database!"){
                            lifecycleScope.launch {
                                loadData()
                            }
                        }
                    }
                }
            }else{
                Loading.hide()
                showError(this, "Error", result.body()?.msg.toString()){
                    lifecycleScope.launch {
                        loadData()
                    }
                }
            }
        }catch (e: Exception){
            Loading.hide()
            showError(this, "Error", e.message.toString()){
                lifecycleScope.launch {
                    loadData()
                }
            }
        }
    }

    private fun showSuccess(title: String, msg: String){
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setIcon(R.drawable.green_checkmark_line_icon)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    loadData()
                }
            }
            .show()
    }
    
}