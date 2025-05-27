package id.creatodidak.kp3k.dashboard.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.yalantis.ucrop.UCrop
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.AtensiAdapter
import id.creatodidak.kp3k.adapter.TargetCapaianAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.MBasicData
import id.creatodidak.kp3k.api.model.RAtensiItem
import id.creatodidak.kp3k.api.model.getAtensiVal
import id.creatodidak.kp3k.databinding.FragmentHomeBinding
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.formatDuaDesimal
import id.creatodidak.kp3k.helper.isInternetAvailable
import id.creatodidak.kp3k.helper.isInternetAvailableAsync
import id.creatodidak.kp3k.network.SocketManager
import id.creatodidak.kp3k.offline.AppDatabase
import id.creatodidak.kp3k.offline.entity.BasicDataEntity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    private lateinit var adapter: AtensiAdapter
    private var dataAtensi = mutableListOf<RAtensiItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPrefs = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)

        binding.nama.text = "${sharedPrefs.getString("pangkat", "")} ${sharedPrefs.getString("nama", "")}"
        binding.tvBPKP.text = "BA PENGGERAK DESA ${sharedPrefs.getString("desa", "")}, KEC. ${sharedPrefs.getString("kecamatan", "")}, KAB. ${sharedPrefs.getString("kabupaten", "")}" // Fix missing closing quote

        val BASE_URL = "${BuildConfig.BASE_URL}file/"
        db = AppDatabase.getDatabase(requireContext())

        val foto = sharedPrefs.getString("foto", "")
        if (foto == "/personil/img/default.jpg") {
            Glide.with(this)
                .load(R.drawable.user)
                .circleCrop()
                .into(binding.fotoProfil)
        } else {
            Glide.with(this)
                .load(BASE_URL + foto)
                .placeholder(R.drawable.logo)
                .circleCrop()
                .into(binding.fotoProfil)
        }

        binding.tvGantiFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val sourceUri = result.data!!.data!!
                val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))

                val intent = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(512, 512)
                    .getIntent(requireContext())
                cropImageLauncher.launch(intent)
            }
        }

        cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                if(resultUri != null){
                    lifecycleScope.launch {
                        uploadFotoProfile(resultUri)
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                cropError?.printStackTrace()
            }
        }

        val youtubePlayerView = binding.youtubePlayerView
        lifecycle.addObserver(youtubePlayerView)
        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "p4OALvMzx9A"
                youTubePlayer.cueVideo(videoId, 0f)
            }
        })

        adapter = AtensiAdapter(
            onReadClicked = { id ->
                simpanIdTerbaca(requireContext(), id)
                binding.chipGroup.check(R.id.chipBelumDibaca)
            }
        )

        binding.rvAtensi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAtensi.adapter = adapter

        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            tampilkanDataSesuaiChip()
        }

        lifecycleScope.launch {
            try {
                val kab = sharedPrefs.getString("kabupatenid", "")
                val role = sharedPrefs.getString("role", "") ?: ""
                val response = Client.retrofit.create(Data::class.java).getAtensi(getAtensiVal(role, kab))
                dataAtensi.clear()
                dataAtensi.addAll(response)

                binding.chipGroup.check(R.id.chipSemua) // Pindahkan ke sini
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching data: ${e.message}")
            }
        }


    }

    fun tampilkanDataSesuaiChip() {
        val readlist = ambilIdTerbaca(requireContext())
        val readIdList = readlist.mapNotNull { it.toIntOrNull() }

        val filtered = when (binding.chipGroup.checkedChipId) {
            R.id.chipBelumDibaca -> dataAtensi.filter { it.id !in readIdList }
            R.id.chipSudahDibaca -> dataAtensi.filter { it.id in readIdList }
            else -> dataAtensi
        }

        adapter.submitList(filtered)
    }

    private suspend fun uploadFotoProfile(uri: Uri) {
        try {
            Loading.show(requireContext())
            val file = getFileFromUri(uri)
            if (file != null && file.exists()) {
                val fotoRequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData("foto", file.name, fotoRequestBody)
                val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
                val nrp = sh.getString("nrp", "")?.takeIf { it.isNotEmpty() } ?: run {
                    println("NRP kosong!")
                    return
                }
                val nrpRequestBody = nrp.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = Client.retrofit.create(Data::class.java).uploadFotoProfile(nrpRequestBody, fotoPart)

                if (response.kode == 200) {
                    Loading.hide()
                    with(sh.edit()){
                        putString("foto", response.msg)
                        apply()
                    }
                    AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Berhasil")
                        .setMessage("Foto Profil berhasil diupload")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            val BASE_URL = "${BuildConfig.BASE_URL}file/"
                            Glide.with(this)
                                .load(BASE_URL + response.msg)
                                .placeholder(R.drawable.logo)
                                .circleCrop()
                                .into(binding.fotoProfil)
                        }
                        .show()
                } else {
                    Loading.hide()
                    AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Gagal")
                        .setMessage(response.msg)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else {
                Loading.hide()
                AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setTitle("Gagal")
                    .setMessage("Foto tidak ditemukan!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        } catch (e: Exception) {
            Loading.hide()
            AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle("Gagal")
                .setMessage(e.message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            println("Gagal mengambil file dari URI: ${e.message}")
            null
        }
    }

}

fun simpanIdTerbaca(context: Context, id: Int) {
    val pref = context.getSharedPreferences("atensi", Context.MODE_PRIVATE)
    val editor = pref.edit()

    val existingSet = pref.getStringSet("idTerbaca", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    existingSet.add(id.toString()) // convert di sini

    editor.putStringSet("idTerbaca", existingSet)
    editor.apply()
}


fun ambilIdTerbaca(context: Context): Set<String> {
    val pref = context.getSharedPreferences("atensi", Context.MODE_PRIVATE)
    return pref.getStringSet("idTerbaca", emptySet()) ?: emptySet()
}
