package id.creatodidak.kp3k.dashboard.ui.dataperkembangan

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.databinding.FragmentAddDataPerkembanganTanamanBinding
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.SumberFoto
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddDataPerkembanganTanamanFragment : Fragment() {
    private lateinit var _binding: FragmentAddDataPerkembanganTanamanBinding
    private val binding get() = _binding
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
    private val args: AddDataPerkembanganTanamanFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddDataPerkembanganTanamanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val listCurahHujan = listOf("PILIH", "RENDAH", "SEDANG", "TINGGI")
        val listSeranganHama = listOf("PILIH", "TANAMAN TERSERANG HAMA", "TANAMAN TIDAK TERSERANG HAMA")
        val listGangguan = listOf("PILIH", "ADA", "TIDAK ADA")

        binding.spCurahHujan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listCurahHujan)
        binding.spHama.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listSeranganHama)
        binding.spGangguanAlam.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listGangguan)
        binding.spGangguanLainnya.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listGangguan)

        binding.spHama.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {
                        binding.lyKeteranganHama.visibility = View.GONE
                    }

                    1 -> {
                        binding.lyKeteranganHama.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.lyKeteranganHama.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.spGangguanAlam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {
                        binding.lyKeteranganGangguanAlam.visibility = View.GONE
                    }

                    1 -> {
                        binding.lyKeteranganGangguanAlam.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.lyKeteranganGangguanAlam.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.spGangguanLainnya.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {
                        binding.lyKeteranganGangguanLainnya.visibility = View.GONE
                    }

                    1 -> {
                        binding.lyKeteranganGangguanLainnya.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.lyKeteranganGangguanLainnya.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
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

        binding.btnKirimDataPerkembanganTanam.setOnClickListener {
            binding.btnKirimDataPerkembanganTanam.isEnabled = false
            if(isValid()){
                processData()
            }else{
                binding.btnKirimDataPerkembanganTanam.isEnabled = true
            }
        }
        return root
    }

    private fun isValid(): Boolean {
        if (binding.etTinggiTanaman.text.toString().isEmpty()) {
            binding.etTinggiTanaman.error = "Tinggi Tanaman Harus Diisi"
            return false
        }
        if (binding.etKondisiTanah.text.toString().isEmpty()) {
            binding.etKondisiTanah.error = "Kondisi Tanah Harus Diisi"
            return false
        }
        if (binding.etWarnaDaun.text.toString().isEmpty()) {
            binding.etWarnaDaun.error = "Warna daun harus diisi"
            return false
        }
        if(binding.spHama.selectedItemPosition == 0){
            binding.errHama.visibility = View.VISIBLE
            return false
        }
        if(binding.spHama.selectedItemPosition == 0 && binding.etKeteranganHama.text.toString().isEmpty()){
            binding.etKeteranganHama.error = "Keterangan Hama harus diisi"
            return false
        }
        if(binding.spGangguanAlam.selectedItemPosition == 0){
            binding.errGangguanAlam.visibility = View.VISIBLE
            return false
        }
        if(binding.spGangguanAlam.selectedItemPosition == 0 && binding.etKeteranganGangguanAlam.text.toString().isEmpty()){
            binding.etKeteranganGangguanAlam.error = "Keterangan gangguan alam harus diisi"
            return false
        }
        if(binding.spGangguanLainnya.selectedItemPosition == 0){
            binding.errGangguanLainnya.visibility = View.VISIBLE
            return false
        }
        if(binding.spGangguanLainnya.selectedItemPosition == 0 && binding.etKeteranganGangguanLainnya.text.toString().isEmpty()){
            binding.etKeteranganGangguanLainnya.error = "Keterangan gangguan lainnya harus diisi"
            return false
        }
        if(binding.etPhTanah.text.toString().isEmpty()){
            binding.etPhTanah.error = "pH Tanah harus diisi"
            return false
        }
        if(binding.etKondisiAir.text.toString().isEmpty()){
            binding.etKondisiAir.error = "Kondisi Air harus diisi"
            return false
        }
        if(binding.etPupuk.text.toString().isEmpty()){
            binding.etPupuk.error = "Pupuk harus diisi"
            return false
        }
        if(binding.etPestisida.text.toString().isEmpty()){
            binding.etPestisida.error = "Pestisida harus diisi"
            return false
        }
        if(binding.etKeteranganTambahan.text.toString().isEmpty()){
            binding.etKeteranganTambahan.error = "Keterangan tambahan harus diisi"
            return false
        }
        if(binding.spCurahHujan.selectedItemPosition == 0){
            binding.errCurahHujan.visibility = View.VISIBLE
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

    private fun processData(){
        var foto1Part: MultipartBody.Part? = null
        var foto2Part: MultipartBody.Part? = null
        var foto3Part: MultipartBody.Part? = null
        var foto4Part: MultipartBody.Part? = null
        var tanaman_id: RequestBody = createPartFromString(args.tanamanId)
        var kodelahan: RequestBody = createPartFromString(args.kodelahan)
        var tinggiTanaman: RequestBody = createPartFromString(binding.etTinggiTanaman.text.toString())
        var kondisitanah: RequestBody = createPartFromString(binding.etKondisiTanah.text.toString())
        var warnaDaun: RequestBody = createPartFromString(binding.etWarnaDaun.text.toString())
        var curahhujan: RequestBody = createPartFromString(binding.spCurahHujan.selectedItem.toString())
        var hama : RequestBody = createPartFromString(binding.spHama.selectedItem.toString())
        val keteranganHama: RequestBody = createPartFromString(
            binding.etKeteranganHama.text.toString().takeIf { it.isNotBlank() } ?: "-"
        )
        val keterangan: RequestBody = createPartFromString(
            binding.etKeteranganTambahan.text.toString().takeIf { it.isNotBlank() } ?: "-"
        )
        var phTanah: RequestBody = createPartFromString(binding.etPhTanah.text.toString())
        var kondisiAir: RequestBody = createPartFromString(binding.etKondisiAir.text.toString())
        var pupuk: RequestBody = createPartFromString(binding.etPupuk.text.toString())
        var pestisida: RequestBody = createPartFromString(binding.etPestisida.text.toString())
        val gangguanAlam: RequestBody = createPartFromString(binding.spGangguanAlam.selectedItem.toString())
        val gangguanLainnya: RequestBody = createPartFromString(binding.spGangguanLainnya.selectedItem.toString())
        val keteranganGangguanAlam: RequestBody = createPartFromString(
            binding.etKeteranganGangguanAlam.text.toString().takeIf { it.isNotBlank() } ?: "-"
        )
        val keteranganGangguanLainnya: RequestBody = createPartFromString(
            binding.etKeteranganGangguanLainnya.text.toString().takeIf { it.isNotBlank() } ?: "-"
        )

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

        if(foto1Part != null && foto2Part != null && foto3Part != null && foto4Part != null && tinggiTanaman != null && kondisitanah != null && warnaDaun != null && curahhujan != null && hama != null && keteranganHama != null && keterangan != null && tanaman_id != null && kodelahan != null && phTanah != null && kondisiAir != null && pupuk != null && pestisida != null && gangguanAlam != null && gangguanLainnya != null && keteranganGangguanAlam != null && keteranganGangguanLainnya != null){
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengirim data?")
                .setPositiveButton("Ya") { _, _ ->
                    lifecycleScope.launch {
                        sendDataKembangTanaman(foto1Part, foto2Part, foto3Part, foto4Part, tinggiTanaman, kondisitanah, warnaDaun, curahhujan, hama, keteranganHama, keterangan, tanaman_id, kodelahan, phTanah, kondisiAir, pupuk, pestisida, gangguanAlam, gangguanLainnya, keteranganGangguanAlam, keteranganGangguanLainnya)
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()

        }
    }

    private suspend fun sendDataKembangTanaman(
        foto1Part: MultipartBody.Part,
        foto2Part: MultipartBody.Part,
        foto3Part: MultipartBody.Part,
        foto4Part: MultipartBody.Part,
        tinggiTanaman: RequestBody,
        kondisitanah: RequestBody,
        warnaDaun: RequestBody,
        curahhujan: RequestBody,
        hama: RequestBody,
        keteranganHama: RequestBody,
        keterangan: RequestBody,
        tanaman_id: RequestBody,
        kodelahan: RequestBody,
        phTanah: RequestBody,
        kondisiAir: RequestBody,
        pupuk: RequestBody,
        pestisida: RequestBody,
        gangguanAlam: RequestBody,
        gangguanLainnya: RequestBody,
        keteranganGangguanAlam: RequestBody,
        keteranganGangguanLainnya: RequestBody
    ) {
        try {
            Loading.show(requireContext())
            var result = Client.retrofit.create(Data::class.java).uploadLaporanPerkembanganTanam(
                kodelahan,
                tanaman_id,
                tinggiTanaman,
                kondisitanah,
                warnaDaun,
                curahhujan,
                hama,
                keteranganHama,
                keterangan,
                phTanah,
                kondisiAir,
                pupuk,
                pestisida,
                gangguanAlam,
                gangguanLainnya,
                keteranganGangguanAlam,
                keteranganGangguanLainnya,
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
                binding.btnKirimDataPerkembanganTanam.isEnabled = true
            }else{
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Gagal")
                    .setMessage("Data gagal dikirim")
                    .setPositiveButton("OK", null)
                    .show()
                binding.btnKirimDataPerkembanganTanam.isEnabled = true
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
        }
        binding.btnKirimDataPerkembanganTanam.isEnabled = true
    }

    private fun resetField(){
        binding.etTinggiTanaman.text.clear()
        binding.etKondisiTanah.text.clear()
        binding.etWarnaDaun.text.clear()
        binding.etKeteranganHama.text.clear()
        binding.spHama.setSelection(0)
        binding.spCurahHujan.setSelection(0)
        binding.etKeteranganTambahan.text.clear()
        binding.etKeteranganHama.text.clear()
        binding.errCurahHujan.visibility = View.GONE
        binding.errHama.visibility = View.GONE
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

    fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    fun createPartFromString(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}