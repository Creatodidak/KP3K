package id.creatodidak.kp3k.dashboard.ui.panen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.databinding.FragmentPanenBinding
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.SumberFoto
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.getValue

class PanenFragment : Fragment() {
    private lateinit var _binding: FragmentPanenBinding
    private val binding get() = _binding!!
    private val args : PanenFragmentArgs by navArgs()
    private lateinit var cameraLauncher1: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher2: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher3: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher4: ActivityResultLauncher<Intent>
    private lateinit var galeriLauncher1: ActivityResultLauncher<String>
    private lateinit var galeriLauncher2: ActivityResultLauncher<String>
    private lateinit var galeriLauncher3: ActivityResultLauncher<String>
    private lateinit var galeriLauncher4: ActivityResultLauncher<String>
    private var isDok1camera = false
    private var isDok2camera = false
    private var isDok3camera = false
    private var isDok4camera = false
    private var pathDok1 = ""
    private var pathDok2 = ""
    private var pathDok3 = ""
    private var pathDok4 = ""
    private var isDok1Changed = false
    private var isDok2Changed = false
    private var isDok3Changed = false
    private var isDok4Changed = false
    private var oldJumlahPanen = ""
    private var oldKeterangan = ""
    private var id = ""
    val fileUrl = "${BuildConfig.BASE_URL}file/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanenBinding.inflate(inflater, container, false)
        val root : View = binding.root
        val tanamanid = args.tanamanId
        val urutan = args.urutan
        val pemilik= args.pemilik
        val tanggaltanam = args.tanggaltanam
        val kodelahan = args.kodelahan
        val masatanam = args.masatanam

