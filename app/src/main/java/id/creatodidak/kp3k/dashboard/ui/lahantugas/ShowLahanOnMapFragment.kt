package id.creatodidak.kp3k.dashboard.ui.lahantugas

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import id.creatodidak.kp3k.databinding.FragmentShowLahanOnMapBinding
import id.creatodidak.kp3k.helper.LocationHelperOld
import java.lang.Double

class ShowLahanOnMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentShowLahanOnMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowLahanOnMapBinding.inflate(inflater, container, false)
        binding.mapShowLahan.onCreate(savedInstanceState)
        binding.mapShowLahan.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val args: ShowLahanOnMapFragmentArgs by navArgs()
        val lats = Double.valueOf(args.koordinat.split("||")[0])
        val longs = Double.valueOf(args.koordinat.split("||")[1])
        // Tambahkan Marker
        val markerLatLng = LatLng(lats, longs)
        googleMap.addMarker(MarkerOptions().position(markerLatLng).title("LOKASI LAHAN"))

        // Pusatkan Kamera
        val centerLatLng = LatLng(lats, longs)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 18f))

        checkLocationPermissionAndEnable()
    }

    private fun checkLocationPermissionAndEnable() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                enableMyLocation()
            } else {
                showLocationSettingsDialog()
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocation() {
        val locationHelper = LocationHelperOld(requireContext(), viewLifecycleOwner) { location ->
            if (location != null) {
                try {
                    googleMap.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
        locationHelper.requestLocation()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Layanan Lokasi Nonaktif")
            .setMessage("Aktifkan layanan lokasi untuk menampilkan lokasi Anda.")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Izin Ditolak")
            .setMessage("Aplikasi memerlukan izin lokasi untuk menampilkan lokasi Anda.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        binding.mapShowLahan.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapShowLahan.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapShowLahan.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapShowLahan.onLowMemory()
    }
}
