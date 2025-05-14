package id.creatodidak.kp3k.dashboard.ui.pemiliklahan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.adapter.pemiliklahan.ListLahanOwnerAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.LahanOwnerItem
import id.creatodidak.kp3k.api.model.LahanfixItem
import id.creatodidak.kp3k.databinding.FragmentListLahanPemilikLahanBinding
import id.creatodidak.kp3k.helper.Loading
import kotlinx.coroutines.launch


class ListLahanPemilikLahanFragment : Fragment() {
    private var _binding: FragmentListLahanPemilikLahanBinding? = null
    private val binding get() = _binding!!
    val lahanList = mutableListOf<LahanfixItem?>()
    private lateinit var adapter: ListLahanOwnerAdapter
    val args: ListLahanPemilikLahanFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListLahanPemilikLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        lifecycleScope.launch {
            loadOwnerLahan(args.ownerid, args.pemiliklahan)
        }

        binding.fabAddLahanBaru.setOnClickListener {
            val action = ListLahanPemilikLahanFragmentDirections.actionNavLahamPemilikLahanToNavAddLahan()
            findNavController().navigate(action)
        }
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadOwnerLahan(ownerid: String, pemiliklahan: String) {
        try {
            val result = Client.retrofit.create(Data::class.java).getOwnerLahan(ownerid)
            if(result.lahanfix.isNullOrEmpty()){
                binding.textView13.text = "Belum ada data lahan milik ${result.namaPok}"
            }else{
                binding.textView13.text = "LAHAN MILIK ${result.namaPok}\nTotal ${result.lahanfix.size} lahan"
                lahanList.clear()
                lahanList.addAll(result.lahanfix)
                adapter = ListLahanOwnerAdapter(lahanList,
                    onMapClick = { coords ->
                        val action = ListLahanPemilikLahanFragmentDirections.showlahanonmap(coords)
                        findNavController().navigate(action)
                    }
                    , onWrapperClick = { idlahan ->
                        val data = idlahan.split("|");
                        val action = ListLahanPemilikLahanFragmentDirections.gotodatatanamfrompemiliklahan(data[0], "KE - ${data[3]} ${pemiliklahan}", data[1], data[2])
                        findNavController().navigate(action)
                    },
                    onDeleteClick = { idlahan ->
                        lifecycleScope.launch {
                            deleteLahan(idlahan)
                        }
                    }

                )
                binding.rvLahanOwner.adapter = adapter
                binding.rvLahanOwner.layoutManager = LinearLayoutManager(requireContext())

                adapter.notifyDataSetChanged()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private suspend fun deleteLahan(kode: String) {
        try {
            Loading.show(requireContext())
            val result = Client.retrofit.create(Data::class.java).deleteLahan(kode)
            if (result.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menghapus lahan",
                    Toast.LENGTH_SHORT
                ).show()
                loadOwnerLahan(args.ownerid, args.pemiliklahan)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gagal menghapus lahan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("ERROR_DELETE_OWNER", e.toString())
        }finally {
            Loading.hide()
        }
    }
}