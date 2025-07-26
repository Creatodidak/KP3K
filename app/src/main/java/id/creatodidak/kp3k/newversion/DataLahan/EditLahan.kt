package id.creatodidak.kp3k.newversion.DataLahan

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.RequestClass.LahanAddRequest
import id.creatodidak.kp3k.api.RequestClass.LahanPatchRequest
import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Dao.LahanDao
import id.creatodidak.kp3k.database.Dao.OwnerDao
import id.creatodidak.kp3k.database.Dao.TanamanDao
import id.creatodidak.kp3k.database.Dao.WilayahDao
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanDraftEntity
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.getMyLevel
import id.creatodidak.kp3k.helper.getMyNrp
import id.creatodidak.kp3k.helper.getMyRole
import id.creatodidak.kp3k.helper.isOnline
import id.creatodidak.kp3k.helper.parseIsoDate
import id.creatodidak.kp3k.helper.showError
import id.creatodidak.kp3k.helper.showSuccess
import kotlinx.coroutines.launch
import java.util.Date

class EditLahan : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var db: AppDatabase
    private lateinit var dbLahan: LahanDao
    private lateinit var dbTanaman: TanamanDao
    private lateinit var dbOwner: OwnerDao
    private lateinit var dbWilayah: WilayahDao
    private lateinit var sh: SharedPreferences
    private lateinit var komoditas: String
    private var kategori: String? = null
    private lateinit var id: String
    private lateinit var tvKeteranganKomoditas: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var spOwner: Spinner
    lateinit var spTypeLahan: Spinner
    lateinit var etLuas: TextInputEditText
    lateinit var mapViewValidasi: MapView
    private lateinit var googleMap: GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    lateinit var etLatitude: TextInputEditText
    lateinit var etLongitude: TextInputEditText
    lateinit var btnValidasiKoordinat: Button
    lateinit var btnKirimData: Button
    lateinit var lyForm: LinearLayout
    lateinit var btnUseLocation: Button
    lateinit var tvConvertHektar: TextView

    private val listOwner = mutableListOf<OwnerEntity>()
    private val listType = listOf("PILIH TYPE LAHAN", "MONOKULTUR", "TUMPANGSARI", "PBPH", "PERHUTANANSOSIAL")
    private lateinit var ownerAdapter: ArrayAdapter<OwnerEntity>
    private lateinit var typeAdapter: ArrayAdapter<String>
    private var isKoordinatValid: Boolean = false

    private var selectedOwnerId: Int = 0
    private var existStatus: String = ""
    private var myLocationNow: LatLng? = null

    private var edited: LahanEntity? = null

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_lahan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.default_bg)
        sh = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        db = DatabaseInstance.getDatabase(this)
        dbLahan = db.lahanDao()
        dbTanaman = db.tanamanDao()
        dbOwner = db.ownerDao()
        dbWilayah = db.wilayahDao()
        komoditas = intent.getStringExtra("komoditas").toString()
        kategori = intent.getStringExtra("kategori")
        id = intent.getStringExtra("id").toString()
        tvKeteranganKomoditas = findViewById(R.id.tvKeteranganKomoditas)
        tvKeteranganKomoditas.text = "pada Komoditas ${komoditas.capitalize()}"
        spOwner = findViewById(R.id.spOwner)
        spTypeLahan = findViewById(R.id.spTypeLahan)
        etLuas = findViewById(R.id.etLuas)
        mapViewValidasi = findViewById(R.id.mapViewValidasi)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        btnValidasiKoordinat = findViewById(R.id.btnValidasiKoordinat)
        btnKirimData = findViewById(R.id.btnKirimData)
        btnUseLocation = findViewById(R.id.btnUseLocation)
        lyForm = findViewById(R.id.lyForm)
        tvConvertHektar = findViewById(R.id.tvConvertHektar)

        ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOwner)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spOwner.adapter = ownerAdapter

        typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listType)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTypeLahan.adapter = typeAdapter


        lifecycleScope.launch {
            loadOwner()
        }

        if (isKoordinatValid) {
            btnKirimData.visibility = View.VISIBLE
        } else {
            btnKirimData.visibility = View.GONE
        }

        spOwner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedOwnerId = listOwner[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedOwnerId = 0
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnUseLocation.setOnClickListener {
            if (myLocationNow != null) {
                etLatitude.setText(myLocationNow!!.latitude.toString())
                etLongitude.setText(myLocationNow!!.longitude.toString())
            } else {
                btnUseLocation.visibility = View.GONE
            }
        }

        btnValidasiKoordinat.setOnClickListener {
            val lat = etLatitude.text.toString().toDoubleOrNull()
            val lng = etLongitude.text.toString().toDoubleOrNull()

            if (lat != null && lng != null) {
                val target = LatLng(lat, lng)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 17f))
                isKoordinatValid = true
                btnKirimData.visibility = View.VISIBLE

                // Tutup keyboard dan clear focus
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                currentFocus?.let { view ->
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            } else {
                showError(this, "Error", "Koordinat tidak valid")
            }
        }

        etLuas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val luashektar =
                    angkaIndonesia(convertToHektar(etLuas.text.toString().toDoubleOrNull() ?: 0.0))
                tvConvertHektar.text =
                    "= $luashektar Ha (Pembulatan Desimal 2 Angka Dibelakang Koma)"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etLatitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isKoordinatValid = false
                btnKirimData.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etLongitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isKoordinatValid = false
                btnKirimData.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapViewValidasi.onCreate(mapViewBundle)
        mapViewValidasi.getMapAsync(this)

        btnKirimData.setOnClickListener {
            if (isValid()) {
                if(existStatus == "REJECTED"){
                    if (isOnline(this)){
                        lifecycleScope.launch {
                            saveToLocal()
                        }
                    }else{
                        showError(this, "Error", "Perbaikan Lahan Ditolak hanya dapat dilakukan jika koneksi internet tersedia!")
                    }
                }else{
                    AlertDialog.Builder(this)
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah Anda yakin ingin menyimpan data lahan ini?")
                        .setPositiveButton("Ya") { _, _ ->
                            lifecycleScope.launch {
                                saveToLocal()
                            }
                        }
                        .setIcon(R.drawable.query_what_how_why_icon)
                        .setNegativeButton("Tidak", null)
                        .show()
                }
            } else {
                showError(this, "Error", "Mohon lengkapi data terlebih dahulu")
            }
        }
    }

    private suspend fun loadOwner(){
        listOwner.clear()
        val owners = mutableListOf<OwnerEntity>()
        owners.add(OwnerEntity(0, TypeOwner.PRIBADI, IsGapki.TIDAK, "", "PILIH PEMILIK LAHAN", "", "", "", 0, 0, 0, 0, "", "", Date(), Date(), "", ""))
        val role = RoleHelper(this)
        when (getMyLevel(this)){
            "provinsi" -> {
                owners.addAll(consumeOwner(dbOwner.getOwnerByProvinsi(komoditas, role.id)))
            }
            "kabupaten" -> {
                owners.addAll(consumeOwner(dbOwner.getOwnerByKabupaten(komoditas, role.id)))
            }
            "kecamatan" -> {
                owners.addAll(consumeOwner(dbOwner.getOwnerByKecamatans(komoditas, role.ids)))
            }
            "desa" -> {
                owners.addAll(consumeOwner(dbOwner.getOwnerByDesa(komoditas, role.id)))
            }
        }
        if(owners.isEmpty()){
            showError(this, "Error", "Tidak ada data Pemilik Lahan pada komoditas $komoditas, silahkan tambahkan data pemilik baru baru terlebih dahulu!"){
                finish()
            }
        }else {
            listOwner.addAll(owners)
            ownerAdapter.notifyDataSetChanged()
            loadExistData()
        }
    }

    private suspend fun loadExistData(){
        val data = dbLahan.getLahanById(id.toInt())
        Log.i("IDLAHAN", id)
        etLuas.setText(data.luas)
        etLatitude.setText(data.latitude)
        etLongitude.setText(data.longitude)
        spTypeLahan.setSelection(data.type.ordinal + 1)
        spOwner.setSelection(listOwner.indexOfFirst { it.id == data.owner_id })
        existStatus = data.status
        edited = data
        isKoordinatValid = true
        btnKirimData.visibility = View.VISIBLE
        btnKirimData.text = "UPDATE DATA"
    }

    private fun consumeOwner(listOwner: List<OwnerEntity>): List<OwnerEntity>{
        val newList = mutableListOf<OwnerEntity>()
        listOwner.forEach { it ->
            newList.add(
                OwnerEntity(
                    it.id,
                    it.type,
                    it.gapki,
                    it.nama_pok,
                    "${it.nama} - ${it.nama_pok}",
                    it.nik,
                    it.alamat,
                    it.telepon,
                    it.provinsi_id,
                    it.kabupaten_id,
                    it.kecamatan_id,
                    it.desa_id,
                    it.status,
                    it.alasan,
                    it.createAt,
                    it.updatedAt,
                    it.komoditas,
                    it.submitter
                )
            )
        }
        return newList
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        map.apply {
            uiSettings.apply {
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMapToolbarEnabled = false
                isMyLocationButtonEnabled = false
                isZoomControlsEnabled = true
                isCompassEnabled = true
            }
            try {
                isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e("MAPS", "Location permission not granted: ${e.message}")
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                        myLocationNow = userLatLng
                        btnUseLocation.visibility = View.VISIBLE
                        Log.d("MAPS", "User location: ${userLatLng.latitude}, ${userLatLng.longitude}")
                    } else {
                        Log.w("MAPS", "Location is null.")
                        showError(this@EditLahan, "Lokasi Tidak Ditemukan", "Pastikan GPS aktif & izin diberikan.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MAPS", "Failed to get location: ${e.message}")
                    showError(this@EditLahan, "Gagal Lokasi", "Tidak bisa mendapatkan lokasi.")
                }
        }

    }

    private fun isValid(): Boolean {
        if(etLuas.text.toString().isEmpty()){
            showError(this, "Error", "Luas tidak boleh kosong")
            return false
        }
        if(selectedOwnerId == 0){
            showError(this, "Error", "Owner tidak boleh kosong")
            return false
        }
        val cekowner = listOwner.find { it.id == selectedOwnerId }
        if(cekowner == null){
            showError(this, "Error", "Owner tidak ditemukan")
            return false
        }
        if(etLatitude.text.toString().isEmpty()){
            showError(this, "Error", "Latitude tidak boleh kosong")
            return false
        }
        if(etLongitude.text.toString().isEmpty()){
            showError(this, "Error", "Longitude tidak boleh kosong")
            return false
        }
        if(spTypeLahan.selectedItemPosition == 0){
            showError(this, "Error", "Type lahan tidak boleh kosong")
            return false
        }
        return true
    }

    private suspend fun saveToLocal(){
        Loading.show(this)
        try {
            val owner = listOwner.find { it.id == selectedOwnerId }
            val type = spTypeLahan.selectedItem.toString()
            val luas = etLuas.text.toString().toString()
            val lat = etLatitude.text.toString()
            val lng = etLongitude.text.toString()
            val lastId = db.draftLahanDao().getLastId()?:0
            val countLahanOwnerOnDraft = db.draftLahanDao().getDraftLahanByOwner(komoditas, selectedOwnerId)
            val countLahanOwner = db.lahanDao().getVerifiedLahanByOwner(komoditas, selectedOwnerId)
            val lahanke = if(existStatus === "REJECTED") (countLahanOwner.size + countLahanOwnerOnDraft.size + 1).toString() else edited?.lahanke!!
            val data = LahanDraftEntity(
                lastId + 1,
                id.toInt(),
                TypeLahan.valueOf(type),
                komoditas,
                selectedOwnerId,
                owner!!.provinsi_id,
                owner.kabupaten_id,
                owner.kecamatan_id,
                owner.desa_id,
                luas,
                lat,
                lng,
                "OFFLINEUPDATE",
                null,
                Date(),
                Date(),
                getMyNrp(this),
                lahanke
            )
            val insert = db.draftLahanDao().insert(data)
            if(insert > 0){
                saveToServer(data)
                if(existStatus == "REJECTED" && edited != null){
                    db.lahanDao().delete(edited!!)
                }
            }else{
                showError(this, "Error", "Gagal menyimpan data di local database, data tidak dilanjutkan!")
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", e.message.toString())
        }
    }

    private suspend fun saveToServer(data: LahanDraftEntity){
        try{
            val res = Client.retrofit.create(LahanEndpoint::class.java).updateLahan(
                id,
                LahanPatchRequest(
                    data.type.name,
                    data.komoditas,
                    data.owner_id,
                    data.provinsi_id,
                    data.kabupaten_id,
                    data.kecamatan_id,
                    data.desa_id,
                    data.luas,
                    data.latitude,
                    data.longitude,
                    "UNVERIFIED",
                    null,
                    getMyNrp(this),
                    getMyRole(this),
                    data.lahanke
                )
            )

            if(res.isSuccessful && res.body() != null && res.body()?.data != null){
                val newData = res.body()!!.data!!
                newData.let { it ->
                    val newInsert = LahanEntity(
                        it.id!!,
                        TypeLahan.valueOf(it.type!!),
                        it.komoditas!!,
                        it.ownerId!!,
                        it.provinsiId!!,
                        it.kabupatenId!!,
                        it.kecamatanId!!,
                        it.desaId!!,
                        it.luas!!,
                        it.latitude!!,
                        it.longitude!!,
                        it.status!!,
                        it.alasan,
                        it.createAt?.let { parseIsoDate(it) } ?: Date(),
                        it.updateAt?.let { parseIsoDate(it) } ?: Date(),
                        it.submitter!!,
                        it.lahanke!!
                    )

                    val resInsert = dbLahan.insert(newInsert)
                    if (resInsert > 0) {
                        db.draftLahanDao().delete(data)
                        showSuccess(this,"Berhasil",res.body()?.msg.toString()){
                            if(kategori.isNullOrEmpty()){
                                finish()
                            }else{
                                val i = Intent(this, ShowDataLahanByCategory::class.java)
                                i.putExtra("komoditas", komoditas)
                                i.putExtra("kategori", kategori)
                                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivity(i)
                            }
                        }
                    }else{
                        if(kategori.isNullOrEmpty()){
                            finish()
                        }else{
                            val i = Intent(this, ShowDataLahanByCategory::class.java)
                            i.putExtra("komoditas", komoditas)
                            i.putExtra("kategori", kategori)
                            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(i)
                        }
                    }
                }
            }else{
                val errorMsg = res.body()?.msg
                    ?: res.errorBody()?.string() ?: "Terjadi kesalahan tidak diketahui"

                showError(this, "Error", errorMsg){
                    if(kategori.isNullOrEmpty()){
                        finish()
                    }else{
                        val i = Intent(this, ShowDataLahanByCategory::class.java)
                        i.putExtra("komoditas", komoditas)
                        i.putExtra("kategori", kategori)
                        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(i)
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            showError(this, "Error", "Data gagal disimpan di server, namun berhasil disimpan di local database sebagai draft Offline!"){
                if(kategori.isNullOrEmpty()){
                    finish()
                }else{
                    val i = Intent(this, ShowDataLahanByCategory::class.java)
                    i.putExtra("komoditas", komoditas)
                    i.putExtra("kategori", kategori)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(i)
                }
            }
        }finally {
            Loading.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        mapViewValidasi.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapViewValidasi.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapViewValidasi.onStop()
    }

    override fun onPause() {
        mapViewValidasi.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapViewValidasi.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewValidasi.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapViewValidasi.onSaveInstanceState(mapViewBundle)
    }
}