        val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        val nrp = sh.getString("nrp", "")
        if(nrp != ""){
            binding.tvNrpCamera.text = nrp
            binding.tvNrpCamera1.text = nrp
            binding.tvNrpCamera2.text = nrp
            binding.tvNrpCamera3.text = nrp
        }
        cameraLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imagePath = result.data?.getStringExtra("imagePath")
                Glide.with(requireContext())
                    .load(imagePath)
                    .into(binding.imageDok1)
                isDok1camera = true
                pathDok1 = imagePath ?: ""
                binding.errorDok1.visibility = View.GONE
                binding.watermarks1.visibility = View.GONE
                binding.imageDok1.visibility = View.VISIBLE
                binding.buttonPilihDok1.text = "UBAH FOTO"
                isDok1Changed = true
            }
        }
        cameraLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imagePath = result.data?.getStringExtra("imagePath")
                Glide.with(requireContext())
                    .load(imagePath)
                    .into(binding.imageDok2)
                binding.errorDok2.visibility = View.GONE
                isDok2camera = true
                pathDok2 = imagePath ?: ""
                binding.watermarks2.visibility = View.GONE
                binding.imageDok2.visibility = View.VISIBLE
                binding.buttonPilihDok2.text = "UBAH FOTO"
                isDok2Changed = true
            }
        }
        cameraLauncher3 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imagePath = result.data?.getStringExtra("imagePath")
                Glide.with(requireContext())
                    .load(imagePath)
                    .into(binding.imageDok3)
                binding.errorDok3.visibility = View.GONE
                isDok3camera = true
                pathDok3 = imagePath ?: ""
                binding.watermarks3.visibility = View.GONE
                binding.imageDok3.visibility = View.VISIBLE
                binding.buttonPilihDok3.text = "UBAH FOTO"
                isDok3Changed = true
            }
        }
        cameraLauncher4 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imagePath = result.data?.getStringExtra("imagePath")
                Glide.with(requireContext())
                    .load(imagePath)
                    .into(binding.imageDok4)
                binding.errorDok4.visibility = View.GONE
                isDok4camera = true
                pathDok4 = imagePath ?: ""
                binding.watermarks4.visibility = View.GONE
                binding.imageDok4.visibility = View.VISIBLE
                binding.buttonPilihDok4.text = "UBAH FOTO"
                isDok4Changed = true
            }
        }
        galeriLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Glide.with(requireContext())
                    .load(it)
                    .into(binding.imageDok1)
                isDok1camera = false
                binding.errorDok1.visibility = View.GONE
                binding.watermarks1.visibility = View.VISIBLE
                binding.imageDok1.visibility = View.VISIBLE
                binding.buttonPilihDok1.text = "UBAH FOTO"
                isDok1Changed = true
            }
        }
        galeriLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Glide.with(requireContext()).load(it).into(binding.imageDok2)
                isDok2camera = false
                binding.errorDok2.visibility = View.GONE
                binding.watermarks2.visibility = View.VISIBLE
                binding.imageDok2.visibility = View.VISIBLE
                binding.buttonPilihDok2.text = "UBAH FOTO"
                isDok2Changed = true
            }
        }
        galeriLauncher3 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Glide.with(requireContext()).load(it).into(binding.imageDok3)
                isDok3camera = false
                binding.errorDok3.visibility = View.GONE
                binding.watermarks3.visibility = View.VISIBLE
                binding.imageDok3.visibility = View.VISIBLE
                binding.buttonPilihDok3.text = "UBAH FOTO"
                isDok3Changed = true
            }
        }
        galeriLauncher4 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Glide.with(requireContext()).load(it).into(binding.imageDok4)
                isDok4camera = false
                binding.errorDok4.visibility = View.GONE
                binding.watermarks4.visibility = View.VISIBLE
                binding.imageDok4.visibility = View.VISIBLE
                binding.buttonPilihDok4.text = "UBAH FOTO"
                isDok4Changed = true
            }
        }

        binding.buttonPilihDok1.setOnClickListener {
            SumberFoto.show(requireContext(),
                onGaleri = {
                    galeriLauncher1.launch("image/*")
                },
                onKamera = {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    cameraLauncher1.launch(intent)
                })
        }
        binding.buttonPilihDok2.setOnClickListener {
            SumberFoto.show(requireContext(),
                onGaleri = {
                    galeriLauncher2.launch("image/*")
                },
                onKamera = {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    cameraLauncher2.launch(intent)
                })
        }
        binding.buttonPilihDok3.setOnClickListener {
            SumberFoto.show(requireContext(),
                onGaleri = {
                    galeriLauncher3.launch("image/*")
                },
                onKamera = {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    cameraLauncher3.launch(intent)
                })
        }
        binding.buttonPilihDok4.setOnClickListener {
            SumberFoto.show(requireContext(),
                onGaleri = {
                    galeriLauncher4.launch("image/*")
                },
                onKamera = {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    cameraLauncher4.launch(intent)
                })
        }

        binding.tvTajuk.text = "DATA PANEN PENANAMAN KE $urutan MASA TANAM $masatanam DI LAHAN $pemilik"

        lifecycleScope.launch {
            checkDataPanen(tanamanid)
        }

        binding.btnKirimDataPanen.setOnClickListener {
            binding.btnKirimDataPanen.isEnabled = false
            if(isValid()){
                processData()
            }else{
                binding.btnKirimDataPanen.isEnabled = true
            }
        }

        binding.btnKirimDataPanenUpdate.setOnClickListener {
            binding.btnKirimDataPanenUpdate.isEnabled = false
            if(isValid()){
                processData2()
            }else{
                binding.btnKirimDataPanenUpdate.isEnabled = true
            }
        }
        return root
    }

    private suspend fun checkDataPanen(tanamanid : String) {
        try {
            val response = Client.retrofit.create(Data::class.java).getDataPanen(tanamanid)
            binding.lyRekapPanen.visibility = View.VISIBLE
            binding.lyAddDataPanen.visibility = View.GONE
            binding.tvTotalPanen.text = "${response.jumlahpanen} KG"
            binding.tvKeterangan.text = response.keterangan
            binding.tvTanggalPanen.text = "${formatTanggalKeIndonesia(response.createAt.toString())}"
            binding.etJumlahPanen.setText(response.jumlahpanen.toString())
            binding.etKeteranganTambahan.setText(response.keterangan)

            oldJumlahPanen = response.jumlahpanen.toString()
            oldKeterangan = response.keterangan.toString()
            id = response.id.toString()
            Glide.with(requireContext()).load(fileUrl+url(response.foto1.toString())).into(binding.ivPanen1)
            Glide.with(requireContext()).load(fileUrl+url(response.foto2.toString())).into(binding.ivPanen2)
            Glide.with(requireContext()).load(fileUrl+url(response.foto3.toString())).into(binding.ivPanen3)
            Glide.with(requireContext()).load(fileUrl+url(response.foto4.toString())).into(binding.ivPanen4)
            Glide.with(requireContext()).load(fileUrl+url(response.foto1.toString())).into(binding.imageDok1)
            Glide.with(requireContext()).load(fileUrl+url(response.foto2.toString())).into(binding.imageDok2)
            Glide.with(requireContext()).load(fileUrl+url(response.foto3.toString())).into(binding.imageDok3)
            Glide.with(requireContext()).load(fileUrl+url(response.foto4.toString())).into(binding.imageDok4)
            binding.imageDok1.visibility = View.VISIBLE
            binding.imageDok2.visibility = View.VISIBLE
            binding.imageDok3.visibility = View.VISIBLE
            binding.imageDok4.visibility = View.VISIBLE

            when (response.status?.uppercase()) {
                "UNVERIFIED" -> {
                    binding.tvStatusPanen.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.holo_blue_dark
                        )
                    )
                    binding.tvStatusPanen.text = response.status
                    binding.btRevisiPanen.visibility = View.GONE
                }
                "VERIFIED" -> {
                    binding.tvStatusPanen.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.holo_green_dark
                        )
                    )
                    binding.tvStatusPanen.text = response.status
                    binding.btRevisiPanen.visibility = View.GONE
                }
                "REJECTED" -> {
                    binding.tvStatusPanen.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.holo_red_dark
                        )
                    )
                    binding.tvStatusPanen.text = "${response.status} - ${response.alasan}"
                    binding.btRevisiPanen.visibility = View.VISIBLE
                    binding.btRevisiPanen.setOnClickListener {
                        binding.lyRekapPanen.visibility = View.GONE
                        binding.lyAddDataPanen.visibility = View.VISIBLE
                        binding.btRevisiPanen.visibility = View.GONE
                        binding.btnKirimDataPanen.visibility = View.GONE
                        binding.btnKirimDataPanenUpdate.visibility = View.VISIBLE
                    }
                }
                else -> binding.tvStatusPanen.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // default
            }
        }catch (e: Exception){
            e.printStackTrace()
            binding.lyRekapPanen.visibility = View.GONE
            binding.lyAddDataPanen.visibility = View.VISIBLE
        }
    }

    private fun isValid(): Boolean {
        if (binding.etJumlahPanen.text.toString().isEmpty()) {
            binding.etJumlahPanen.error = "Jumlah Panen Harus Diisi"
            return false
        }
        if (binding.etKeteranganTambahan.text.toString().isEmpty()) {
            binding.etKeteranganTambahan.error = "Keterangan Harus Diisi"
            return false
        }
        if(binding.imageDok1.isGone){
            binding.errorDok1.visibility = View.VISIBLE
            return false
        }
        if(binding.imageDok2.isGone){
            binding.errorDok2.visibility = View.VISIBLE
            return false
        }
        if(binding.imageDok3.isGone){
            binding.errorDok3.visibility = View.VISIBLE
            return false
        }
        if(binding.imageDok4.isGone){
            binding.errorDok4.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun resetField(){
        binding.etJumlahPanen.text.clear()
        binding.etKeteranganTambahan.text.clear()
        binding.errorDok1.visibility = View.GONE
        binding.errorDok2.visibility = View.GONE
        binding.errorDok3.visibility = View.GONE
        binding.errorDok4.visibility = View.GONE
        binding.imageDok1.visibility = View.GONE
        binding.imageDok2.visibility = View.GONE
        binding.imageDok3.visibility = View.GONE
        binding.imageDok4.visibility = View.GONE
        binding.watermarks1.visibility = View.GONE
        binding.watermarks2.visibility = View.GONE
        binding.watermarks3.visibility = View.GONE
        binding.watermarks4.visibility = View.GONE
        binding.buttonPilihDok1.text = "PILIH DOKUMENTASI"
        binding.buttonPilihDok2.text = "PILIH DOKUMENTASI"
        binding.buttonPilihDok3.text = "PILIH DOKUMENTASI"
        binding.buttonPilihDok4.text = "PILIH DOKUMENTASI"
        isDok1camera = false
        isDok2camera = false
        isDok3camera = false
        isDok4camera = false
        pathDok1 = ""
        pathDok2 = ""
        pathDok3 = ""
        pathDok4 = ""
        binding.tvNrpCamera.text = ""
        binding.tvNrpCamera1.text = ""
        binding.tvNrpCamera2.text = ""
        binding.tvNrpCamera3.text = ""
    }

    private fun processData(){
        val args: PanenFragmentArgs by navArgs()
        var foto1Part: MultipartBody.Part? = null
        var foto2Part: MultipartBody.Part? = null
        var foto3Part: MultipartBody.Part? = null
        var foto4Part: MultipartBody.Part? = null
        var tanaman_id: RequestBody = createPartFromString(args.tanamanId)
        var jumlahpanen: RequestBody = createPartFromString(binding.etJumlahPanen.text.toString())
        var keterangan: RequestBody = createPartFromString(binding.etKeteranganTambahan.text.toString())

        foto1Part = if (isDok1camera) {
            prepareFilePart("foto1", compressCamera(File(pathDok1)))
        }else{
            prepareFilePart("foto1", saveBitmapToFile(getBitmapFromView(binding.dok1)))
        }

        foto2Part = if (isDok2camera) {
            prepareFilePart("foto2", compressCamera(File(pathDok2)))
        }else{
            prepareFilePart("foto2", saveBitmapToFile(getBitmapFromView(binding.dok2)))
        }

        foto3Part = if (isDok3camera) {
            prepareFilePart("foto3", compressCamera(File(pathDok3)))
        }else{
            prepareFilePart("foto3", saveBitmapToFile(getBitmapFromView(binding.dok3)))
        }

        foto4Part = if (isDok4camera) {
            prepareFilePart("foto4", compressCamera(File(pathDok4)))
        }else {
            prepareFilePart("foto4", saveBitmapToFile(getBitmapFromView(binding.dok4)))
        }

        if(foto1Part != null && foto2Part != null && foto3Part != null && foto4Part != null && tanaman_id != null && jumlahpanen != null && keterangan != null){
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengirim data?")
                .setPositiveButton("Ya") { _, _ ->
                    lifecycleScope.launch {
                        sendDataPanen(
                            foto1Part,
                            foto2Part,
                            foto3Part,
                            foto4Part,
                            tanaman_id,
                            jumlahpanen,
                            keterangan
                        )
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()

        }
    }
    private fun processData2(){
        var id1 = createPartFromString(id)
        var foto1Part: MultipartBody.Part? = null
        var foto2Part: MultipartBody.Part? = null
        var foto3Part: MultipartBody.Part? = null
        var foto4Part: MultipartBody.Part? = null
        var jumlahpanen: RequestBody? = null
        var keterangan: RequestBody? = null

        if (isDok1Changed) {
            foto1Part = if (isDok1camera) {
                prepareFilePart("foto1", compressCamera(File(pathDok1)))
            } else {
                prepareFilePart("foto1", saveBitmapToFile(getBitmapFromView(binding.dok1)))
            }
        }
        if (isDok2Changed) {
            foto2Part = if (isDok2camera) {
                prepareFilePart("foto2", compressCamera(File(pathDok2)))
            } else {
                prepareFilePart("foto2", saveBitmapToFile(getBitmapFromView(binding.dok2)))
            }
        }
        if (isDok3Changed) {
            foto3Part = if (isDok3camera) {
                prepareFilePart("foto3", compressCamera(File(pathDok3)))
            } else {
                prepareFilePart("foto3", saveBitmapToFile(getBitmapFromView(binding.dok3)))
            }
        }
        if (isDok4Changed) {
            foto4Part = if (isDok4camera) {
                prepareFilePart("foto4", compressCamera(File(pathDok4)))
            } else {
                prepareFilePart("foto4", saveBitmapToFile(getBitmapFromView(binding.dok4)))
            }
        }
        if(binding.etJumlahPanen.text.toString() != oldJumlahPanen){
            jumlahpanen = createPartFromString(binding.etJumlahPanen.text.toString())
        }
        if(binding.etKeteranganTambahan.text.toString() != oldKeterangan){
            keterangan = createPartFromString(binding.etKeteranganTambahan.text.toString())
        }

        if(foto1Part == null && foto2Part == null && foto3Part == null && foto4Part == null && jumlahpanen == null && keterangan == null){
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Tidak ada perubahan data!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    binding.btnKirimDataPanenUpdate.isEnabled = true
                }
                .show()
        }else {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengirim data?")
                .setPositiveButton("Ya") { _, _ ->
                    lifecycleScope.launch {
                        sendDataPanenUpdate(
                            id1,
                            jumlahpanen,
                            keterangan,
                            foto1Part,
                            foto2Part,
                            foto3Part,
                            foto4Part,
                        )
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private suspend fun sendDataPanenUpdate(
        id1: RequestBody,
        jumlahpanen: RequestBody?,
        keterangan: RequestBody?,
        foto1Part: MultipartBody.Part?,
        foto2Part: MultipartBody.Part?,
        foto3Part: MultipartBody.Part?,
        foto4Part: MultipartBody.Part?
    ) {
        try {
            Loading.show(requireContext())
            val result = Client.retrofit.create(Data::class.java).uploadUpdateDataPanen(
                id1,
                jumlahpanen,
                keterangan,
                foto1Part,
                foto2Part,
                foto3Part,
                foto4Part
            )

            if(result.isSuccessful){
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Berhasil")
                    .setMessage("Data berhasil dikirim")
                    .setPositiveButton("OK", { _, _ ->
                        resetField()
                        val fragmentManager = parentFragmentManager
                        val currentFragment = this

                        fragmentManager.beginTransaction()
                            .detach(currentFragment)
                            .commitNow()
                    })
                    .show()
                binding.btnKirimDataPanenUpdate.isEnabled = true
            }else{
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Gagal")
                    .setMessage("Data gagal dikirim")
                    .setPositiveButton("OK", null)
                    .show()
                binding.btnKirimDataPanenUpdate.isEnabled = true
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
            binding.btnKirimDataPanenUpdate.isEnabled = true
        }
    }

    private suspend fun sendDataPanen(
        foto1Part: MultipartBody.Part,
        foto2Part: MultipartBody.Part,
        foto3Part: MultipartBody.Part,
        foto4Part: MultipartBody.Part,
        tanaman_id: RequestBody,
        jumlahpanen: RequestBody,
        keterangan: RequestBody
    ) {
        try {
            Loading.show(requireContext())
            val result = Client.retrofit.create(Data::class.java).uploadDataPanen(
                tanaman_id,
                jumlahpanen,
                keterangan,
                foto1Part,
                foto2Part,
                foto3Part,
                foto4Part
            )

            if(result.isSuccessful){
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Berhasil")
                    .setMessage("Data berhasil dikirim")
                    .setPositiveButton("OK", { _, _ ->
                        resetField()
                        findNavController().popBackStack()
                    })
                    .show()
                binding.btnKirimDataPanen.isEnabled = true
            }else{
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Gagal")
                    .setMessage("Data gagal dikirim")
                    .setPositiveButton("OK", null)
                    .show()
                binding.btnKirimDataPanen.isEnabled = true
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
            binding.btnKirimDataPanen.isEnabled = true
        }
    }
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val filename = "IMG_WM_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out)
        }
        return file
    }

    private fun compressCamera(inputFile: File, quality: Int = 40): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath, options)

        val compressedFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "COMPRESSED_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(compressedFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        return compressedFile
    }

    fun url(fullPath: String): String {
        val keyword = "uploads/"
        val index = fullPath.indexOf(keyword)
        return if (index != -1) {
            fullPath.substring(index + keyword.length)
        } else {
            fullPath // fallback kalau tidak mengandung "uploads/"
        }
    }

    fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    fun createPartFromString(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}