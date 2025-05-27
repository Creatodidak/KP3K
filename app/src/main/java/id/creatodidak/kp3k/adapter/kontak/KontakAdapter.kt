package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.api.model.pimpinan.DataKontak
import id.creatodidak.kp3k.databinding.KontakLayoutBinding

class KontakAdapter(
    private val data: List<DataKontak>,
    private val onDataClick: (String) -> Unit,
) : RecyclerView.Adapter<KontakAdapter.DataKontakViewHolder>() {

    inner class DataKontakViewHolder(val binding: KontakLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataKontakViewHolder {
        val binding = KontakLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataKontakViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataKontakViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        b.tvKontakName.text = item.nama ?: "-"
        b.tvKontakJabatan.text = item.jabatan ?: "-"

        b.lyKontakAct.setOnClickListener {
            val sendData = "${item.target}/${item.nama}"
            onDataClick(sendData)
        }
    }

    override fun getItemCount(): Int = data.size
}
