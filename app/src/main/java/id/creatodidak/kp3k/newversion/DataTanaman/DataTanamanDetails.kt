package id.creatodidak.kp3k.newversion.DataTanaman

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.MediaEndpoint
import id.creatodidak.kp3k.api.RequestClass.DeleteMediaRequest
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.AturReminder
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getReminderList
import id.creatodidak.kp3k.helper.isReminderExists
import id.creatodidak.kp3k.helper.saveReminderList
import id.creatodidak.kp3k.helper.setAlarmReminder
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.collections.forEach

class DataTanamanDetails : AppCompatActivity() {
    private lateinit var db : AppDatabase
    private lateinit var id: String
    private lateinit var komoditas : String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var swlRincian: SwipeRefreshLayout
    private lateinit var svRincian: ScrollView

    // TextViews
    private lateinit var tvShowcaseName: TextView
    private lateinit var tvLahan: TextView
    private lateinit var tvTypeLahan: TextView
    private lateinit var tvLuasLahan: TextView
    private lateinit var tvTanggalTanam: TextView
    private lateinit var tvLuasTanam: TextView
    private lateinit var tvPersentaseTanam: TextView
    private lateinit var tvMasaTanam: TextView
    private lateinit var tvPrediksiJumlahPanen: TextView
    private lateinit var tvPrediksiTanggalPanen: TextView
    private lateinit var tvVarietasBibit: TextView
    private lateinit var tvSumberBibit: TextView
    private lateinit var tvKeteranganSumberBibit: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCreateAt: TextView
    private lateinit var tvDiverifikasiPada: TextView
    private lateinit var tvKeterangan: TextView

    // ImageViews
    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var iv3: ImageView
    private lateinit var iv4: ImageView

    // Buttons
    private lateinit var btHapus: Button
    private lateinit var btEdit: Button

    private lateinit var lySetReminder: LinearLayout
    private lateinit var ivSetReminder: ImageView
    private lateinit var tvSetReminder: TextView

