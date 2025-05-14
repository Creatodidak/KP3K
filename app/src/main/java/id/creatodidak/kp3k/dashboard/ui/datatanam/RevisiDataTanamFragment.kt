package id.creatodidak.kp3k.dashboard.ui.datatanam

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
import androidx.core.graphics.createBitmap
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.databinding.FragmentRevisiDataTanamBinding
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

class RevisiDataTanamFragment : Fragment() {
    private lateinit var _binding : FragmentRevisiDataTanamBinding
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
    private var isDok1Changed = false
    private var isDok2Changed = false
    private var isDok3Changed = false
    private var isDok4Changed = false
    private var oldMasaTanam = ""
    private var oldLuasTanam = ""
    private var oldPrediksi = ""
    private var oldVarietas = ""
    val fileUrl = "${BuildConfig.BASE_URL}file/"

    val args: RevisiDataTanamFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRevisiDataTanamBinding.inflate(inflater, container, false)
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

        lifecycleScope.launch {
            loadExistData(args.id)
        }
        return root
    }

    private suspend fun loadExistData(id: String){
        Loading.show(requireContext())
        try {
            val result = Client.retrofit.create(Data::class.java).getDataTanamById(id)
            if(result !== null){
                binding.etMasaTanam.setText(result.masatanam)
                binding.etLuasTanam.setText(result.luastanam)
                binding.etPrediksi.setText(result.prediksipanen)
                binding.etVarietas.setText(result.varietas)
                Glide.with(requireContext())
                    .load(fileUrl+url(result.foto1.toString()))
                    .into(binding.imageDok1)
                Glide.with(requireContext())
                    .load(fileUrl+url(result.foto2.toString()))
                    .into(binding.imageDok2)
                Glide.with(requireContext())
                    .load(fileUrl+url(result.foto3.toString()))
                    .into(binding.imageDok3)
                Glide.with(requireContext())
                    .load(fileUrl+url(result.foto4.toString()))
                    .into(binding.imageDok4)
                oldMasaTanam = result.masatanam.toString()
                oldLuasTanam = result.luastanam.toString()
                oldPrediksi = result.prediksipanen.toString()
                oldVarietas = result.varietas.toString()
                binding.btnKirimDataTanam.setOnClickListener {
                    binding.btnKirimDataTanam.isEnabled = false
                    if(isValid()){
                        processData()
                    }else{
                        binding.btnKirimDataTanam.isEnabled = true
                    }
                }
            }else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Gagal mendapatkan data dari server!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        findNavController().popBackStack()
                    }
                    .show()
            }
        }catch (e: Exception){
            e.printStackTrace()
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage("Gagal mendapatkan data dari server! -> $e")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    findNavController().popBackStack()
                }
                .show()
        }finally {
            Loading.hide()
        }
    }

    private fun processData(){
        var foto1Part: MultipartBody.Part? = null
        var foto2Part: MultipartBody.Part? = null
        var foto3Part: MultipartBody.Part? = null
        var foto4Part: MultipartBody.Part? = null
        var id: RequestBody = createPartFromString(args.id)
        var luasTanam: RequestBody? = null
        var prediksi: RequestBody? = null
        var varietas: RequestBody? = null
        var masatanam: RequestBody? = null

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

        if(binding.etMasaTanam.text.toString() != oldMasaTanam){
            masatanam = createPartFromString(binding.etMasaTanam.text.toString())
        }
        if(binding.etLuasTanam.text.toString() != oldLuasTanam){
            luasTanam = createPartFromString(binding.etLuasTanam.text.toString())
        }
        if(binding.etPrediksi.text.toString() != oldPrediksi){
            prediksi = createPartFromString(binding.etPrediksi.text.toString())
        }
        if(binding.etVarietas.text.toString() != oldVarietas){
            varietas = createPartFromString(binding.etVarietas.text.toString())
        }

        if(foto1Part == null && foto2Part == null && foto3Part == null && foto4Part == null && luasTanam == null && prediksi == null && varietas == null && masatanam == null){
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Tidak ada perubahan data!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    binding.btnKirimDataTanam.isEnabled = true
                }
                .show()
        }else {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin mengirim data?")
                .setPositiveButton("Ya") { _, _ ->
                    lifecycleScope.launch {
                        sendDataTanamUpdate(
                            id,
                            masatanam,
                            luasTanam,
                            varietas,
                            prediksi,
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

    private fun isValid(): Boolean {
        if (binding.etMasaTanam.text.toString().isEmpty()) {
            binding.etMasaTanam.error = "Masa Tanam Harus Diisi"
            return false
        }
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

    private suspend fun sendDataTanamUpdate(
        id: RequestBody,
        masatanam: RequestBody?,
        luasTanam: RequestBody?,
        varietas: RequestBody?,
        prediksi: RequestBody?,
        foto1Part: MultipartBody.Part?,
        foto2Part: MultipartBody.Part?,
        foto3Part: MultipartBody.Part?,
        foto4Part: MultipartBody.Part?,
    ) {
        try {
            Loading.show(requireContext())
            var result = Client.retrofit.create(Data::class.java).updateLaporanTanam(
                id,
                masatanam,
                luasTanam,
                prediksi,
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
                        findNavController().popBackStack()
                    })
                    .show()
                binding.btnKirimDataTanam.isEnabled = true
            }else{
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setTitle("Gagal")
                    .setMessage("Data gagal dikirim")
                    .setPositiveButton("OK", null)
                    .show()
                binding.btnKirimDataTanam.isEnabled = true
            }
        }catch (e: Exception){
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK", null)
                .show()
            binding.btnKirimDataTanam.isEnabled = true
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


    fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    fun createPartFromString(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    fun url(fullPath: String): String {
        val keyword = "uploads/"
        val index = fullPath.indexOf(keyword)
        return if (index != -1) {
            fullPath.substring(index + keyword.length)
        } else {
            fullPath // fallback kalau tidak mengandung "uploads/"
        }
    }
}