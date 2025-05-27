package id.creatodidak.kp3k.adapter.pimpinan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.api.model.pimpinan.KabupatenSummaryByMasaTanam
import id.creatodidak.kp3k.databinding.ItemDataMonitoringBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal
import id.creatodidak.kp3k.helper.formatDuaDesimalKoma

class KabupatenAdapter(
    private val data: List<KabupatenSummaryByMasaTanam>
) : RecyclerView.Adapter<KabupatenAdapter.KabupatenViewHolder>() {

    inner class KabupatenViewHolder(val binding: ItemDataMonitoringBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KabupatenViewHolder {
        val binding = ItemDataMonitoringBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KabupatenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KabupatenViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvNamaKab.text = item.nama
        holder.binding.tvJumlahOwner.text = "${formatDuaDesimalKoma(item.towner.toDouble())} Org"
        holder.binding.tvJumlahLahan.text = "${formatDuaDesimalKoma(item.tlahan.toDouble())} Titik"
        holder.binding.tvLuasLahan.text = "${formatDuaDesimalKoma(item.tluaslahan / 10000.0)} Ha"
        holder.binding.tvProduksi.text = "${formatDuaDesimalKoma(item.tproduksi / 1000.0)} Ton"

        val innerAdapter = MasaTanamAdapter(item.data.toList(), item.tluaslahan)
        holder.binding.rvData.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.binding.rvData.adapter = innerAdapter
    }

    override fun getItemCount(): Int = data.size
}
