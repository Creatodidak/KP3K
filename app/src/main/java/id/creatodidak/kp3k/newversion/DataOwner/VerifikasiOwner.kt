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
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.KonfirmasiTolak
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.getMyNamePangkat
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class VerifikasiOwner : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var totalData : TextView
    private lateinit var rvVerifikasiDO : RecyclerView
    private lateinit var adapter : OwnerDataDraftVerifikasiAdapter

    private var datas = mutableListOf<OwnerEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verifikasi_owner)
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
        adapter = OwnerDataDraftVerifikasiAdapter(
            datas,
            onDisetujui = {owner ->
                val req = VerifikasiRequest(
                    alasan = "Disetujui oleh ${getMyNamePangkat(this)}",
                    status = "VERIFIED",
                    komoditas = owner.komoditas
                )
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin menyetujui data ini?")
                    .setIcon(R.drawable.query_what_how_why_icon)
                    .setPositiveButton("Ya") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            saveDataToServer(owner.id.toString(), req)
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onDitolak = {owner ->
                KonfirmasiTolak.show(this,
                    onBatal = {},
                    onLanjut = {d ->
                        val req = VerifikasiRequest(
                            alasan = "Ditolak oleh ${getMyNamePangkat(this)} karena $d",
                            status = "REJECTED",
                            komoditas = owner.komoditas
                        )
                        lifecycleScope.launch {
                            saveDataToServer(owner.id.toString(), req)
                        }
                    }
                )
            },
            onDeleteClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onKirimDataKeServerUpdateClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onKirimDataKeServerCreateClick = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onEdit = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            onDeleteOnServer = {owner ->
                Log.d("DataOwner", "Verifikasi: $owner")
            },
            lifecycleOwner = this
        )

        rvVerifikasiDO.adapter = adapter
        rvVerifikasiDO.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            loadData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadData(){
        try{
            datas.clear()
            val data = dbOwner.getUnverifiedData(komoditas)
            datas.addAll(data)
            adapter.notifyDataSetChanged()
            totalData.text = "${data.size} Data Ditemukan!"
        }catch (e: Exception){
            showError(this, "Error",e.message.toString())
        }
    }

    private suspend fun saveDataToServer(id: String, req: VerifikasiRequest) {
        Loading.show(this)
        try {
            val api = Client.retrofit.create(OwnerEndpoint::class.java)
            val result = api.verifikasiOwner(id, req)

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