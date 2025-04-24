package id.creatodidak.kp3k.dashboard.ui.koordinat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import id.creatodidak.kp3k.databinding.FragmentKoordinatFinderBinding
import id.creatodidak.kp3k.helper.LocationHelperOld
import androidx.core.graphics.toColorInt

class KoordinatFinderFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentKoordinatFinderBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationHelper: LocationHelperOld
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                locationHelper.requestLocation()  // Mulai pembaruan lokasi saat izin diberikan
            } else {
                Toast.makeText(requireContext(), "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoordinatFinderBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Menyiapkan MapView
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationHelper = LocationHelperOld(
            requireContext(),
            viewLifecycleOwner
        ) { location ->
            if (location != null) {
                // Menampilkan Latitude, Longitude, dan Akurasi
                binding.tvLatitude.text = "${location.latitude}"
                binding.tvLongitude.text = "${location.longitude}"
                binding.tvAkurasi.text = "${location.accuracy} m"

                // Update warna akurasi
                if (location.accuracy <= 10) {
                    binding.tvAkurasi.setTextColor("#4CAF50".toColorInt()) // Hijau
                } else if (location.accuracy > 10 && location.accuracy <= 20) {
                    binding.tvAkurasi.setTextColor("#FFC107".toColorInt()) // Kuning
                } else {
                    binding.tvAkurasi.setTextColor("#FF0000".toColorInt()) // Merah
                }

                // Menambahkan marker di peta
                googleMap.clear() // Membersihkan peta
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Lokasi Anda"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f)) // Menyesuaikan zoom
            } else {
                Toast.makeText(requireContext(), "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                binding.lySalin.visibility = View.GONE
            }
        }

        // Memeriksa izin lokasi dan status lokasi aktif
        checkPermissionsAndStartRequest()

        // Salin ke clipboard
        fun salinKeClipboard(label: String, teks: String) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, teks)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "$label disalin!", Toast.LENGTH_SHORT).show()
        }

        binding.btSalinLatitude.setOnClickListener {
            val lat = binding.tvLatitude.text.toString()
            if (lat.isNotEmpty()) salinKeClipboard("Latitude", lat)
        }

        binding.btSalinLongitude.setOnClickListener {
            val lng = binding.tvLongitude.text.toString()
            if (lng.isNotEmpty()) salinKeClipboard("Longitude", lng)
        }

        binding.btSalinKoordinat.setOnClickListener {
            val lat = binding.tvLatitude.text.toString()
            val lng = binding.tvLongitude.text.toString()
            if (lat.isNotEmpty() && lng.isNotEmpty()) {
                salinKeClipboard("Koordinat", "$lat, $lng")
            }
        }

        return root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        // Set pengaturan peta jika diperlukan
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    // Memeriksa izin lokasi dan memulai pembaruan lokasi jika diizinkan
    private fun checkPermissionsAndStartRequest() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Periksa jika lokasi aktif
            if (isLocationEnabled()) {
                locationHelper.requestLocation() // Meminta lokasi terbaik
            } else {
                Toast.makeText(requireContext(), "Aktifkan lokasi terlebih dahulu", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        locationHelper.stop() // Berhenti dari pembaruan lokasi saat fragment dihancurkan
    }
}
