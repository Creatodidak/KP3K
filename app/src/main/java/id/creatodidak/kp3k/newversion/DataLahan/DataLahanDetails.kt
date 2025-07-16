package id.creatodidak.kp3k.newversion.DataLahan

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class DataLahanDetails : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var db : AppDatabase
    private lateinit var komoditas : String
    private lateinit var id : String
    private lateinit var status : String
    private lateinit var kategori : String
    private lateinit var tvKeteranganKomoditas : TextView

    private lateinit var tvType: TextView
    private lateinit var tvOwner: TextView
    private lateinit var tvLuas: TextView
    private lateinit var tvProvinsi: TextView
    private lateinit var tvKabupaten: TextView
    private lateinit var tvKecamatan: TextView
    private lateinit var tvDesa: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDiajukanPada: TextView
    private lateinit var tvDiverifikasiPada: TextView
    private lateinit var tvKeterangan: TextView
    private lateinit var mvRincian: MapView
    private lateinit var swRincian: SwipeRefreshLayout
    private lateinit var btDelete: Button
    private lateinit var btEdit: Button
    private lateinit var tvLahanKe: TextView

    private lateinit var googleMap: GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_lahan_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = getColor(R.color.default_bg)
        db = DatabaseInstance.getDatabase(this)
        komoditas = intent.getStringExtra("komoditas").toString()
        id = intent.getStringExtra("id").toString()
        status = intent.getStringExtra("status").toString()
        kategori = intent.getStringExtra("kategori").toString()

        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize(Locale.ROOT)}"

        tvType = findViewById(R.id.tvType)
        tvOwner = findViewById(R.id.tvOwner)
        tvLuas = findViewById(R.id.tvLuas)
        tvProvinsi = findViewById(R.id.tvProvinsi)
        tvKabupaten = findViewById(R.id.tvKabupaten)
        tvKecamatan = findViewById(R.id.tvKecamatan)
        tvDesa = findViewById(R.id.tvDesa)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvStatus = findViewById(R.id.tvStatus)
        tvDiajukanPada = findViewById(R.id.tvDiajukanPada)
        tvDiverifikasiPada = findViewById(R.id.tvDiverifikasiPada)
        tvKeterangan = findViewById(R.id.tvKeterangan)
        mvRincian = findViewById(R.id.mvRincian)
        swRincian = findViewById(R.id.swRincian)
        btDelete = findViewById(R.id.btDelete)
        btEdit = findViewById(R.id.btEdit)
        tvLahanKe = findViewById(R.id.tvLahanKe)

        if(getMyRole(this) in listOf("ADMINPOLSEK", "BPKP")){
            btDelete.visibility = View.VISIBLE
            btEdit.visibility = View.VISIBLE
        }else{
            btDelete.visibility = View.GONE
            btEdit.visibility = View.GONE
        }

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mvRincian.onCreate(mapViewBundle)
        mvRincian.getMapAsync(this)

        swRincian.setOnRefreshListener {
            lifecycleScope.launch {
                if(isOnline(this@DataLahanDetails)){
                    loadDataOnline()
                }else{
                    loadDataOffline()
                }
                swRincian.isRefreshing = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        map.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMapToolbarEnabled = true
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
                isCompassEnabled = true
            }
            try {
                isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e("MAPS", "Location permission not granted: ${e.message}")
            }
        }

    }

    private suspend fun loadDataOnline(){
        Loading.show(this)
        swRincian.isRefreshing = true
        try {
            if(status == "VERIFIED"){
                val result = withContext(Dispatchers.IO){
                    Client.retrofit.create(LahanEndpoint::class.java).getDetailLahan(id)
                }

                if(result.isSuccessful && result.body() != null){
                    val data = result.body()!!
                    val newdataLahan = LahanEntity(
                        data.id!!,
                        TypeLahan.valueOf(data.type!!),
                        data.komoditas!!,
                        data.ownerId!!,
                        data.provinsiId!!,
                        data.kabupatenId!!,
                        data.kecamatanId!!,
                        data.desaId!!,
                        data.luas!!,
                        data.latitude!!,
                        data.longitude!!,
                        data.status!!,
                        data.alasan,
                        parseIsoDate(data.createAt!!)?: Date(),
                        parseIsoDate(data.updateAt!!)?: Date(),
                        data.submitter!!,
                        data.lahanke!!
                    )
                    db.lahanDao().insert(newdataLahan)
                    loadDataOffline()
                }else{
                    loadDataOffline()
                }
            }else{
                loadDataOffline()
            }
        }catch (e: Exception){
            showError(this, "Error", e.message.toString())
            loadDataOffline()
        }
    }

    private suspend fun loadDataOffline(){
        val data = db.lahanDao().getLahanById(id.toInt())
        val owner = db.ownerDao().getOwnerById(data.owner_id)
        val textOwner = "${owner?.nama} - ${owner?.nama_pok}"
        tvLahanKe.text = data.lahanke
        tvType.text = data.type.toString()
        tvOwner.text = textOwner
        tvLuas.text = "${angkaIndonesia(data.luas.toDouble())} m2 / ${angkaIndonesia(convertToHektar(data.luas.toDouble()))} Ha"
        tvProvinsi.text = db.wilayahDao().getProvinsiById(data.provinsi_id).nama
        tvKabupaten.text = db.wilayahDao().getKabupatenById(data.kabupaten_id).nama
        tvKecamatan.text = db.wilayahDao().getKecamatanById(data.kecamatan_id).nama
        tvDesa.text = db.wilayahDao().getDesaById(data.desa_id).nama
        tvLatitude.text = data.latitude.toString()
        tvLongitude.text = data.longitude.toString()
        tvStatus.text = data.status.toString()
        tvDiajukanPada.text = formatTanggalKeIndonesia(data.createAt.toIsoString())
        tvDiverifikasiPada.text = formatTanggalKeIndonesia(data.updateAt.toIsoString())
        if(data.alasan.isNullOrEmpty()){
            tvKeterangan.text = "-"
        }
        tvKeterangan.text = data.alasan.toString().toUpperCase()
        val location = LatLng(data.latitude.toDouble(), data.longitude.toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        googleMap.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(location))
        Loading.hide()
        swRincian.isRefreshing = false
        btEdit.setOnClickListener {
            val intent = Intent(this, EditLahan::class.java)
            intent.putExtra("id", data.id.toString())
            intent.putExtra("komoditas", komoditas)
            intent.putExtra("kategori", kategori)
            startActivity(intent)
        }

        btDelete.setOnClickListener {
            askUser(this, "Konfirmasi", "Anda yakin ingin menghapus data ini?"){
                if(isOnline(this)){
                    lifecycleScope.launch {
                        deleteData(data)
                    }
                }else{
                    showError(this, "Error", "Tidak ada koneksi internet")
                }
            }
        }
    }

    private suspend fun deleteData(data: LahanEntity){
        try {
            val result = withContext(Dispatchers.IO){
                Client.retrofit.create(LahanEndpoint::class.java).deleteLahan(data.id.toString())
            }
            if(result.isSuccessful){
                db.lahanDao().delete(data)
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
            showError(this, "Error", e.message.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if(isOnline(this@DataLahanDetails)){
                loadDataOnline()
            }else{
                loadDataOffline()
            }
        }
        mvRincian.onResume()
    }

    override fun onStart() {
        super.onStart()
        mvRincian.onStart()
    }

    override fun onStop() {
        super.onStop()
        mvRincian.onStop()
    }

    override fun onPause() {
        mvRincian.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mvRincian.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mvRincian.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mvRincian.onSaveInstanceState(mapViewBundle)
    }
}