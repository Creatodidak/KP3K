package id.creatodidak.kp3k.dashboard.ui.laporansaya.prog2a

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.databinding.FragmentListLaporanProg2aBinding


class ListLaporanProg2aFragment : Fragment() {

    private var _binding: FragmentListLaporanProg2aBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListLaporanProg2aBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val args: ListLaporanProg2aFragmentArgs by navArgs()
        binding.infoLahan.text = args.namaLahan

        aturVisibilitas(args.lastprogress)

        return root
    }

    fun aturVisibilitas(lastProgress: Int) {
        when (lastProgress) {
            1 -> {
                binding.penanamanbaru.visibility = View.GONE
                binding.perkembangantanaman.visibility = View.GONE
            }
            2 -> {
                binding.progressLahan.visibility = View.GONE
                binding.perkembangantanaman.visibility = View.GONE
            }
            3 -> {
                binding.progressLahan.visibility = View.GONE
            }
            else -> {
                // Default: semua tampil
                binding.progressLahan.visibility = View.VISIBLE
                binding.penanamanbaru.visibility = View.VISIBLE
                binding.perkembangantanaman.visibility = View.VISIBLE
            }
        }
    }

}