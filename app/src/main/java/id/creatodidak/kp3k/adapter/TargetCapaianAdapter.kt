package id.creatodidak.kp3k.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.TargetdancapaianItem

class TargetCapaianAdapter(
    private val dataList: List<TargetdancapaianItem>
) : RecyclerView.Adapter<TargetCapaianAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kabupaten: TextView = view.findViewById(R.id.kabupatenTextView)
        val capaianTanamM: TextView = view.findViewById(R.id.capaianTanamMonokultur)
        val persenTanamM: TextView = view.findViewById(R.id.persenCapaianTanamMonokultur)
        val progressTanamM: ProgressBar = view.findViewById(R.id.pbCapaianTanamMonokultur)

        val capaianPanenM: TextView = view.findViewById(R.id.capaianPanenMonokultur)
        val persenPanenM: TextView = view.findViewById(R.id.aktualisasiPanenMonokultur)
        val progressPanenM: ProgressBar = view.findViewById(R.id.pbCapaianPanenMonokultur)

        val capaianTanamT: TextView = view.findViewById(R.id.capaianTanamTumpangsari)
        val persenTanamT: TextView = view.findViewById(R.id.persenCapaianTanamTumpangsari)
        val progressTanamT: ProgressBar = view.findViewById(R.id.pbCapaianTanamTumpangsari)

        val capaianPanenT: TextView = view.findViewById(R.id.capaianPanenTumpangsari)
        val persenPanenT: TextView = view.findViewById(R.id.aktualisasiPanenTumpangsari)
        val progressPanenT: ProgressBar = view.findViewById(R.id.pbCapaianPanenTumpangsari)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_target_dan_capaian, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        if (item.namaKab == "KOTA PONTIANAK" || item.namaKab == "KOTA SINGKAWANG") {
            holder.kabupaten.text = item.namaKab
        } else {
            holder.kabupaten.text = "KABUPATEN ${item.namaKab}"
        }


        // Monokultur
        val realisasitanamM = item.monokultur.totaltanam / 10000.0  // Ubah ke Double untuk presisi lebih
        val luasM = item.monokultur.luaslahan / 10000.0  // Ubah ke Double
        val targetPM = item.monokultur.totaltargetpanen / 1000.0  // Ubah ke Double
        val realisasiPM = 0.0  // Ubah ke Double

        val persenTM = if (luasM != 0.0) (realisasitanamM / luasM) * 100 else 0.0  // Tambahkan * 100 untuk persen
        val persenPM = if (targetPM != 0.0) (realisasiPM / targetPM) * 100 else 0.0  // Tambahkan * 100 untuk persen

        holder.capaianTanamM.text = "Capaian Tanam ${formatDuaDesimal(realisasitanamM)}Ha/${formatDuaDesimal(luasM)}Ha"
        holder.persenTanamM.text = "Aktualisasi ${"%.2f".format(persenTM)} %"
        holder.progressTanamM.progress = persenTM.toInt()

        holder.capaianPanenM.text = "Capaian Panen ${realisasiPM}Ton/${targetPM}Ton"
        holder.persenPanenM.text = "Aktualisasi ${"%.2f".format(persenPM)} %"
        holder.progressPanenM.progress = persenPM.toInt()

        val realisasitanamTS = item.tumpangsari.totaltanam / 10000.0  // Ubah ke Double untuk presisi lebih
        val luasTS = item.tumpangsari.luaslahan / 10000.0  // Ubah ke Double
        val targetPTS = when (val value = item.tumpangsari.totaltargetpanen) {
            is Number -> value.toDouble() / 1000.0
            is String -> value.toDoubleOrNull()?.div(1000.0) ?: 0.0
            else -> 0.0
        }
        val realisasiPTS = 0.0  // Ubah ke Double

        val persenTTS = if (luasTS != 0.0) (realisasitanamTS / luasTS) * 100 else 0.0  // Tambahkan * 100 untuk persen
        val persenPTS = if (targetPTS != 0.0) (realisasiPTS / targetPTS) * 100 else 0.0  // Tambahkan * 100 untuk persen

        holder.capaianTanamT.text = "Capaian Tanam ${formatDuaDesimal(realisasitanamTS)}Ha/${formatDuaDesimal(luasTS)}Ha"
        holder.persenTanamT.text = "Aktualisasi ${"%.2f".format(persenTTS)} %"
        holder.progressTanamT.progress = persenTTS.toInt()

        holder.capaianPanenT.text = "Capaian Panen ${formatDuaDesimal(realisasiPTS)}Ton/${formatDuaDesimal(targetPTS)}Ton"
        holder.persenPanenT.text = "Aktualisasi ${"%.2f".format(persenPTS)} %"
        holder.progressPanenT.progress = persenPTS.toInt()

    }

    fun formatDuaDesimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            // Bilangan bulat (misalnya: 5.0 jadi "5")
            value.toInt().toString()
        } else {
            // Format ke 2 desimal, lalu hapus 0 jika di ujung
            String.format("%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

}
