package id.creatodidak.kp3k.dashboard.ui.pemiliklahan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.adapter.pemiliklahan.ListLahanOwnerAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.LahanOwnerItem
import id.creatodidak.kp3k.databinding.FragmentListLahanPemilikLahanBinding
import kotlinx.coroutines.launch


class ListLahanPemilikLahanFragment : Fragment() {
    private var _binding: FragmentListLahanPemilikLahanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListLahanPemilikLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val args: ListLahanPemilikLahanFragmentArgs by navArgs()
        lifecycleScope.launch {
            loadOwnerLahan(args.ownerid, args.pemiliklahan)
        }

        binding.fabAddLahanBaru.setOnClickListener {
            val action = ListLahanPemilikLahanFragmentDirections.actionNavLahamPemilikLahanToNavAddLahan()
            findNavController().navigate(action)
        }
        return root
    }

    private suspend fun loadOwnerLahan(ownerid: String, pemiliklahan: String) {
        try {
            val result = Client.retrofit.create(Data::class.java).getOwnerLahan(ownerid)
            if(result.lahan.isNullOrEmpty()){
                binding.textView13.text = "Belum ada data lahan milik ${result.namaPok}"
            }else{
                binding.textView13.text = "LAHAN MILIK ${result.namaPok}\nTotal ${result.lahan.size} lahan"
                val lahanList = mutableListOf<LahanOwnerItem?>()
                lahanList.addAll(result.lahan)
                binding.rvLahanOwner.adapter = ListLahanOwnerAdapter(lahanList,
                    onMapClick = { coords ->
                        val action = ListLahanPemilikLahanFragmentDirections.showlahanonmap(coords)
                        findNavController().navigate(action)
                    }
                    , onWrapperClick = { idlahan ->
                        val data = idlahan.split("|");
                        val action = ListLahanPemilikLahanFragmentDirections.gotodatatanamfrompemiliklahan(data[0], "KE - ${data[3]} ${pemiliklahan}", data[1], data[2])
                        findNavController().navigate(action)
                    }
                )
                binding.rvLahanOwner.layoutManager = LinearLayoutManager(requireContext())
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}