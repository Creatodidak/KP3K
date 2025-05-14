package id.creatodidak.kp3k.dashboard.ui.lahantugas

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.MNewOwnerItem
import id.creatodidak.kp3k.api.model.MOwnerItem
import id.creatodidak.kp3k.api.model.OwnerItem
import id.creatodidak.kp3k.api.model.newLahan
import id.creatodidak.kp3k.databinding.FragmentAddLahanBinding
import id.creatodidak.kp3k.databinding.FragmentRincianLahanBinding
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch

class AddLahan : Fragment() {
    private var _binding: FragmentAddLahanBinding? = null
    private val binding get() = _binding!!
    private var DataOwner = mutableListOf<MNewOwnerItem?>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val jenislahan = listOf("PILIH", "MONOKULTUR", "TUMPANG SARI")
        val spJenis = binding.spJenisLahan

        spJenis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jenislahan)
        spJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 ->{
                        binding.LLDataPemilik.visibility = View.GONE
                        binding.LLDataLahan.visibility = View.GONE
                        binding.btKirimLahan.visibility = View.GONE
                        resetField()
                    }
                    1 ->{
                        lifecycleScope.launch {
                            loadOwner("monokultur")
                        }
                    }
                    2 ->{
                        lifecycleScope.launch {
                            loadOwner("tumpangsari")
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.spPemilik.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position != 0){
                    binding.LLDataLahan.visibility = View.VISIBLE
                    binding.btKirimLahan.visibility = View.VISIBLE
                }else{
                    binding.LLDataLahan.visibility = View.GONE
                    binding.btKirimLahan.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.btKirimLahan.setOnClickListener {
            if(startValidate()){
                AlertDialog.Builder(requireContext())
                    .setTitle("Peringatan")
                    .setMessage("Data akan dikirim ke server dan hanya dapat dibatalkan oleh Admin Polres Anda, kirim data?")
                    .setCancelable(false)
                    .setPositiveButton("Kirim") { dialog, _ ->
                        dialog.dismiss()
                        lifecycleScope.launch {
                            sendData()
                        }
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        return root
    }

    private suspend fun sendData() {
        try {
            if (!startValidate()) return
            Loading.show(requireContext())

            val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            val desaid = sh.getString("desaid", null)
            if (desaid.isNullOrEmpty()) {
                Loading.hide()
                showDialog("Gagal", "desaid tidak ditemukan")
                return
            }

            val response = Client.retrofit.create(Data::class.java).sendNewLahan(newLahan(
                owner_id = DataOwner[binding.spPemilik.selectedItemPosition-1]?.kode.toString(),
                type = binding.spJenisLahan.selectedItem.toString(),
                luas = binding.etLuas.text.toString(),
                latitude = binding.etLatitude.text.toString(),
                longitude = binding.etLongitude.text.toString(),
            ))
            Loading.hide()
            if (response.kode == 201) {
                showDialog("Berhasil", response.msg)
            } else {
                showDialog("Gagal", response.msg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Loading.hide()
            Log.e("NEW_OWNER", e.toString())
            showDialog("Gagal", "Terjadi kesalahan: ${e.message}")
        }
    }

    private suspend fun loadOwner(s: String) {
        try {
            val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            val desaid = sh.getString("desaid", null)
            if (desaid.isNullOrEmpty()) {
                Loading.hide()
                showDialog("Gagal", "desaid tidak ditemukan")
                return
            }
            val response = Client.retrofit.create(Data::class.java).getOwner(desaid)

            if (response.isNullOrEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Peringatan")
                    .setCancelable(false)
                    .setMessage("Anda belum memiliki pemilik lahan, silahkan melakukan pendaftaran pemilik lahan baru atau hubungi Admin Polres Anda")
                    .setPositiveButton("OK") { dialog, _ ->
                        findNavController().popBackStack()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        findNavController().popBackStack()
                        dialog.dismiss()
                    }
                    .show()
            } else {
                resetField()
                val spPemilik = binding.spPemilik

                val pemilikList = mutableListOf("PILIH")
                DataOwner.addAll(response)
                pemilikList.addAll(response.map { "${it.namaPok} - ${it.nama}" })
                spPemilik.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, pemilikList)

                binding.LLDataPemilik.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }
    }


    private fun resetField() {
        binding.apply {
            etLuas.error = null
            etLuas.text = null
            etLatitude.error = null
            etLatitude.text = null
            etLongitude.error = null
            etLongitude.text = null
            spPemilik.setSelection(0)
        }
    }

    private fun startValidate(): Boolean {
        var isValid = true
        val coordRegex = "^-?\\d*\\.?\\d+$".toRegex()
        val luaslahan = binding.etLuas.text.toString()
        val latitude = binding.etLatitude.text.toString()
        val longitude = binding.etLongitude.text.toString()
        if (luaslahan.isEmpty()) {
            binding.etLuas.error = "Luas lahan tidak boleh kosong"
            isValid = false
        }
        if (latitude.isEmpty()) {
            binding.etLatitude.error = "Latitude tidak boleh kosong"
            isValid = false
        } else if (latitude.contains(",") || latitude.contains(" ")) {
            binding.etLatitude.error = "Latitude tidak boleh mengandung koma atau spasi"
            isValid = false
        } else if (!latitude.matches(coordRegex)) {
            binding.etLatitude.error = "Latitude hanya boleh berisi angka, titik, dan tanda negatif"
            isValid = false
        }
        if (longitude.isEmpty()) {
            binding.etLongitude.error = "Longitude tidak boleh kosong"
            isValid = false
        } else if (longitude.contains(",") || longitude.contains(" ")) {
            binding.etLongitude.error = "Longitude tidak boleh mengandung koma atau spasi"
            isValid = false
        } else if (!longitude.matches(coordRegex)) {
            binding.etLongitude.error = "Longitude hanya boleh berisi angka, titik, dan tanda negatif"
            isValid = false
        }

        return isValid
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack()
            }
            .show()
    }

}