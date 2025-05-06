package id.creatodidak.kp3k.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.creatodidak.kp3k.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var captureButton: ImageButton
    private lateinit var locationHelper: LocationHelperOld
    private lateinit var preview: Preview
    private lateinit var outputFile: File
    private lateinit var camera: androidx.camera.core.Camera
    private lateinit var cameraSelectorButton: ImageButton

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                locationHelper.requestLocation()
            } else {
                Toast.makeText(this@CameraActivity, "Izin lokasi diperlukan", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun setFocusPoint(x: Float, y: Float) {
        val factory = viewFinder.meteringPointFactory
        val point: MeteringPoint = factory.createPoint(x, y)

        val action = FocusMeteringAction.Builder(point).build()

        camera.cameraControl.startFocusAndMetering(action).addListener({
        }, ContextCompat.getMainExecutor(this))
    }

    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        viewFinder.setOnTouchListener { _, event ->
            val x = event.x
            val y = event.y
            setFocusPoint(x, y)
            true
        }

        captureButton = findViewById(R.id.captureButton)
        cameraSelectorButton = findViewById<ImageButton>(R.id.cameraSwitcher)
        cameraSelectorButton.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            Toast.makeText(
                this,
                "Beralih ke kamera ${if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "belakang" else "depan"}",
                Toast.LENGTH_SHORT
            ).show()
            startCamera()
        }

        locationHelper = LocationHelperOld(
            this@CameraActivity,
            lifecycleOwner = this
        ) { location ->
            val tvKoordinat = findViewById<TextView>(R.id.tvKoordinatCamera)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                tvKoordinat.text = "$latitude, $longitude"
            } else {
                Toast.makeText(this@CameraActivity, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        checkPermissionsAndStartRequest()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        captureButton.setOnClickListener {
            Loading.show(this@CameraActivity)
            takePhoto()
//            captureAndSaveWithWatermark()
        }

        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller?.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val tvNrp = findViewById<TextView>(R.id.tvNrpCamera)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggalCamera)

        val sh = getSharedPreferences("session", MODE_PRIVATE)
        val nrp = sh.getString("nrp", "")
        tvNrp.text = nrp
        tvTanggal.text = TimeUtils.getFormattedDateTime()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                .build()

            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setResolutionSelector(resolutionSelector)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal binding kamera: ${e.message}", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        outputFile = File(
            externalCacheDir,
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val previewBitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                        ?: run {
                            Toast.makeText(
                                this@CameraActivity,
                                "Gagal decode foto",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                    val watermarkView = findViewById<ConstraintLayout>(R.id.watermarks)
                        ?: run {
                            Toast.makeText(
                                this@CameraActivity,
                                "View watermark tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(this@CameraActivity)
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()

                    captureAndSaveWithWatermark(
                        previewBitmap,
                        watermarkView,
                        this@CameraActivity
                    ) { watermarkedFile ->
                        // **Hanya** tampilkan preview. Jangan finish di sini!
                        showImagePreviewDialog(watermarkedFile)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Loading.hide()
                    Toast.makeText(
                        this@CameraActivity,
                        "Gagal ambil foto",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error ambil foto", exc)
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun checkPermissionsAndStartRequest() {
        if (ContextCompat.checkSelfPermission(
                this@CameraActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                locationHelper.requestLocation()
            } else {
                Toast.makeText(
                    this@CameraActivity,
                    "Aktifkan lokasi terlebih dahulu",
                    Toast.LENGTH_LONG
                ).show()
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
            this@CameraActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun captureAndSaveWithWatermark(
        previewBitmap: Bitmap,
        watermarkView: View,
        context: Context,
        onSaved: (File) -> Unit
    ) {
        // 1. Render watermarkView jadi bitmap kecil (seperti ukuran view)
        val smallWatermark = getBitmapFromView(watermarkView)

        // 2. Skala watermark ke ukuran penuh previewBitmap
        val scaledWatermark = smallWatermark.scale(previewBitmap.width, previewBitmap.height)

        // 3. Buat bitmap hasil dengan ukuran penuh preview
        val resultBitmap = createBitmap(
            previewBitmap.width,
            previewBitmap.height,
            previewBitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)

        // 4. Gambar foto kamera
        canvas.drawBitmap(previewBitmap, 0f, 0f, null)

        // 5. Gambar watermark yang sudah diskalakan
        canvas.drawBitmap(scaledWatermark, 0f, 0f, null)

        // 6. Simpan dan kembalikan file
        val savedFile = saveBitmapToFile(resultBitmap, context)
        onSaved(savedFile)
    }



    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        val filename = "IMG_WM_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    private fun showImagePreviewDialog(watermarkedFile: File) {

        Loading.hide()

        // Tampilkan konfirmasi custom
        KonfirmasiFoto.show(
            context = this@CameraActivity,
            absolutePath = watermarkedFile.absolutePath,
            onGunakan = {
                val result = Intent().apply {
                    putExtra("imagePath", watermarkedFile.absolutePath)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            },
            onUlangi = {
                startCamera()
            }
        )
    }



}
