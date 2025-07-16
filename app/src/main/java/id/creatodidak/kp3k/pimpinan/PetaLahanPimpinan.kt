package id.creatodidak.kp3k.pimpinan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.*
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.DataPimpinan
import id.creatodidak.kp3k.api.model.PersonilItem
import id.creatodidak.kp3k.api.model.SocketLahanfixItems
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.LocationHelperOld
import kotlinx.coroutines.launch

class PetaLahanPimpinan : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var mapLahanPimpinan: MapView
    private lateinit var cbKabupaten: LinearLayout
    private lateinit var ivFilterLahan: ImageView
    private lateinit var lyFilterLahan: LinearLayout
    private lateinit var tvFilterLahan: TextView
    private val checkBoxMap = mutableMapOf<String, CheckBox>()
    private lateinit var checkBoxAll: CheckBox
    private lateinit var clusterManager: ClusterManager<UnifiedItem>
    private val myItemList = mutableListOf<UnifiedItem>()

    private lateinit var cachedMonoIcon: BitmapDescriptor
    private lateinit var cachedMultiIcon: BitmapDescriptor
    private lateinit var cachedPersonilIcon: BitmapDescriptor
    private lateinit var cachedPersonilIconOffline: BitmapDescriptor
    private var selectedLahan: SocketLahanfixItems? = null
    private var selectedPersonil: PersonilItem? = null
    private lateinit var cvBottomInfo: CardView
    private lateinit var cvTopInfo: CardView
    private lateinit var lyVideoCall: LinearLayout
    private lateinit var tvNamaPers: TextView
    private lateinit var tvNrpPangkat: TextView
    private lateinit var tvSatker: TextView
    private lateinit var tvBinaan: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivPers: ImageView
    private lateinit var tvType: TextView
    private lateinit var tvPemilik: TextView
    private lateinit var tvAlamat: TextView
    private lateinit var tvLuas: TextView
    private lateinit var ivClose: ImageView
    private lateinit var sh: SharedPreferences
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) enableMyLocation() else showPermissionDeniedDialog()
    }

    data class UnifiedItem(
        private val latLng: LatLng,
        private val titleText: String,
        val type: String, // "LAHAN" atau "PERSONIL"
        val lahan: SocketLahanfixItems? = null,
        val personil: PersonilItem? = null
    ) : ClusterItem {
        override fun getPosition(): LatLng = latLng
        override fun getTitle(): String = titleText
        override fun getSnippet(): String? = null
        override fun getZIndex(): Float? {
            TODO("Not yet implemented")
        }
    }

    class UnifiedRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<UnifiedItem>,
        private val monoIcon: BitmapDescriptor,
        private val multiIcon: BitmapDescriptor,
        private val activeIcon: BitmapDescriptor,
        private val inactiveIcon: BitmapDescriptor
    ) : DefaultClusterRenderer<UnifiedItem>(context, map, clusterManager) {
        override fun onBeforeClusterItemRendered(item: UnifiedItem, markerOptions: MarkerOptions) {
            val icon = when (item.type) {
                "LAHAN" -> when (item.lahan?.type) {
                    "MONOKULTUR" -> monoIcon
                    "TUMPANGSARI" -> multiIcon
                    else -> monoIcon
                }
                "PERSONIL" -> if (!item.personil?.token?.token.isNullOrEmpty()) activeIcon else inactiveIcon
                else -> monoIcon
            }
            markerOptions.icon(icon)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<UnifiedItem>) = cluster.size > 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sh = getSharedPreferences("session", MODE_PRIVATE)
        val role = sh.getString("role", "")
        val kabupaten = sh.getString("kabupaten_id", "")

        setContentView(R.layout.activity_peta_lahan_pimpinan)

        mapLahanPimpinan = findViewById(R.id.mapLahanPimpinan)
        cbKabupaten = findViewById(R.id.cbKabupaten)
        ivFilterLahan = findViewById(R.id.ivFilterLahan)
        lyFilterLahan = findViewById(R.id.lyFilterLahan)
        tvFilterLahan = findViewById(R.id.tvFilterLahan)
        cvBottomInfo = findViewById(R.id.cvBottomInfo)
        cvTopInfo = findViewById(R.id.cvTopInfo)
        lyVideoCall = findViewById(R.id.lyVideoCall)
        tvNamaPers = findViewById(R.id.tvNamaPers)
        tvNrpPangkat = findViewById(R.id.tvNrpPangkat)
        tvSatker = findViewById(R.id.tvSatker)
        tvBinaan = findViewById(R.id.tvBinaan)
        tvStatus = findViewById(R.id.tvStatus)
        ivPers = findViewById(R.id.ivPers)
        tvType = findViewById(R.id.tvType)
        tvPemilik = findViewById(R.id.tvPemilik)
        tvAlamat = findViewById(R.id.tvAlamat)
        tvLuas = findViewById(R.id.tvLuas)
        ivClose = findViewById(R.id.ivClose)

        lyFilterLahan.setOnClickListener {
            val show = cbKabupaten.visibility == LinearLayout.GONE
            cbKabupaten.visibility = if (show) LinearLayout.VISIBLE else LinearLayout.GONE
            ivFilterLahan.setImageResource(if (show) R.drawable.baseline_keyboard_arrow_up_24 else R.drawable.baseline_keyboard_arrow_down_24)
            tvFilterLahan.text = if (show) "TUTUP FILTER" else "FILTER LAHAN"
        }

        ivClose.setOnClickListener {
            cvBottomInfo.visibility = LinearLayout.GONE
            cvTopInfo.visibility = LinearLayout.GONE
            val currentZoom = googleMap.cameraPosition.zoom
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 13))
        }

        mapLahanPimpinan.onCreate(savedInstanceState)
        mapLahanPimpinan.getMapAsync(this)

        cachedMonoIcon = resizeMapIcons(this, R.drawable.markerlahan, 80, 80)
        cachedMultiIcon = resizeMapIcons(this, R.drawable.markermulti, 80, 80)
        cachedPersonilIcon = resizeMapIcons(this, R.drawable.polaktif, 80, 80)
        cachedPersonilIconOffline = resizeMapIcons(this, R.drawable.polnonaktif, 80, 80)

        if(role.equals("PIMPINAN")){
            lifecycleScope.launch { loadDataPeta() }
        }else{
            lifecycleScope.launch { loadDataPetaWilayah(kabupaten!!) }
        }
    }

    private suspend fun loadDataPeta() {
        Loading.show(this)
        try {
            val res = Client.retrofit.create(DataPimpinan::class.java).getPetaData()
            val totalLahan = res.dataLahan?.sumOf { it?.lahanfix?.size ?: 0 }
            checkBoxAll = CheckBox(this).apply {
                text = "SEMUA ($totalLahan)"
                isChecked = true
            }
            cbKabupaten.addView(checkBoxAll)

            checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
                checkBoxMap.values.forEach {
                    it.setOnCheckedChangeListener(null)
                    it.isChecked = isChecked
                }
                updateMapMarkers()
                checkBoxMap.values.forEach { cb ->
                    cb.setOnCheckedChangeListener { _, _ -> handleKabupatenCheckboxChange() }
                }
            }

            res.dataLahan?.forEach { x ->
                val namaKab = x?.nama ?: return@forEach
                val jumlahLahan = x.lahanfix?.size ?: 0

                val cb = CheckBox(this).apply {
                    text = "$namaKab ($jumlahLahan)"
                    isChecked = true
                }
                cbKabupaten.addView(cb)
                checkBoxMap[namaKab] = cb
                cb.setOnCheckedChangeListener { _, _ -> handleKabupatenCheckboxChange() }

                x.lahanfix?.forEach { lahan ->
                    val lat = lahan?.latitude?.toDoubleOrNull()
                    val lng = lahan?.longitude?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        myItemList.add(UnifiedItem(LatLng(lat, lng), namaKab, "LAHAN", lahan = lahan))
                    }
                }
            }

            res.dataPersonil?.forEach { x ->
                x?.personil?.forEach { personil ->
                    val lat = personil?.tracking?.latitude?.toDoubleOrNull()
                    val lng = personil?.tracking?.longitude?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        myItemList.add(UnifiedItem(LatLng(lat, lng), personil.nama ?: "Personil", "PERSONIL", personil = personil))
                    }
                }
            }

            updateMapMarkers()
        } catch (e: Exception) {
            showErrorDialog("Terjadi kesalahan: ${e.message}")
        } finally {
            Loading.hide()
        }
    }
    private suspend fun loadDataPetaWilayah(kabupaten: String) {
        Loading.show(this)
        try {
            val res = Client.retrofit.create(DataPimpinan::class.java).getPetaDataWilayah(kabupaten)
            val totalLahan = res.dataLahan?.sumOf { it?.lahanfix?.size ?: 0 }
            checkBoxAll = CheckBox(this).apply {
                text = "SEMUA ($totalLahan)"
                isChecked = true
            }
            cbKabupaten.addView(checkBoxAll)

            checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
                checkBoxMap.values.forEach {
                    it.setOnCheckedChangeListener(null)
                    it.isChecked = isChecked
                }
                updateMapMarkers()
                checkBoxMap.values.forEach { cb ->
                    cb.setOnCheckedChangeListener { _, _ -> handleKabupatenCheckboxChange() }
                }
            }

            res.dataLahan?.forEach { x ->
                val namaKab = x?.nama ?: return@forEach
                val jumlahLahan = x.lahanfix?.size ?: 0

                val cb = CheckBox(this).apply {
                    text = "$namaKab ($jumlahLahan)"
                    isChecked = true
                }
                cbKabupaten.addView(cb)
                checkBoxMap[namaKab] = cb
                cb.setOnCheckedChangeListener { _, _ -> handleKabupatenCheckboxChange() }

                x.lahanfix?.forEach { lahan ->
                    val lat = lahan?.latitude?.toDoubleOrNull()
                    val lng = lahan?.longitude?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        myItemList.add(UnifiedItem(LatLng(lat, lng), namaKab, "LAHAN", lahan = lahan))
                    }
                }
            }

            res.dataPersonil?.forEach { x ->
                x?.personil?.forEach { personil ->
                    val lat = personil?.tracking?.latitude?.toDoubleOrNull()
                    val lng = personil?.tracking?.longitude?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        myItemList.add(UnifiedItem(LatLng(lat, lng), personil.nama ?: "Personil", "PERSONIL", personil = personil))
                    }
                }
            }

            updateMapMarkers()
        } catch (e: Exception) {
            showErrorDialog("Terjadi kesalahan: ${e.message}")
        } finally {
            Loading.hide()
        }
    }

    private fun updateMapMarkers() {
        val kabChecked = checkBoxMap.filterValues { it.isChecked }.keys
        clusterManager.clearItems()
        clusterManager.addItems(myItemList.filter { it.type == "PERSONIL" || kabChecked.contains(it.title ?: "") })
        clusterManager.cluster()
    }

    private fun handleKabupatenCheckboxChange() {
        val allChecked = checkBoxMap.values.all { it.isChecked }
        checkBoxAll.setOnCheckedChangeListener(null)
        checkBoxAll.isChecked = allChecked
        checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
            checkBoxMap.values.forEach { it.setOnCheckedChangeListener(null); it.isChecked = isChecked }
            updateMapMarkers()
            checkBoxMap.values.forEach { cb ->
                cb.setOnCheckedChangeListener { _, _ -> handleKabupatenCheckboxChange() }
            }
        }
        updateMapMarkers()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = false

        clusterManager = ClusterManager(this, googleMap)
        clusterManager.renderer = UnifiedRenderer(this, googleMap, clusterManager, cachedMonoIcon, cachedMultiIcon, cachedPersonilIcon, cachedPersonilIconOffline)

        googleMap.setOnCameraIdleListener { clusterManager.onCameraIdle() }
        googleMap.setOnMarkerClickListener(clusterManager)

        clusterManager.setOnClusterItemClickListener { item ->
            if (item.type == "LAHAN") {
                lifecycleScope.launch {
                    getDataLahan(item.lahan?.kode!!)
                }
            } else {
                lifecycleScope.launch {
                    getDataPersonil(item.personil?.nrp!!)
                }
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.position, 20f))
            true
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-0.0578, 111.3101), 7f))
        checkLocationPermissionAndEnable()
    }

    private suspend fun getDataLahan(kode: String) {
        Loading.show(this)
        try{
            val res = Client.retrofit.create(DataPimpinan::class.java).getDetailLahan(kode)
            tvType.text = res.type
            tvPemilik.text = "${res.owner?.nama} - (${res.owner?.namaPok})"
            tvAlamat.text = "DESA ${res.desa} - KEC ${res.kecamatan} - KAB ${res.kabupaten} - PROV KALIMATAN BARAT"
            tvLuas.text = String.format("%.2f ha", (res.luas?.toFloat() ?: 0f) / 10000f)
            tvNamaPers.text = res.bpkp?.pers?.nama
            tvNrpPangkat.text = "${res.bpkp?.pers?.nrp} - ${res.bpkp?.pers?.pangkat}"
            tvSatker.text = "${res.bpkp?.pers?.jabatan} ${res.bpkp?.pers?.myPolres?.nama} POLDA ${res.bpkp?.pers?.myPolres?.provinsi}"
            tvBinaan.text = "DESA ${res.owner?.desa} - KEC ${res.owner?.kecamatan} - KAB ${res.owner?.kabupaten} - PROV KALIMANTAN BARAT"
            tvStatus.text = if(res.bpkp?.pers?.sockettoken?.token === null){
                "Offline"
            }else{
                "Online"
            }
            val sh = getSharedPreferences("session", MODE_PRIVATE)
            val jabatan = sh.getString("jabatan", "")
            lyVideoCall.setOnClickListener {
                lifecycleScope.launch {
                    callPersonil(jabatan!!, res.bpkp?.pers?.nohp!!, res.bpkp?.pers?.nama!!, res.bpkp?.pers?.pangkat!!)
                }
            }
            cvTopInfo.visibility = LinearLayout.VISIBLE
            cvBottomInfo.visibility = LinearLayout.VISIBLE
            Loading.hide()
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Terjadi kesalahan: ${e.message}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private suspend fun getDataPersonil(nrp: String) {
        Loading.show(this)
        try {
            val res = Client.retrofit.create(DataPimpinan::class.java).getDetailPers(nrp)
            tvNamaPers.text = res.nama
            tvNrpPangkat.text = "${res.nrp} - ${res.pangkat}"
            tvSatker.text = "${res.jabatan} ${res.satker?.polsek} ${res.satker?.nama} POLDA ${res.satker?.provinsi}"
            tvBinaan.text = "DESA ${res.binaan?.desa} - KEC ${res.binaan?.kec} - KAB ${res.binaan?.kab} - PROV ${res.binaan?.prov}"
            tvStatus.text = if(res.sockettoken?.token === null){
                "Offline"
            }else{
                "Online"
            }
            cvBottomInfo.visibility = LinearLayout.VISIBLE
            val sh = getSharedPreferences("session", MODE_PRIVATE)
            val jabatan = sh.getString("jabatan", "")
            lyVideoCall.setOnClickListener {
                lifecycleScope.launch {
                    callPersonil(jabatan!!, res.nohp!!, res.nama!!, res.pangkat!!)
                }
            }
            Loading.hide()
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Terjadi kesalahan: ${e.message}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private suspend fun callPersonil(jabatan: String, nrp: String, nama: String, pangkat: String) {
        Loading.show(this)
        try {
            val res = Client.retrofit.create(DataPimpinan::class.java).callPersonil(DataPimpinan.CallPimpinan(nrp, jabatan, "BPKP"))
            if(res.token === null){
                Loading.hide()
                AlertDialog.Builder(this)
                    .setTitle("Informasi")
                    .setMessage("Token Tidak Ditemukan")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            }else{
                Loading.hide()
                val i = Intent(this, PimpinanVideoCall::class.java)
                i.putExtra("token", res.token)
                i.putExtra("channel", nrp)
                i.putExtra("nama", nama)
                i.putExtra("pangkat", pangkat)
                startActivity(i)
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Terjadi kesalahan: ${e.message}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        }
    }

    private fun resizeMapIcons(context: Context, iconRes: Int, width: Int, height: Int): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(context.resources, iconRes)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    private fun checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && isLocationEnabled()) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun enableMyLocation() {
        val helper = LocationHelperOld(this, this) { location ->
            if (location != null) {
                try {
                    googleMap.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
        helper.requestLocation()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Ditolak")
            .setMessage("Aplikasi memerlukan izin lokasi untuk menampilkan lokasi Anda.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Informasi")
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    override fun onResume() { super.onResume(); mapLahanPimpinan.onResume() }
    override fun onPause() { mapLahanPimpinan.onPause(); super.onPause() }
    override fun onDestroy() { mapLahanPimpinan.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapLahanPimpinan.onLowMemory() }
}
