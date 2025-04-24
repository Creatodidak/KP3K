package id.creatodidak.kp3k.dashboard.ui.petalahan

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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import id.creatodidak.kp3k.databinding.FragmentPetaLahanBinding
import id.creatodidak.kp3k.helper.LocationHelperOld
import com.google.maps.android.SphericalUtil

class PetaLahanFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPetaLahanBinding? = null
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
        _binding = FragmentPetaLahanBinding.inflate(inflater, container, false)
        binding.mapLahan.onCreate(savedInstanceState)
        binding.mapLahan.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        // Tambahkan Polygon
        val polygonPoints = listOf(
            LatLng(0.37594881657074647, 109.9414268398714),
            LatLng(0.37709409507209696, 109.94065436369564),
            LatLng(0.3777619506225703, 109.9402815366547),
            LatLng(0.37869265686650616, 109.94147780183353),
            LatLng(0.37686074794681174, 109.94258555413663)
        )

        val polygon = googleMap.addPolygon(
            PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(0xFF00FF00.toInt())
                .fillColor(0x3300FF00)
                .clickable(true) // <- ini penting!
        )

// Simpan ID untuk identifikasi (opsional jika hanya 1 polygon)
        polygon.tag = "POLRES_LANDAK"

// Listener ketika polygon diklik
        googleMap.setOnPolygonClickListener { clickedPolygon ->
            if (clickedPolygon.tag == "POLRES_LANDAK") {
                // Calculate the area of the polygon in square meters
                val area = SphericalUtil.computeArea(clickedPolygon.points)

                // Display the area
                val areaInHectares = area / 10000 // Convert from square meters to hectares

                // Show marker with area information
                val center = getPolygonCenterPoint(clickedPolygon.points)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(center)
                        .title("LAHAN POLRES LANDAK: ${"%.2f".format(areaInHectares)} ha")
                )?.showInfoWindow()
            }
        }


//        // Tambahkan Marker
//        val markerLatLng = LatLng(0.37694666541744465, 109.94114852610464)
//        googleMap.addMarker(MarkerOptions().position(markerLatLng).title("Marker Lokasi"))

        // Pusatkan Kamera
        val centerLatLng = LatLng(0.376158061166571, 109.94163383149947)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 17f))

        checkLocationPermissionAndEnable()
    }

    private fun checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

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
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

    private fun getPolygonCenterPoint(points: List<LatLng>): LatLng {
        var lat = 0.0
        var lng = 0.0
        for (point in points) {
            lat += point.latitude
            lng += point.longitude
        }
        val totalPoints = points.size
        return LatLng(lat / totalPoints, lng / totalPoints)
    }


    override fun onResume() {
        super.onResume()
        binding.mapLahan.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapLahan.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapLahan.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapLahan.onLowMemory()
    }
}
