package id.creatodidak.kp3k.newversion.Maps

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.resizeMapIcon

class ShowSingleDataOnMap : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var komoditas: String = ""
    private var judul: String = ""


    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_single_data_on_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            insets
        }
        latitude = intent.getStringExtra("latitude")?.toDouble() ?: 0.0
        longitude = intent.getStringExtra("longitude")?.toDouble() ?: 0.0
        komoditas = intent.getStringExtra("komoditas").toString()
        judul = intent.getStringExtra("judul").toString()
        mapView = findViewById(R.id.mapSingle)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val icon = when (komoditas.uppercase()) {
            "SINGKONGs" -> resizeMapIcon(this, R.drawable.markersingkong, 96, 96)
            "JAGUNGs" -> resizeMapIcon(this, R.drawable.markerjagung, 96, 96)
            "KEDELAIs" -> resizeMapIcon(this, R.drawable.markerkedelai, 96, 96)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
        if(latitude != 0.0 && longitude != 0.0){
            val lokasi = LatLng(latitude, longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(lokasi)
                    .title("LAHAN ${komoditas.toUpperCase()} || $judul")
                    .contentDescription("$judul || $komoditas")
                    .icon(icon)
            )

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 12f))
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }
}