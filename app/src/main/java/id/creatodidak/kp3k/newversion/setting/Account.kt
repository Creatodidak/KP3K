package id.creatodidak.kp3k.newversion.setting

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.BpkpEndpoint
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.newModel.Data
import id.creatodidak.kp3k.api.newModel.UserDataResponse
import retrofit2.Call
import retrofit2.Response
import java.time.Instant

class Account : AppCompatActivity() {
    private lateinit var swlAccount : SwipeRefreshLayout
    private lateinit var fpAccount: ImageView
    private lateinit var tvChangeFpAccount: TextView
    private lateinit var tvNama: TextView
    private lateinit var tvPangkat: TextView
    private lateinit var tvNrp: TextView
    private lateinit var tvPolda: TextView
    private lateinit var tvPolres: TextView
    private lateinit var tvPolsek: TextView
    private lateinit var tvJabatan: TextView
    private lateinit var tvHp: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btUbahDataDiri: Button
    private lateinit var rowPolres: TableRow
    private lateinit var rowPolsek: TableRow
    private val BASE_URL = BuildConfig.BASE_URL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        swlAccount = findViewById(R.id.swlAccount)
        fpAccount = findViewById(R.id.fpAccount)
        tvChangeFpAccount = findViewById(R.id.tvChangeFpAccount)
        tvNama = findViewById(R.id.tvNama)
        tvPangkat = findViewById(R.id.tvPangkat)
        tvNrp = findViewById(R.id.tvNrp)
        tvPolda = findViewById(R.id.tvPolda)
        tvPolres = findViewById(R.id.tvPolres)
        tvPolsek = findViewById(R.id.tvPolsek)
        tvJabatan = findViewById(R.id.tvJabatan)
        tvHp = findViewById(R.id.tvHp)
        tvEmail = findViewById(R.id.tvEmail)
        tvStatus = findViewById(R.id.tvStatus)
        btUbahDataDiri = findViewById(R.id.btUbahDataDiri)
        rowPolres = findViewById(R.id.rowPolres)
        rowPolsek = findViewById(R.id.rowPolsek)

        loadData()

        swlAccount.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData(){
        swlAccount.isRefreshing = true
        val sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val nrp = sh.getString("nrp", "")!!

        val res = Client.retrofit.create(BpkpEndpoint::class.java).getDataPersonil(
            BpkpEndpoint.dataPersRequest(nrp)
        )

        res.enqueue(object : retrofit2.Callback<UserDataResponse>{
            override fun onResponse(
                call: Call<UserDataResponse?>,
                response: Response<UserDataResponse?>
            ) {
                swlAccount.isRefreshing = false
                if(response.isSuccessful){
                    val data = response.body()
                    if(data != null){
                        tvNama.text = data.nama
                        tvPangkat.text = data.pangkat
                        tvNrp.text = data.nrp
                        tvJabatan.text = data.jabatan
                        tvHp.text = data.nohp
                        tvEmail.text = "${data.nrp}@polri.go.id"
                        tvStatus.text = data.status

                        Glide.with(this@Account)
                            .load(BASE_URL+"media"+data.foto)
                            .placeholder(R.drawable.outline_account_circle_24)
                            .circleCrop()
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .into(fpAccount)

                        when (data.satker?.level) {
                            "POLDA" -> {
                                tvPolda.text = data.satker.nama
                                rowPolres.visibility = android.view.View.GONE
                                rowPolsek.visibility = android.view.View.GONE
                            }
                            "POLRES" -> {
                                tvPolres.text = data.satker.nama
                                tvPolda.text = data.satker.parent?.nama
                                rowPolsek.visibility = android.view.View.GONE
                            }
                            "POLSEK" -> {
                                tvPolsek.text = data.satker.nama
                                tvPolres.text = data.satker.parent?.nama
                                tvPolda.text = data.satker.parent?.parent?.nama
                            }
                        }
                        val prefs = sh.edit()
                        with(prefs) {
                            putInt("id", data.id ?: -1)
                            putString("nrp", data.nrp ?: "")
                            putString("nohp", data.nohp ?: "")
                            putString("jabatan", data.jabatan ?: "")
                            putString("pangkat", data.pangkat ?: "")
                            putString("foto", data.foto ?: "")
                            putString("role", data.role ?: "")
                            putString("status", data.status ?: "")
                            putInt("satkerId", data.satkerId ?: -1)
                            putInt("desaBinaanId", data.desaBinaanId ?: -1)
                            putString("password", data.password ?: "")
                            putString("passwordiv", data.passwordiv ?: "")
                            putString("nama", data.nama ?: "")

                            // Satker (nested object)
                            putString("satker_nama", data.satker?.nama ?: "")
                            putString("satker_kode", data.satker?.kode ?: "")
                            putString("satker_level", data.satker?.level ?: "")
                            putInt("satker_id", data.satker?.id ?: -1)
                            putInt("satker_kabupatenId", data.satker?.kabupatenId ?: -1)

                            // Parent Satker
                            putString("satker_parent_nama", data.satker?.parent?.nama ?: "")
                            putString("satker_parent_level", data.satker?.parent?.level ?: "")

                            //Parent of Parent Satker
                            putString("satkerparent_parent_nama", data.satker?.parent?.parent?.nama ?: "")
                            putString("satkerparent_parent_level", data.satker?.parent?.parent?.level ?: "")

                            // Desa Binaan
                            putString("desa_nama", data.desaBinaan?.nama ?: "")
                            putInt("desa_id", data.desaBinaan?.id ?: -1)
                            putInt("desa_kecamatanId", data.desaBinaan?.kecamatanId ?: -1)

                            // Kecamatan
                            putString("kecamatan_nama", data.desaBinaan?.kecamatan?.nama ?: "")
                            putInt("kecamatan_id", data.desaBinaan?.kecamatan?.id ?: -1)

                            // Kabupaten
                            putString("kabupaten_nama", data.desaBinaan?.kecamatan?.kabupaten?.nama ?: "")
                            putInt("kabupaten_id", data.desaBinaan?.kecamatan?.kabupaten?.id ?: -1)

                            // Provinsi
                            putString("provinsi_nama", data.desaBinaan?.kecamatan?.kabupaten?.provinsi?.nama ?: "")
                            putInt("provinsi_id", data.desaBinaan?.kecamatan?.kabupaten?.provinsi?.id ?: -1)

                            apply()
                        }
                    }
                }else{
                    AlertDialog.Builder(this@Account)
                        .setTitle("Error")
                        .setMessage("Gagal mengambil data: ${response.body()?.msg}")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .show()
                }
            }

            override fun onFailure(
                call: Call<UserDataResponse?>,
                t: Throwable
            ) {
                swlAccount.isRefreshing = false
                AlertDialog.Builder(this@Account)
                    .setTitle("Error")
                    .setMessage("Gagal mengambil data: ${t.message}")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
            }

        })


    }
}