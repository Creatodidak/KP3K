package id.creatodidak.kp3k.dashboard.ui.pemiliklahan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.adapter.pemiliklahan.PemilikLahanAdapter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.Data
import id.creatodidak.kp3k.api.model.MOwnerItem
import id.creatodidak.kp3k.databinding.FragmentPemilikLahanBinding
import kotlinx.coroutines.launch

class PemilikLahanFragment : Fragment() {
    private var _binding: FragmentPemilikLahanBinding? = null
    private val binding get() = _binding!!
    private var nrp = ""
    private var ownerList = mutableListOf<MOwnerItem>()
    private lateinit var adapter: PemilikLahanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPemilikLahanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val sh = requireActivity().getSharedPreferences("session", Context.MODE_PRIVATE)
        nrp = sh.getString("nrp", "") ?: ""
        lifecycleScope.launch {
            loadOwner()
        }

        adapter = PemilikLahanAdapter(ownerList,
            onCardClick = {data ->
                val send = data.split("|")
                val action = PemilikLahanFragmentDirections.navLahamPemilikLahanact(send[0], send[1])
                findNavController().navigate(action)
            },
            onCallClick = { telepon ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${telepon}")
                }
                startActivity(intent)
            })

        binding.rvMitra.adapter = adapter
        binding.rvMitra.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddMitra.setOnClickListener {
            val action = PemilikLahanFragmentDirections.navAddPemiliklahanact()
            findNavController().navigate(action)
        }

        return root
    }

    private suspend fun loadOwner() {
        try {
            val result = Client.retrofit.create(Data::class.java).getOwner(nrp)
            ownerList.clear()
            ownerList.addAll(result)
            adapter.notifyDataSetChanged()
            binding.textView22.text = "Total Mitra/Pemilik Lahan : ${result.size}"


        } catch (e: Exception) {
            Log.e("ERROR_LOAD_OWNER", e.toString())
        }
    }


}