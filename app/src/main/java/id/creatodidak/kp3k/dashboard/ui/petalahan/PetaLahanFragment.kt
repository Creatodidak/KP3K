package id.creatodidak.kp3k.dashboard.ui.petalahan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentPetaLahanBinding
import id.creatodidak.kp3k.helper.LocationHelperOld
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.RMyLahanTugasItem
import id.creatodidak.kp3k.helper.formatDuaDesimalKoma
import kotlinx.coroutines.launch
import java.lang.Float.parseFloat

class PetaLahanFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPetaLahanBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var sh : SharedPreferences
    private var desaid = ""
    private lateinit var monoIcon: BitmapDescriptor
    private lateinit var multiIcon: BitmapDescriptor

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
        sh = requireActivity().getSharedPreferences("session", Context.MODE_PRIVATE)
        desaid = sh.getString("desaid", "").toString()
        monoIcon = resizeMapIcons(requireContext(), R.drawable.markerlahan, 100, 100)
        multiIcon = resizeMapIcons(requireContext(), R.drawable.markermulti, 100, 100)

        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val centerLatLng = LatLng(0.376158061166571, 109.94163383149947)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 7f))
        checkLocationPermissionAndEnable()
        lifecycleScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        try {
            val res = Client.retrofit.create(Data::class.java).getMyLahanTUgas(desaid)
            res.forEach { lahan ->
                try {
                    val lat = lahan.latitude?.toDouble()
                    val lng = lahan.longitude?.toDouble()
                    val position = LatLng(lat!!, lng!!)

                    val icon = when (lahan.type) {
                        "MONOKULTUR" -> monoIcon
                        "TUMPANGSARI" -> multiIcon
                        else -> monoIcon
                    }

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("LAHAN ${lahan.type} MILIK ${lahan.owner} (${lahan.pok})" ?: "LAHAN")
                            .snippet("Luas: ${lahan.luas} m² (${formatDuaDesimalKoma((lahan.luas?.toDoubleOrNull() ?: 0.0) / 10000)}Ha)")
                            .icon(icon)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            AlertDialog.Builder(requireContext())
                .setTitle("Terjadi Kesalahan")
                .setMessage("Terjadi kesalahan saat mengambil data lahan tugas.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    findNavController().popBackStack()
                }
                .show()
            e.printStackTrace()
        }
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

    private fun resizeMapIcons(context: Context, iconRes: Int, width: Int, height: Int): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(context.resources, iconRes)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }
}
