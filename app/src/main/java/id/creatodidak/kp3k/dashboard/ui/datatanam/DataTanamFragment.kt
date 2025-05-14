package id.creatodidak.kp3k.dashboard.ui.datatanam

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.pemiliklahan.DataTanamAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.DatatanamItem
import id.creatodidak.kp3k.api.model.MRealisasiItem
import id.creatodidak.kp3k.dashboard.ui.pemiliklahan.ListLahanPemilikLahanFragmentArgs
import id.creatodidak.kp3k.databinding.FragmentDataTanamBinding
import id.creatodidak.kp3k.helper.CameraActivity
import id.creatodidak.kp3k.helper.formatDuaDesimal
import kotlinx.coroutines.launch

class DataTanamFragment : Fragment() {
    private var _binding: FragmentDataTanamBinding? = null
    private val binding get() = _binding!!
    var data = mutableListOf<MRealisasiItem?>()
    private lateinit var adapter : DataTanamAdapter
    private val args: DataTanamFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDataTanamBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.floatingActionButton.setOnClickListener {
            val action = DataTanamFragmentDirections.actionDataTanamFragmentToTambahDataTanamFragment(args.lahanId)
            findNavController().navigate(action)
        }

        binding.tvIdentitasLahan.text = "DATA TANAM LAHAN ${args.pemilik}"

        val tertanam = args.tertanam.toFloat() / 10000
        val luasLahan = args.luastotal.toFloat() / 10000
        binding.tvLuasTanamTotal.text = "${formatDuaDesimal(tertanam.toDouble())}Ha"
        binding.tvLuasLahanTotal.text = "${formatDuaDesimal(luasLahan.toDouble())}Ha"
        lifecycleScope.launch {
            loadLahan(args.lahanId)
        }
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadLahan(s: String){
        try {
            val result = Client.retrofit.create(Data::class.java).getDataTanamOnLahan(s)
            if(!result.isNullOrEmpty()){
                data.clear()
                data.addAll(result)
                adapter = DataTanamAdapter(data,
                    onWrapperClick = {data ->
                        val sData = data.split("|")
                        val action = DataTanamFragmentDirections.actionDataTanamFragmentToDataPerkembanganTanamanFragment(sData[0], sData[1], args.pemilik, sData[2], args.lahanId)
                        findNavController().navigate(action)
                    },
                    onPanenClick = {data ->
                        val sData = data.split("|")
                        val action = DataTanamFragmentDirections.actionDataTanamFragmentToPanenFragment(sData[0], sData[1], args.pemilik, sData[2], args.lahanId, sData[3])
                        findNavController().navigate(action)
                    },
                    onRevisiClick = {data ->
                        val action = DataTanamFragmentDirections.actionDataTanamFragmentToRevisiDataTanamFragment(data)
                        findNavController().navigate(action)
                    }

                )
                binding.rvDataTanam.adapter = adapter
                binding.rvDataTanam.layoutManager = LinearLayoutManager(requireContext())
                adapter.notifyDataSetChanged()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}