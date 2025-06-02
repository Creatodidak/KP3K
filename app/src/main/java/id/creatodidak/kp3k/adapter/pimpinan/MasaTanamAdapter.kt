package id.creatodidak.kp3k.adapter.pimpinan

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.api.model.pimpinan.MasaTanamSummary
import id.creatodidak.kp3k.databinding.ItemDataMonitoringItemBinding
import id.creatodidak.kp3k.helper.formatDuaDesimalKoma

class MasaTanamAdapter(
    private val list: List<Pair<String, MasaTanamSummary>>,
    private val luaslahan: Double
) : RecyclerView.Adapter<MasaTanamAdapter.MasaTanamViewHolder>() {

    inner class MasaTanamViewHolder(val binding: ItemDataMonitoringItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasaTanamViewHolder {
        val binding = ItemDataMonitoringItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MasaTanamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MasaTanamViewHolder, position: Int) {
        val (masa, summary) = list[position]

        holder.binding.tvMasaTanam.text = "Masa Tanam $masa"
        holder.binding.tvLuasTanam.text = "${formatDuaDesimalKoma(  summary.totaltanam / 10000.0)} Ha"
        holder.binding.tvLuasLahan.text = "${formatDuaDesimalKoma(  luaslahan / 10000.0)} Ha"

        val persenTanam = (summary.totaltanam.toDouble() * 100) / luaslahan.coerceAtLeast(1.0)
        holder.binding.pbTanam.max = 100
        holder.binding.pbTanam.progress = persenTanam.toInt().coerceAtMost(100)
        holder.binding.tvPersenTanam.text = formatDuaDesimalKoma( persenTanam )

        holder.binding.tvTargetPanen.text = "${formatDuaDesimalKoma( summary.totaltargetpanen / 1000.0)} Ton"
        holder.binding.tvHasilPanen.text = "${formatDuaDesimalKoma( summary.totalpanen / 1000.0)} Ton"

        val persenPanen = (summary.totalpanen.toDouble() * 100) / summary.totaltargetpanen.coerceAtLeast(1.0)
        holder.binding.pbPanen.max = 100
        holder.binding.pbPanen.progress = persenPanen.toInt().coerceAtMost(100)
        holder.binding.tvPersenPanen.text = formatDuaDesimalKoma( persenPanen)

        val warnaPanen = if (summary.totalpanen > summary.totaltargetpanen) Color.RED else Color.GREEN
        holder.binding.pbPanen.progressDrawable.setColorFilter(warnaPanen, PorterDuff.Mode.SRC_IN)

    }


    override fun getItemCount(): Int = list.size
}