    private var isLoaded: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_tanaman_details)
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

        tvShowcaseName = findViewById(R.id.tvShowcaseName)
        tvLahan = findViewById(R.id.tvLahan)
        tvTypeLahan = findViewById(R.id.tvTypeLahan)
        tvLuasLahan = findViewById(R.id.tvLuasLahan)
        tvTanggalTanam = findViewById(R.id.tvTanggalTanam)
        tvLuasTanam = findViewById(R.id.tvLuasTanam)
        tvPersentaseTanam = findViewById(R.id.tvPersentaseTanam)
        tvMasaTanam = findViewById(R.id.tvMasaTanam)
        tvPrediksiJumlahPanen = findViewById(R.id.tvPrediksiJumlahPanen)
        tvPrediksiTanggalPanen = findViewById(R.id.tvPrediksiTanggalPanen)
        tvVarietasBibit = findViewById(R.id.tvVarietasBibit)
        tvSumberBibit = findViewById(R.id.tvSumberBibit)
        tvKeteranganSumberBibit = findViewById(R.id.tvKeteranganSumberBibit)
        tvStatus = findViewById(R.id.tvStatus)
        tvCreateAt = findViewById(R.id.tvCreateAt)
        tvDiverifikasiPada = findViewById(R.id.tvDiverifikasiPada)
        tvKeterangan = findViewById(R.id.tvKeterangan)

        lySetReminder = findViewById(R.id.setReminder)
        ivSetReminder = findViewById(R.id.ivSetReminder)
        tvSetReminder = findViewById(R.id.tvSetReminder)

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
            val tanaman = db.tanamanDao().getById(id.toInt())
            if(tanaman == null){
                showError(this@DataTanamanDetails, "Error", "Data tanaman tidak ditemukan") {
                    finish()
                }
            }else{
                consumeTanaman(tanaman)
            }
        }catch (e: Exception){
            showError(this@DataTanamanDetails, "Error", "Terjadi kesalahan saat memuat data: ${e.message}") {
                finish()
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private suspend fun consumeTanaman(tanaman: TanamanEntity){
        try {
            val tanamans = db.tanamanDao().getById(id.toInt())
            val lahans = db.lahanDao().getAll(komoditas)
            val owners = db.ownerDao().getAllByKomoditas(komoditas)
            val lahan = lahans.find { lahan -> lahan.id == tanaman.lahan_id }
            val owner = owners.find{owner -> lahan?.owner_id == owner.id}
            val dataPanen = db.panenDao().getPanenByTanamanId(tanaman.id)
            val jumlahPanen = if(dataPanen.isNullOrEmpty()) 0.0 else dataPanen.sumOf { it.jumlahpanen.toDoubleOrNull() ?: 0.0 }

            if(tanamans == null){
                showError(this@DataTanamanDetails, "Error", "Data tanaman tidak ditemukan") {
                    finish()
                }
            }else{
                val data = tanamans.let {
                    NewTanamanEntity(
                        it.id,
                        it.lahan_id,
                        "Tanaman Ke - ${it.tanamanke} Masa Tanam Ke - ${it.masatanam}",
                        "Lahan Ke - ${lahan?.lahanke} (${lahan?.type?.name}) Milik ${owner?.nama} - ${owner?.nama_pok}",
                        lahan?.type?: TypeLahan.MONOKULTUR,
                        lahan?.luas.toString(),
                        jumlahPanen.toString(),
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
                }
                isLoaded = true
                btHapus.setOnClickListener {
                    askUser(this, "Konfirmasi", "Anda yakin akan menghapus data ini?"){
                        lifecycleScope.launch {
                            deleteData(data)
                        }
                    }
                }
                btEdit.setOnClickListener {
                    val i = Intent(this, EditTanaman::class.java)
                    i.putExtra("id", data.id.toString())
                    i.putExtra("komoditas", data.komoditas)
                    startActivity(i)
                }
                data.let{
                    val luasTanam = it.luastanam.toDouble()
                    val luasLahan = it.luasLahan.toDouble()
                    val persentaseTanam = (luasTanam / luasLahan) * 100
                    tvShowcaseName.text = it.showCaseName
                    tvLahan.text = it.lahanWithOwner
                    tvTypeLahan.text = it.typeLahan.name
                    tvLuasLahan.text = "${it.luasLahan}m²/${angkaIndonesia(convertToHektar(luasLahan))}Ha"
                    tvTanggalTanam.text = formatTanggalKeIndonesia(it.tanggaltanam.toIsoString())
                    tvLuasTanam.text = "${it.luastanam}m²/${angkaIndonesia(convertToHektar(luasTanam))}Ha"
                    tvPersentaseTanam.text = "${angkaIndonesia(persentaseTanam)}%"
                    tvMasaTanam.text = "Masa Tanam Ke - ${it.masatanam}"
                    tvPrediksiJumlahPanen.text = "${it.prediksipanen}KG/${angkaIndonesia(convertToTon(it.prediksipanen.toDouble()))}Ton"
                    tvPrediksiTanggalPanen.text = formatTanggalKeIndonesia(it.rencanatanggalpanen.toIsoString())
                    tvVarietasBibit.text = it.varietas
                    tvSumberBibit.text = it.sumber.name
                    tvKeteranganSumberBibit.text = it.keteranganSumber
                    tvStatus.text = it.status
                    tvCreateAt.text = formatTanggalKeIndonesia(it.createAt.toIsoString())
                    tvDiverifikasiPada.text = formatTanggalKeIndonesia(it.updateAt.toIsoString())
                    tvKeterangan.text = it.alasan

                    val reminderList = getReminderList(this)
                    val isiReminder = "Akan dilaksanakan Panen di ${it.showCaseName} lahan ${it.lahanWithOwner} Pada Hari Ini"
                    if(isReminderExists(this, it.id.toString())){
                        tvSetReminder.text = "Alarm Pengingat Sudah Diatur!"
                        lySetReminder.setOnClickListener(null)
                    }else{
                        tvSetReminder.text = "Atur Alarm Pengingat"
                        lySetReminder.setOnClickListener {
                            AturReminder.show(this) { tanggal, jam ->
                                val reminder = JSONObject().apply {
                                    put("id", id)
                                    put("tanggal", tanggal)
                                    put("jam", jam)
                                    put("isi", "Akan dilaksanakan Panen di lahan ${isiReminder} pada tanggal $tanggal jam $jam")
                                    put("alarmAktif", true)
                                }
                                tvSetReminder.text = "Alarm Pengingat Sudah Diatur!"
                                reminderList.put(reminder)
                                saveReminderList(this, reminderList)
                                setAlarmReminder(this, id, tanggal, jam, reminder.getString("isi"))
                                showSuccess(this, "Success", "Alarm Pengingat Berhasil Diatur!")
                            }
                        }
                    }

                    Glide.with(this)
                        .load("${BASE_URL}media${it.foto1}")
                        .placeholder(R.drawable.notfound)
                        .into(iv1)
                    Glide.with(this)
                        .load("${BASE_URL}media${it.foto2}")
                        .placeholder(R.drawable.notfound)
                        .into(iv2)
                    Glide.with(this)
                        .load("${BASE_URL}media${it.foto3}")
                        .placeholder(R.drawable.notfound)
                        .into(iv3)
                    Glide.with(this)
                        .load("${BASE_URL}media${it.foto4}")
                        .placeholder(R.drawable.notfound)
                        .into(iv4)
                }
            }
        }catch (e: Exception){
            showError(this@DataTanamanDetails, "Error", "Terjadi kesalahan saat memuat data: ${e.message}") {
                finish()
            }
        }finally {
            swlRincian.isRefreshing = false
            svRincian.visibility = ScrollView.VISIBLE
        }
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
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this, "Error", e.message.toString()){
                finish()
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
                    finish()
                }
            }else{
                val msg = result.body()?.msg ?: result.errorBody().toString()
                showError(this, "Error", msg){
                    finish()
                }
            }
        }catch (e: Exception){
            showError(this, "Error", e.message.toString()){
                finish()
            }
        }finally {
            Loading.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isLoaded){
            finish()
        }else{
            lifecycleScope.launch {
                loadData()
            }
        }
    }
}