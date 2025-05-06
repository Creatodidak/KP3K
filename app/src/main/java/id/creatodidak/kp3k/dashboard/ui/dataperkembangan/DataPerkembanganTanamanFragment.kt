package id.creatodidak.kp3k.dashboard.ui.dataperkembangan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.pemiliklahan.DataPerkembanganAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.MDataPerkembanganItem
import id.creatodidak.kp3k.databinding.FragmentDataPerkembanganTanamanBinding
import kotlinx.coroutines.launch

class DataPerkembanganTanamanFragment : Fragment() {
    private lateinit var _binding: FragmentDataPerkembanganTanamanBinding
    private val binding get() = _binding
    private val listPerkembangan = mutableListOf<MDataPerkembanganItem>()
    private lateinit var adapter : DataPerkembanganAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataPerkembanganTanamanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val args : DataPerkembanganTanamanFragmentArgs by navArgs()
        val tanamanid = args.tanamanId
        val urutan = args.urutan
        val pemilik= args.pemilik
        val tanggaltanam = args.tanggaltanam
        val kodelahan = args.kodelahan

        binding.fabAddDataPerkembangan.setOnClickListener {
            val action = DataPerkembanganTanamanFragmentDirections.actionDataPerkembanganTanamanFragmentToAddDataPerkembanganTanamanFragment(tanamanid,urutan,pemilik,tanggaltanam, kodelahan)
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            loadData(kodelahan, tanamanid)
        }

        adapter = DataPerkembanganAdapter(listPerkembangan, tanggaltanam)
        binding.rvPerkembangan.adapter = adapter
        binding.rvPerkembangan.layoutManager = LinearLayoutManager(requireContext())
        return root
    }

    private suspend fun loadData(kodelahan: String, tanamanid: String){
        try {
            val result = Client.retrofit.create(Data::class.java).getDataPerkembangan(kodelahan, tanamanid)
            if (!result.isEmpty()){
                showData(result)
                listPerkembangan.clear()
                listPerkembangan.addAll(result)
                adapter.notifyDataSetChanged()
            }else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Data Kosong")
                    .setMessage("Belum ada data, silahkan menambahkan data Perkembangan Tanaman!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }catch (e: Exception){
            e.printStackTrace()
            AlertDialog.Builder(requireContext())
                .setTitle("Gagal")
                .setMessage("Gagal memuat data")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun showData(items: List<MDataPerkembanganItem>){

    }
}