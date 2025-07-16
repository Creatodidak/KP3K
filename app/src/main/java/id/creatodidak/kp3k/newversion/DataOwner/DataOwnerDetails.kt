package id.creatodidak.kp3k.newversion.DataOwner

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.DialogTitle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.OwnerEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.parseIsoDate
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import java.util.Locale

class DataOwnerDetails : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var komoditas : String
    private lateinit var id : String
    private lateinit var tvKeteranganKomoditas : TextView
    private lateinit var tvTypeOD: TextView
    private lateinit var tvGapkiOD: TextView
    private lateinit var tvNamaPokOD: TextView
    private lateinit var tvNamaCpOD: TextView
    private lateinit var tvNikOD: TextView
    private lateinit var tvAlamatOD: TextView
    private lateinit var tvTeleponOD: TextView
    private lateinit var tvProvinsiOD: TextView
    private lateinit var tvKabupatenOD: TextView
    private lateinit var tvKecamatanOD: TextView
    private lateinit var tvDesaOD: TextView
    private lateinit var tvStatusOD: TextView
    private lateinit var swlDetailOD: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_owner_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        id = intent.getStringExtra("id").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"

        tvTypeOD = findViewById(R.id.tvTypeOD)
        tvGapkiOD = findViewById(R.id.tvGapkiOD)
        tvNamaPokOD = findViewById(R.id.tvNamaPokOD)
        tvNamaCpOD = findViewById(R.id.tvNamaCpOD)
        tvNikOD = findViewById(R.id.tvNikOD)
        tvAlamatOD = findViewById(R.id.tvAlamatOD)
        tvTeleponOD = findViewById(R.id.tvTeleponOD)
        tvProvinsiOD = findViewById(R.id.tvProvinsiOD)
        tvKabupatenOD = findViewById(R.id.tvKabupatenOD)
        tvKecamatanOD = findViewById(R.id.tvKecamatanOD)
        tvDesaOD = findViewById(R.id.tvDesaOD)
        tvStatusOD = findViewById(R.id.tvStatusOD)
        swlDetailOD = findViewById(R.id.swlDetailOD)

        lifecycleScope.launch {
            loadData()
        }

        swlDetailOD.setOnRefreshListener {
            lifecycleScope.launch {
                loadData()
            }
        }
    }

    private suspend fun loadData() {
        swlDetailOD.isRefreshing = true
        try {
            val res = Client.retrofit.create(OwnerEndpoint::class.java).getOwnerDetail(id)
            if (res.isSuccessful) {
                res.body()?.let { response ->
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
                        createAt = parseIsoDate(response.createAt!!) ?: Date(),
                        updatedAt = parseIsoDate(response.updatedAt!!) ?: Date(),
                        submitter = response.submitter!!
                    )
                    dbOwner.insertSingleData(data)
                }
            } else {
                val errorBody = res.errorBody()?.string()
                val msg = try {
                    JSONObject(errorBody ?: "").optString("msg", "Terjadi kesalahan")
                } catch (e: Exception) {
                    "Terjadi kesalahan"
                }
                showError("Error", "${msg}\nMenampilkan data dari database offline!")
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError("Error Exception", "${e.message}\nMenampilkan data dari database offline!")
        }finally {
            lifecycleScope.launch {
                loadDataFromDatabase()
            }
        }
    }

    private suspend fun loadDataFromDatabase() {
        try {
            val data = dbOwner.getOwnerById(id.toInt())
            if(data !== null) {
                tvTypeOD.text = data.type.toString()
                tvGapkiOD.text = data.gapki.toString()
                tvNamaPokOD.text = data.nama_pok
                tvNamaCpOD.text = data.nama
                tvNikOD.text = data.nik
                tvAlamatOD.text = data.alamat
                tvTeleponOD.text = data.telepon
                tvProvinsiOD.text = dbWilayah.getProvinsiById(data.provinsi_id).nama
                tvKabupatenOD.text = dbWilayah.getKabupatenById(data.kabupaten_id).nama
                tvKecamatanOD.text = dbWilayah.getKecamatanById(data.kecamatan_id).nama
                tvDesaOD.text = dbWilayah.getDesaById(data.desa_id).nama
                tvStatusOD.text = data.status
            }else{
                showError("Error", "Data Tidak Ditemukan")
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError("Error Exception Local", e.message.toString())
            Toast.makeText(this@DataOwnerDetails, "ID $id", Toast.LENGTH_SHORT).show()
        }finally {
            swlDetailOD.isRefreshing = false
        }
    }

    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this@DataOwnerDetails)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            .setIcon(R.drawable.outline_warning_24)
            .show()
    }
}