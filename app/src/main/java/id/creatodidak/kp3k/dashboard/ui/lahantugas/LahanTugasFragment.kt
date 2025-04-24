package id.creatodidak.kp3k.dashboard.ui.lahantugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import id.creatodidak.kp3k.databinding.FragmentLahanTugasBinding

class LahanTugasFragment : Fragment() {

    private var _binding: FragmentLahanTugasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(LahanTugasViewModel::class.java)

        _binding = FragmentLahanTugasBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.showdimap.setOnClickListener {
            val action = LahanTugasFragmentDirections.showlahanonmap("0.37698036645221994||109.94122266998441")
            findNavController().navigate(action)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}