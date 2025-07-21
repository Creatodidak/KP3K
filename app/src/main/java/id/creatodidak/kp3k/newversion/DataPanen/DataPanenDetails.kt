package id.creatodidak.kp3k.newversion.DataPanen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataPanenDetails : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var id: String
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var swlRincian: SwipeRefreshLayout
    private lateinit var svRincian: ScrollView
    
    private lateinit var tvTanaman: TextView
    private lateinit var tvLahan: TextView
    private lateinit var tvTanggalPanen: TextView
    private lateinit var tvLuasPanen: TextView
    private lateinit var tvJumlahPanen: TextView
    private lateinit var tvPersentase: TextView
    private lateinit var tvKeteranganPanen: TextView
    private lateinit var tvAnalisa: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCreateAt: TextView
    private lateinit var tvDiverifikasiPada: TextView
    private lateinit var tvKeterangan: TextView
    private lateinit var tvShowcaseName: TextView
    private lateinit var tvTargetPanen: TextView
    private lateinit var tvLuasTanam: TextView
    // ImageViews
    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var iv3: ImageView
    private lateinit var iv4: ImageView

    // Buttons
    private lateinit var btHapus: Button
    private lateinit var btEdit: Button
    private var isLoaded: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_panen_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        id = intent.getStringExtra("id").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()}"

        swlRincian = findViewById(R.id.swlRincian)
        svRincian = findViewById(R.id.svRincian)
        svRincian.visibility = View.GONE

        tvShowcaseName = findViewById(R.id.tvShowcaseName)
        // Inisialisasi semua TextView
        tvTanaman = findViewById(R.id.tvTanaman)
        tvLahan = findViewById(R.id.tvLahan)
        tvTanggalPanen = findViewById(R.id.tvTanggalPanen)
        tvLuasPanen = findViewById(R.id.tvLuasPanen)
        tvJumlahPanen = findViewById(R.id.tvJumlahPanen)
        tvPersentase = findViewById(R.id.tvPersentase)
        tvKeteranganPanen = findViewById(R.id.tvKeteranganPanen)
        tvAnalisa = findViewById(R.id.tvAnalisa)
        tvStatus = findViewById(R.id.tvStatus)
        tvCreateAt = findViewById(R.id.tvCreateAt)
        tvDiverifikasiPada = findViewById(R.id.tvDiverifikasiPada)
        tvKeterangan = findViewById(R.id.tvKeterangan)
        tvTargetPanen = findViewById(R.id.tvTargetPanen)
        tvLuasTanam = findViewById(R.id.tvLuasTanam)

        iv1 = findViewById(R.id.iv1)
        iv2 = findViewById(R.id.iv2)
        iv3 = findViewById(R.id.iv3)
        iv4 = findViewById(R.id.iv4)

        btHapus = findViewById(R.id.btHapus)
        btEdit = findViewById(R.id.btEdit)
        swlRincian.setOnRefreshListener {
            lifecycleScope.launch {
                loadData()
            }
        }
    }

    private suspend fun loadData(){
        swlRincian.isRefreshing = true
        svRincian.visibility = ScrollView.GONE
        try {
            val panen = db.panenDao().getPanenById(id.toInt())
            if(panen == null){
                showError(this@DataPanenDetails, "Error", "Data panen tidak ditemukan") {
                    finish()
                }
            }else{
                isLoaded = true
                consumePanen(panen)
            }
        }catch (e: Exception){
            showError(this@DataPanenDetails, "Error", "Terjadi kesalahan saat memuat data: ${e.message}") {
                finish()
            }
        }
    }

    private suspend fun consumePanen(data: PanenEntity){
        val panen = db.panenDao().getPanenByTanamanIdSorted(data.tanaman_id)
        val tanaman = db.tanamanDao().getById(data.tanaman_id)
        val lahan = db.lahanDao().getLahanById(tanaman!!.lahan_id)
        val owner = db.ownerDao().getOwnerById(lahan.owner_id)
        val urutanke = panen.indexOf(data) + 1
        val jumlahpanen = data.jumlahpanen.toDoubleOrNull()?: 0.0
        val prediksipanen = tanaman.prediksipanen.toDoubleOrNull() ?: 0.0
        val persentase = (jumlahpanen / prediksipanen) * 100

        tvShowcaseName.text = "PANEN KE $urutanke"
        tvTanaman.text = "Tanaman Ke - ${tanaman.tanamanke} Masa Tanam - ${tanaman.masatanam}"
        tvLahan.text = "Lahan ${lahan.type.name} Milik ${owner?.nama} - ${owner?.nama_pok}"
        tvTanggalPanen.text = formatTanggalKeIndonesia(data.tanggalpanen.toIsoString())
        tvLuasTanam.text = "${angkaIndonesia(tanaman.luastanam.toDouble())}m²/${angkaIndonesia(convertToHektar(tanaman.luastanam.toDouble()))}Ha"
        tvTargetPanen.text = "${angkaIndonesia(tanaman.prediksipanen.toDouble())}Kg/${angkaIndonesia(convertToTon(tanaman.prediksipanen.toDouble()))}Ton"
        tvLuasPanen.text = "${angkaIndonesia(data.luaspanen.toDouble())}m²/${angkaIndonesia(convertToHektar(data.luaspanen.toDouble()))}Ha"
        tvJumlahPanen.text = "${angkaIndonesia(data.jumlahpanen.toDouble())}Kg/${angkaIndonesia(convertToTon(data.jumlahpanen.toDouble()))}Ton"
        tvPersentase.text = "${angkaIndonesia(persentase)}% Target Panen Terpenuhi"
        tvKeteranganPanen.text= data.keterangan
        tvAnalisa.text = data.analisa
        tvStatus.text = data.status
        tvCreateAt.text = formatTanggalKeIndonesia(data.createAt.toIsoString())
        tvDiverifikasiPada.text = formatTanggalKeIndonesia(data.updateAt.toIsoString())
        tvKeterangan.text = data.alasan
        swlRincian.isRefreshing = false
        svRincian.visibility = View.VISIBLE

        Glide.with(this)
            .load("${BASE_URL}media${data.foto1}")
            .placeholder(R.drawable.notfound)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(iv1)

        Glide.with(this)
            .load("${BASE_URL}media${data.foto2}")
            .placeholder(R.drawable.notfound)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(iv2)

        Glide.with(this)
            .load("${BASE_URL}media${data.foto3}")
            .placeholder(R.drawable.notfound)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(iv3)

        Glide.with(this)
            .load("${BASE_URL}media${data.foto4}")
            .placeholder(R.drawable.notfound)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(iv4)

        btEdit.setOnClickListener {
            val intent = android.content.Intent(this@DataPanenDetails, EditPanen::class.java)
            intent.putExtra("id", data.id.toString())
            intent.putExtra("komoditas", komoditas)
            startActivity(intent)
        }

        btHapus.setOnClickListener {
            if(isOnline(this)){
                askUser(this, "Konfirmasi", "Apakah Anda yakin ingin menghapus data ini?") {
                    lifecycleScope.launch {
                        deleteData(data)
                    }
                }
            }else{
                showError(this@DataPanenDetails, "Error", "Tidak ada koneksi internet")
            }
        }
    }

    private suspend fun deleteData(data: PanenEntity){
        Loading.show(this)
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(PanenEndpoint::class.java).deletePanenById(data.id)
            }

            if(result.isSuccessful){
                db.panenDao().delete(data.id)
                showSuccess(this, "Success", "Data berhasil dihapus"){
                    finish()
                }
            }else{
                showError(this, "Error", result.message()){
                    finish()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString()){
                finish()
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