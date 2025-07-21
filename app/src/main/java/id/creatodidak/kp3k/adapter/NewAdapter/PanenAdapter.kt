package id.creatodidak.kp3k.adapter.NewAdapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataPanen.ShowDataPanenByCategory.NewPanenEntity

class PanenAdapter(
    private val data: List<NewPanenEntity>,
    private val onRincianClick: (NewPanenEntity) -> Unit,
) : RecyclerView.Adapter<PanenAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btRincian = itemView.findViewById<LinearLayout>(R.id.btRincian)
        val cvData = itemView.findViewById<View>(R.id.cvData)
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvRincianTanaman = itemView.findViewById<TextView>(R.id.tvRincianTanaman)
        val tvLahanWithOwner = itemView.findViewById<TextView>(R.id.tvLahanWithOwner)
        val tvTypeLahan = itemView.findViewById<TextView>(R.id.tvTypeLahan)
        val tvLuasLahan = itemView.findViewById<TextView>(R.id.tvLuasLahan)
        val tvLuasPanen = itemView.findViewById<TextView>(R.id.tvLuasPanen)
        val tvProduksi = itemView.findViewById<TextView>(R.id.tvProduksi)
        val tvPersenProduksi = itemView.findViewById<TextView>(R.id.tvPersenProduksi)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_panen_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val panen = data[position]
        val layoutParams = h.cvData.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            layoutParams.topMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, h.itemView.context.resources.displayMetrics
            ).toInt()
        } else {
            layoutParams.topMargin = 0
        }
        val totalPanen = panen.jumlahpanen.toDoubleOrNull() ?: 0.0
        val targetPanen = panen.tanaman?.prediksipanen?.toDoubleOrNull() ?: 0.0
        val persentasePanen = if (targetPanen > 0) (totalPanen / targetPanen) * 100 else 0.0
        
        h.tvTitle.text = "${panen.showCaseName}"
        h.tvRincianTanaman.text = "Tanaman Ke - ${panen.tanaman?.tanamanke} Masa Tanam Ke - ${panen.tanaman?.masatanam}"
        h.tvLahanWithOwner.text = "(${panen.lahan?.type?.name}) ${panen.owner?.nama} - ${panen.owner?.nama_pok}"
        h.tvLuasPanen.text = "Luas Panen ${panen.luaspanen}m²/${angkaIndonesia(convertToHektar(panen.luaspanen.toDouble()))}Hektar"
        h.tvProduksi.text = "Total Panen ${panen.jumlahpanen}Kg/${angkaIndonesia(convertToTon(panen.jumlahpanen.toDouble()))}Ton"
        h.tvPersenProduksi.text = "Tercapai ${angkaIndonesia(persentasePanen)}% dari Target Panen ${angkaIndonesia(targetPanen)}Kg"
        h.tvTypeLahan.text = "Dipanen Pada ${formatTanggalKeIndonesia(panen.tanggalpanen.toIsoString())}"
        h.tvLuasLahan.visibility = View.GONE
        h.btRincian.setOnClickListener {
            onRincianClick(panen)
        }
    }
}
