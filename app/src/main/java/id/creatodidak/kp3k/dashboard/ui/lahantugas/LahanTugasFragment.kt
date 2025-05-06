package id.creatodidak.kp3k.dashboard.ui.lahantugas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.Login
import id.creatodidak.kp3k.adapter.lahantugas.LahanTugasAdapter
import id.creatodidak.kp3k.adapter.lahantugas.LahanTugasMonokulturAdapter
import id.creatodidak.kp3k.adapter.lahantugas.LahanTugasTumpangSariAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.LahanItem
import id.creatodidak.kp3k.databinding.FragmentLahanTugasBinding
import kotlinx.coroutines.launch

class LahanTugasFragment : Fragment() {

    private var _binding: FragmentLahanTugasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLahanTugasBinding.inflate(inflater, container, false)
        val root: View = binding.root
        lifecycleScope.launch {
            loadData()
        }

        binding.fabAddLAporan.setOnClickListener {
            val action = LahanTugasFragmentDirections.addLahanBaru()
            findNavController().navigate(action)
        }
        return root
    }

    private suspend fun loadData() {
        try {
            val sh = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
            val nrp = sh.getString("nrp", "")
            if(nrp === null){
                Toast.makeText(requireContext(), "NRP Tidak Ditemukan! Login Kembali!", Toast.LENGTH_SHORT).show()
                val i = Intent(requireContext(), Login::class.java)
                startActivity(i)
                requireActivity().finish()
            }else{
                val response = Client.retrofit.create(Data::class.java).getMyLahan(nrp)
                val totalMonokultur = response.lahanmonokultur?.size ?: 0
                val totalTumpangsari = response.lahantumpangsari?.size ?: 0
                val total = totalMonokultur + totalTumpangsari
                if (total == 0) {
                    binding.textView31.text = "Belum ada lahan tugas"
                } else {
                    binding.textView31.text = "Total Lahan Tugas: $total Lahan"
                }

                val combinedList = mutableListOf<LahanItem>()

                response.lahanmonokultur?.forEach {
                    if (it != null) combinedList.add(LahanItem.Monokultur(it))
                }

                response.lahantumpangsari?.forEach {
                    if (it != null) combinedList.add(LahanItem.Tumpangsari(it))
                }

                val adapterGabungan = LahanTugasAdapter(
                    combinedList,
                    onMapClick = { koordinat ->
                        val action = LahanTugasFragmentDirections.showlahanonmap(koordinat)
                        findNavController().navigate(action)
                    },
                    onCardClick = { data ->
                        val send = data.split("|")
                        val action = LahanTugasFragmentDirections.gotodatatanamfromplahantugas(send[0], send[1], send[2], send[3])
                        findNavController().navigate(action)
                    }
                )
                binding.rvMonokultur.layoutManager = LinearLayoutManager(requireContext())
                binding.rvMonokultur.adapter = adapterGabungan
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}