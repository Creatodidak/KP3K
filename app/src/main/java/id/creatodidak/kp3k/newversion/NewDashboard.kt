package id.creatodidak.kp3k.newversion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.FirebaseEndpoint
import id.creatodidak.kp3k.api.RequestClass.FirebaseRequest
import id.creatodidak.kp3k.api.WilayahEndpoint
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.*
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.isPejabat
import id.creatodidak.kp3k.newversion.dashboard.DashboardBPKP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.InputStreamReader
import androidx.core.content.edit
import kotlinx.coroutines.tasks.await

class NewDashboard : AppCompatActivity() {

    private var wilayahLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch {
            try {
                DatabaseInstance.getDatabase(this@NewDashboard).wilayahDao().getProvinsi()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        if (!wilayahLoaded) {
            wilayahLoaded = true
            loadDataWilayah()
        }
    }

    private fun loadDataWilayah() {
        if (isOnline(this)) {
            lifecycleScope.launch {
                loadDataFromJsonFile()
            }
        } else {
            lifecycleScope.launch {
                loadFCM()
            }
        }
    }

    private suspend fun loadFCM() {
        val prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val nrp = prefs.getString("nrp", "") ?: ""
        val isTokenAvailable = prefs.getBoolean("IS_FCM_TOKEN_AVAILABLE", false)
        val isTokenOnline = prefs.getBoolean("IS_FCM_TOKEN_ONLINE", false)

        try {
            // 1. Token tersedia dan sudah online → lanjut ke dashboard
            if (isTokenAvailable && isTokenOnline) {
                goToDashboard()
                return
            }

            val fcmToken: String = if (!isTokenAvailable) {
                // 2. Token tidak tersedia → ambil baru
                val newToken = FirebaseMessaging.getInstance().token.await()
                prefs.edit {
                    putString("FCM_TOKEN", newToken)
                    putBoolean("IS_FCM_TOKEN_AVAILABLE", true)
                }
                newToken
            } else {
                // 3. Token sudah ada di local
                prefs.getString("FCM_TOKEN", "") ?: ""
            }

            // Kirim token ke server
            val response = Client.retrofit
                .create(FirebaseEndpoint::class.java)
                .saveFCMTokenToServer(FirebaseRequest(nrp, fcmToken))

            if (response.isSuccessful) {
                prefs.edit {
                    putBoolean("IS_FCM_TOKEN_ONLINE", true)
                }
            } else {
                prefs.edit {
                    putBoolean("IS_FCM_TOKEN_ONLINE", false)
                }
                Log.e("FCM", "❌ Gagal simpan FCM ke server: ${response.code()}")
            }
        } catch (e: Exception) {
            prefs.edit {
                putBoolean("IS_FCM_TOKEN_AVAILABLE", false)
                putBoolean("IS_FCM_TOKEN_ONLINE", false)
            }
            Log.e("FCM", "❌ Gagal ambil atau kirim token", e)
        } finally {
            goToDashboard()
        }
    }


    private fun goToDashboard() {
        startActivity(Intent(this, DashboardBPKP::class.java))
        finish()
    }

    inline fun <reified T> parseJsonFile(file: File): List<T> {
        val gson = Gson()
        val reader = InputStreamReader(file.inputStream())
        val listType = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(reader, listType)
    }

    suspend inline fun <reified T> handleWilayahJson(
        responseBody: okhttp3.ResponseBody?,
        fileName: String,
        tableName: String,
        crossinline onParsed: suspend (List<T>) -> Unit
    ) {
        val file = File(cacheDir, fileName)
        withContext(Dispatchers.IO) {
            responseBody?.byteStream()?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            val dataList = parseJsonFile<T>(file)
            Log.d("WilayahLoad", "✅ Parsed $tableName -> ${dataList.size} data")

            onParsed(dataList)

            Log.d("WilayahLoad", "📥 Inserted $tableName -> ${dataList.size} ke DB")

            file.delete()
        }
    }

    private suspend fun loadDataFromJsonFile() {
        try {
            val db = DatabaseInstance.getDatabase(this)
            val dao = db.wilayahDao()
            val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
            val api = Client.retrofit.create(WilayahEndpoint::class.java)

            // === PROVINSI ===
            val provId = sh.getInt("satker_provinsiId", 0)
            Log.d("WilayahLoad", "📥 ProvinsiId: $provId")
            val provinsi = api.getProvinsi(WilayahEndpoint.wilayahRequest(listOf(provId)))
            if (provinsi.isSuccessful) {
                val fileName = provinsi.headers()["X-Filename"] ?: "provinsi.json"
                handleWilayahJson<ProvinsiEntity>(provinsi.body(), fileName, "Provinsi") {
                    dao.insertProvinsi(it)
                    val total = dao.getProvinsi().size
                    Log.d("WilayahLoad", "📊 Total Provinsi di DB: $total")
                }
            }

            // === KABUPATEN ===
            val provIds = dao.getProvinsi().map { it.id }
            if (provIds.isEmpty()) {
                Log.w("WilayahLoad", "⚠️ Tidak ada data Provinsi untuk fetch Kabupaten.")
                return
            }

            val kabupaten = api.getKabupaten(WilayahEndpoint.wilayahRequest(provIds))
            if (kabupaten.isSuccessful) {
                val fileName = kabupaten.headers()["X-Filename"] ?: "kabupaten.json"
                handleWilayahJson<KabupatenEntity>(kabupaten.body(), fileName, "Kabupaten") {
                    dao.insertKabupaten(it)
                    val total = dao.getKabupaten().size
                    Log.d("WilayahLoad", "📊 Total Kabupaten di DB: $total")
                }
            }

            // === KECAMATAN ===
            val kabIds = dao.getKabupaten().map { it.id }
            if (kabIds.isEmpty()) {
                Log.w("WilayahLoad", "⚠️ Tidak ada data Kabupaten untuk fetch Kecamatan.")
                return
            }

            val kecamatan = api.getKecamatan(WilayahEndpoint.wilayahRequest(kabIds))
            if (kecamatan.isSuccessful) {
                val fileName = kecamatan.headers()["X-Filename"] ?: "kecamatan.json"
                handleWilayahJson<KecamatanEntity>(kecamatan.body(), fileName, "Kecamatan") {
                    dao.insertKecamatan(it)
                    val total = dao.getKecamatan().size
                    Log.d("WilayahLoad", "📊 Total Kecamatan di DB: $total")
                }
            }

            // === DESA ===
            val kecIds = dao.getKecamatan().map { it.id }
            if (kecIds.isEmpty()) {
                Log.w("WilayahLoad", "⚠️ Tidak ada data Kecamatan untuk fetch Desa.")
                return
            }

            val desa = api.getDesa(WilayahEndpoint.wilayahRequest(kecIds))
            if (desa.isSuccessful) {
                val fileName = desa.headers()["X-Filename"] ?: "desa.json"
                handleWilayahJson<DesaEntity>(desa.body(), fileName, "Desa") {
                    dao.insertDesa(it)
                    val total = dao.getDesaByKecamatan(kecIds.first()).size
                    Log.d("WilayahLoad", "📊 Total Desa sample (kecId=${kecIds.first()}): $total")
                }
            }
            val nrp = sh.getString("nrp", "") ?: ""
            val isPejabat = isPejabat(this@NewDashboard)

            val dataSatker: Response<ResponseBody>? = if (isPejabat) {
                api.getSatkerData("pejabat", nrp)
            } else {
                api.getSatkerData("personil", nrp)
            }

            dataSatker?.let { response ->
                if (response.isSuccessful) {
                    val fileName = response.headers()["X-Filename"] ?: "satker.json"
                    handleWilayahJson<SatkerEntity>(response.body(), fileName, "satker") {
                        dao.insertSatker(it)
                        val total = dao.getSatker().size
                        Log.d("WilayahLoad", "📊 Total Satker di DB: $total")
                    }
                } else {
                    Log.e("WilayahLoad", "❌ Gagal fetch data satker: ${response.code()}")
                }
            } ?: Log.e("WilayahLoad", "❌ Response dataSatker null, role mungkin tidak valid")


            val dataPivot: Response<ResponseBody>? = if (isPejabat) {
                api.getPolsekPivot("pejabat", nrp)
            } else {
                api.getPolsekPivot("personil", nrp)
            }

            dataPivot?.let { response ->
                if (response.isSuccessful) {
                    val fileName = response.headers()["X-Filename"] ?: "pivot.json"
                    handleWilayahJson<PolsekPivotEntity>(response.body(), fileName, "polsekpivot") {
                        dao.insertPolsekPivot(it)
                        val total = dao.getPolsekPivot().size
                        Log.d("WilayahLoad", "📊 Total Polsek Pivot di DB: $total")
                    }
                } else {
                    Log.e("WilayahLoad", "❌ Gagal fetch data polsek pivot: HTTP ${response.code()} ${response.message()}")
                }
            } ?: Log.e("WilayahLoad", "❌ Response Polsek Pivot null, kemungkinan role tidak valid")

        } catch (e: Exception) {
            Log.e("WilayahLoad", "❌ Gagal load data wilayah", e)
        } finally {
            lifecycleScope.launch {
                loadFCM()
            }
        }
    }
}
