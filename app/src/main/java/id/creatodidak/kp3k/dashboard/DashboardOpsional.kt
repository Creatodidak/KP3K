package id.creatodidak.kp3k.dashboard

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.SetPin
import id.creatodidak.kp3k.Welcome
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Update
import id.creatodidak.kp3k.api.model.MUpdate
import id.creatodidak.kp3k.databinding.ActivityDashboardOpsionalBinding
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.offline.AppDatabase
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import java.io.File
import androidx.core.content.edit

class DashboardOpsional : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardOpsionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardOpsionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDashboardOpsional.toolbar)

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

        if (socket.connected()) {
            socket.emit("bpkp-join", data)
        } else {
            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("bpkp-join", data)
            }
        }
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_lahantugas, R.id.nav_laporansaya, R.id.nav_koordinat_finder, R.id.nav_pemiliklahan_ok
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

        if(sharedPreferences.getString("desabinaan", "").isNullOrEmpty()){
            AlertDialog.Builder(this@DashboardOpsional)
                .setTitle("Informasi")
                .setMessage("Data session kurang lengkap, silahkan login kembali!")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    logoutUser()
                }
                .show()
        }else{
            lifecycleScope.launch {
                checkUpdate()
            }
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
