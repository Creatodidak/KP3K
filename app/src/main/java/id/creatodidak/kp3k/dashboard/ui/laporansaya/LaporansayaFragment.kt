package id.creatodidak.kp3k.dashboard.ui.laporansaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentLaporansayaBinding
import androidx.navigation.fragment.findNavController
class LaporansayaFragment : Fragment() {

    private var _binding: FragmentLaporansayaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(LaporansayaViewModel::class.java)

        _binding = FragmentLaporansayaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fabAddLaporan.setOnClickListener {
            findNavController().navigate(R.id.pilihProgramLaporan)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}