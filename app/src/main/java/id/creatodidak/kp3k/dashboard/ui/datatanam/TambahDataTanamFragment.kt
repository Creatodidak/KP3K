package id.creatodidak.kp3k.dashboard.ui.datatanam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.graphics.createBitmap
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.databinding.FragmentTambahDataTanamBinding
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.SumberFoto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

import java.io.File
import java.io.FileOutputStream
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.helper.Loading
class TambahDataTanamFragment : Fragment() {
    private var _binding: FragmentTambahDataTanamBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahDataTanamBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        binding.btnKirimDataTanam.setOnClickListener {
            if(isValid()){
                processData()
            }
        }
        return root
    }

    private fun processData(){
        val args: TambahDataTanamFragmentArgs by navArgs()
        var foto1Part: MultipartBody.Part? = null
        var foto2Part: MultipartBody.Part? = null
        var foto3Part: MultipartBody.Part? = null
        var foto4Part: MultipartBody.Part? = null
        var kodelahan: RequestBody = createPartFromString(args.lahanId)
        var luasTanam: RequestBody
        var komoditas: RequestBody = createPartFromString("1")
        var prediksi: RequestBody
        var varietas: RequestBody

        foto1Part = if (isDok1camera) {
            prepareFilePart("foto1", File(pathDok1))
        }else{
            prepareFilePart("foto1", saveBitmapToFile(getBitmapFromView(binding.dok1)))
        }

        foto2Part = if (isDok2camera) {
            prepareFilePart("foto2", File(pathDok2))
        }else{
            prepareFilePart("foto2", saveBitmapToFile(getBitmapFromView(binding.dok2)))
        }

        foto3Part = if (isDok3camera) {
            prepareFilePart("foto3", File(pathDok3))
        }else{
            prepareFilePart("foto3", saveBitmapToFile(getBitmapFromView(binding.dok3)))
        }

        foto4Part = if (isDok4camera) {
            prepareFilePart("foto4", File(pathDok4))
        }else {
            prepareFilePart("foto4", saveBitmapToFile(getBitmapFromView(binding.dok4)))
        }

        luasTanam = createPartFromString(binding.etLuasTanam.text.toString())
        prediksi = createPartFromString(binding.etPrediksi.text.toString())
        varietas  = createPartFromString(binding.etVarietas.text.toString())
        if(foto1Part != null && foto2Part != null && foto3Part != null && foto4Part != null && luasTanam != null && varietas != null && prediksi != null && kodelahan != null && komoditas != null){
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengirim data?")
                .setPositiveButton("Ya") { _, _ ->
                    lifecycleScope.launch {
                        sendDatatanam(
                            foto1Part,
                            foto2Part,
                            foto3Part,
                            foto4Part,
                            luasTanam,
                            varietas,
                            prediksi,
                            kodelahan,
                            komoditas
                        )
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()

        }
    }

    private suspend fun sendDatatanam(
        foto1Part: MultipartBody.Part,
        foto2Part: MultipartBody.Part,
        foto3Part: MultipartBody.Part,
        foto4Part: MultipartBody.Part,
        luasTanam: RequestBody,
        varietas: RequestBody,
        prediksi: RequestBody,
        kodelahan: RequestBody,
        komoditas: RequestBody
    ){
        try {
            Loading.show(requireContext())
            var result = Client.retrofit.create(Data::class.java).uploadLaporanTanam(
                kodelahan,
                luasTanam,
                prediksi,
                komoditas,
                varietas,
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
            }else{
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Gagal")
                    .setMessage("Data gagal dikirim")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
    private fun isValid(): Boolean {
        if (binding.etLuasTanam.text.toString().isEmpty()) {
            binding.etLuasTanam.error = "Luas Tanam Harus Diisi"
            return false
        }
        if (binding.etPrediksi.text.toString().isEmpty()) {
            binding.etPrediksi.error = "Prediksi Harus Diisi"
            return false
        }
        if (binding.etVarietas.text.toString().isEmpty()) {
            binding.etVarietas.error = "Varietas Bibit Harus Diisi"
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
        binding.etLuasTanam.text.clear()
        binding.etPrediksi.text.clear()
        binding.etVarietas.text.clear()
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    fun createPartFromString(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}