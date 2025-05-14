package id.creatodidak.kp3k.dashboard.ui.pemiliklahan

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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.AddOwner
import id.creatodidak.kp3k.databinding.FragmentAddPemilikLahanBinding
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch

class AddPemilikLahanFragment : Fragment() {

    private var _binding : FragmentAddPemilikLahanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPemilikLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val type = listOf("PILIH TYPE", "PRIBADI", "PERUSAHAAN", "POKTAN", "KWT")
        val gapki = listOf("PILIH", "YA", "TIDAK")

        val spType = binding.spType
        val spGapki = binding.spGapki

        spType.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, type)
        spGapki.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, gapki)

        binding.btKirimPemilikLahan.setOnClickListener {
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
        if (!startValidate()) return
        Loading.show(requireContext())

        try {
            val namaPok = binding.etNamaPok.text.toString()
            val nama = binding.etNama.text.toString()
            val nik = binding.etNik.text.toString()
            val alamat = binding.etAlamat.text.toString()
            val telepon = binding.etTelepon.text.toString()
            val type = binding.spType.selectedItem.toString()
            var gapki = ""
            if(!binding.spGapki.selectedItem.toString().equals("YA")){
                gapki = "-"
            }else{
                gapki = binding.spGapki.selectedItem.toString()
            }

            val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            val desaid = sh.getString("desaid", null)
            if (desaid.isNullOrEmpty()) {
                Loading.hide()
                showDialog("Gagal", "Nrp tidak ditemukan")
                return
            }

            val response = Client.retrofit.create(Data::class.java).sendNewPemilikLahan(
                AddOwner(
                    nama = nama,
                    nik = nik,
                    alamat = alamat,
                    telepon = telepon,
                    gapki = gapki,
                    nama_pok = namaPok,
                    desa_id = desaid,
                    type = type
                )
            )

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

    private fun resetField() {
        binding.apply {
            etNamaPok.error = null
            etNamaPok.text = null
            etNama.error = null
            etNama.text = null
            etNik.error = null
            etNik.text = null
            etAlamat.error = null
            etAlamat.text = null
            etTelepon.error = null
            etTelepon.text = null
            spType.setSelection(0)
            spGapki.setSelection(0)
        }
    }


    private fun startValidate(): Boolean {
        var isValid = true

        val namaPok = binding.etNamaPok.text.toString()
        val nama = binding.etNama.text.toString()
        val nik = binding.etNik.text.toString()
        val alamat = binding.etAlamat.text.toString()
        val telepon = binding.etTelepon.text.toString()
        val type = binding.spType.selectedItem.toString()
        val gapki = binding.spGapki.selectedItem.toString()

        if (namaPok.isEmpty()) {
            binding.etNamaPok.error = "Nama tidak boleh kosong"
            isValid = false
        }
        if (nama.isEmpty()) {
            binding.etNama.error = "Nama tidak boleh kosong"
            isValid = false
        }
        if (nik.isEmpty()) {
            binding.etNik.error = "NIK tidak boleh kosong"
            isValid = false
        } else if (nik.length != 16) {
            binding.etNik.error = "NIK Harus 16 Digit!"
            isValid = false
        }
        if (alamat.isEmpty()) {
            binding.etAlamat.error = "Alamat tidak boleh kosong"
            isValid = false
        }
        if (telepon.isEmpty()) {
            binding.etTelepon.error = "Nomor telepon tidak boleh kosong"
            isValid = false
        }
        if (type == "PILIH TYPE") {
            val errorText = binding.spType.selectedView as TextView
            errorText.error = ""
            errorText.setTextColor(Color.RED)
            errorText.text = "Wajib Dipilih"
            isValid = false
        }
        if (gapki == "PILIH") {
            val errorText = binding.spGapki.selectedView as TextView
            errorText.error = ""
            errorText.setTextColor(Color.RED)
            errorText.text = "Wajib Dipilih"
            isValid = false
        }

        return isValid
    }


}