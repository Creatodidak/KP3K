package id.creatodidak.kp3k.dashboard

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.SetPin
import id.creatodidak.kp3k.Verifikasi
import id.creatodidak.kp3k.Welcome
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.Update
import id.creatodidak.kp3k.api.model.MUpdate
import id.creatodidak.kp3k.databinding.ActivityDashboardOpsionalBinding
import id.creatodidak.kp3k.helper.LocationHelperOld
import id.creatodidak.kp3k.network.SocketManager
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import java.io.File

class DashboardOpsional : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardOpsionalBinding
    private lateinit var locationHelper: LocationHelperOld
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                locationHelper.requestLocation()  // Mulai pembaruan lokasi saat izin diberikan
            } else {
                Toast.makeText(this@DashboardOpsional, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardOpsionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDashboardOpsional.toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "INCOMING_CALL_CHANNEL"
            val channelName = "Incoming Call"
            val channelDescription = "Channel for incoming calls"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard_opsional)
        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
        val nrp = sharedPreferences.getString("nrp", "")
        val socket = SocketManager.getSocket()

        val data = JSONObject().apply {
            put("nrp", nrp)
        }

        if (!sharedPreferences.getBoolean("isCurrentlyLogedIn", false)) {
            val i = Intent(this@DashboardOpsional, SetPin::class.java)
            startActivity(i)
            finish()
        }

        val hasAskedPermission = sharedPreferences.getBoolean("asked_audio_permission", false)

        if (!hasAskedPermission) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1001)
            } else {
                sharedPreferences.edit().putBoolean("asked_audio_permission", true).apply()
            }
        }

        if (socket.connected()) {
            socket.emit("bpkp-join", data)
        } else {
            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("bpkp-join", data)
            }
        }
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_lahantugas, R.id.nav_laporansaya, R.id.nav_koordinat_finder, R.id.nav_pemiliklahan_ok, R.id.nav_ai
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                else -> {
                    val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    handled
                }
            }
        }

        val headerView = navView.getHeaderView(0)
        val versionText = headerView.findViewById<TextView>(R.id.version_text)
        val versionName = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager
                    .getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                    .versionName
            } else {
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }

        versionText.setText("Versi "+versionName)

        if(sharedPreferences.getString("desaid", "").isNullOrEmpty()){
            AlertDialog.Builder(this@DashboardOpsional)
                .setTitle("Informasi")
                .setMessage("Data session kurang lengkap, silahkan login kembali!")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    logoutUser()
                }
                .show()
        }else{
            if(sharedPreferences.getString("status", "").isNullOrEmpty() || sharedPreferences.getString("status", "").equals("UNVERIFIED")){
                val i = Intent(this@DashboardOpsional, Verifikasi::class.java)
                startActivity(i)
            }else{
                lifecycleScope.launch {
                    checkUpdate()
                }
            }
        }

        locationHelper = LocationHelperOld(
            this,
            lifecycleOwner = this
        ) { location ->
            if (location != null) {
                lifecycleScope.launch {
                    sendMyLocation(location.latitude, location.longitude, sharedPreferences.getString("nrp", ""))
                }

            } else {
                Toast.makeText(this@DashboardOpsional, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        checkPermissionsAndStartRequest()

    }

    private suspend fun sendMyLocation(latitude: Double, longitude: Double, nrp: String?){
        try{
            val res = Client.retrofit.create(Data::class.java).sendLocation(
                Data.LocationRequest(
                    nrp!!,
                    latitude.toString(),
                    longitude.toString()
                )
            )

            if(!res.isSuccessful){
                Log.i("Tracking", res.raw().toString())
            }
        }catch (e: Exception){
            Log.e("Error", e.message.toString())
        }
    }

    private suspend fun checkUpdate() {
        try {
            val response = Client.retrofit
                .create(Update::class.java)
                .getLatestUpdate()

            Log.i("Last Version", response.name.toString())
            val currentVersionCode = BuildConfig.VERSION_CODE
            val serverVersionCode = response.code ?: 0

            if (serverVersionCode > currentVersionCode) {
                // Versi terbaru tersedia
                showUpdateDialog(response)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateDialog(update: MUpdate) {
        AlertDialog.Builder(this@DashboardOpsional)
            .setTitle("Versi Terbaru Tersedia (${update.name})")
            .setMessage("Fitur Baru:\n${update.log}" ?: "Perbarui aplikasi Anda untuk fitur terbaru.")
            .setPositiveButton("Update") { _, _ ->
                downloadApkWithPermission(update) // Memanggil fungsi yang memeriksa izin
            }
            .setNegativeButton("Nanti") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.REQUEST_INSTALL_PACKAGES)
    fun downloadApkWithPermission(update: MUpdate) {
        val filename = update.filename ?: return
        val url = "${BuildConfig.BASE_URL}android/download/${update.id}"
        Log.i("Download URL", url)
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("Mengunduh ${update.name}")
        request.setDescription("Mengunduh pembaruan aplikasi...")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setMimeType("application/vnd.android.package-archive")
        request.setAllowedOverMetered(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        AlertDialog.Builder(this@DashboardOpsional)
            .setTitle("Informansi")
            .setMessage("Proses download sedang berlangsung, silahkan buka Folder Download untuk menginstall pembaruan")
            .setPositiveButton("OK") { dialog, _ ->
               dialog.dismiss()
            }
            .show()
    }


    @OnPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.REQUEST_INSTALL_PACKAGES)
    fun onPermissionDenied() {
        Toast.makeText(this, "Permission denied. Cannot download the update.", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val sharedPref = getSharedPreferences("session", MODE_PRIVATE)

            // Tandai bahwa izin telah diminta
            sharedPref.edit().putBoolean("asked_audio_permission", true).apply()

            if (granted) {
                // Izin diberikan, lakukan aksi yang diperlukan
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                // Izin ditolak, beri feedback ke pengguna
                Toast.makeText(this, "Permission denied! Audio call may not work.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun registerReceiverForInstall(context: Context, filename: String) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    filename
                )
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    file
                )

                val install = Intent(Intent.ACTION_VIEW)
                install.setDataAndType(fileUri, "application/vnd.android.package-archive")
                install.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(install)
            }
        }

        // Daftarkan receiver untuk menerima pemberitahuan selesai unduhan
        ContextCompat.registerReceiver(
            context,
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    private fun logoutUser() {
        val sh = getSharedPreferences("session", MODE_PRIVATE)
        sh.edit() { clear() }
        val intent = Intent(this, Welcome::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_opsional, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard_opsional)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            this@DashboardOpsional.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissionsAndStartRequest() {
        if (ContextCompat.checkSelfPermission(
                this@DashboardOpsional,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Periksa jika lokasi aktif
            if (isLocationEnabled()) {
                locationHelper.requestLocation() // Meminta lokasi terbaik
            } else {
                Toast.makeText(this@DashboardOpsional, "Aktifkan lokasi terlebih dahulu", Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.disconnect()
        val sh = getSharedPreferences("session", MODE_PRIVATE)
        with(sh.edit()){
            putBoolean("isCurrentlyLogedIn", false)
            apply()
        }
    }
}
