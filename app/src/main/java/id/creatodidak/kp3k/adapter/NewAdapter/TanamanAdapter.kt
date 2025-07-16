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
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity

class TanamanAdapter(
    private val data: List<NewTanamanEntity>,
    private val onRincianClick: (NewTanamanEntity) -> Unit,
) : RecyclerView.Adapter<TanamanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvLahanWithOwner = itemView.findViewById<TextView>(R.id.tvLahanWithOwner)
        val tvTypeLahan = itemView.findViewById<TextView>(R.id.tvTypeLahan)
        val tvLuasLahan = itemView.findViewById<TextView>(R.id.tvLuasLahan)
        val tvLuasTanam = itemView.findViewById<TextView>(R.id.tvLuasTanam)
        val tvPersentaseTanam = itemView.findViewById<TextView>(R.id.tvPersentaseTanam)
        val tvProduksi = itemView.findViewById<TextView>(R.id.tvProduksi)
        val tvPersenProduksi = itemView.findViewById<TextView>(R.id.tvPersenProduksi)
        val btRincian = itemView.findViewById<LinearLayout>(R.id.btRincian)
        val cvData = itemView.findViewById<View>(R.id.cvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_tanaman_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tanaman = data[position]
        val layoutParams = holder.cvData.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            layoutParams.topMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, holder.itemView.context.resources.displayMetrics
            ).toInt()
        } else {
            layoutParams.topMargin = 0
        }
        holder.tvTitle.text = tanaman.showCaseName
        holder.tvLahanWithOwner.text = tanaman.lahanWithOwner
        holder.tvTypeLahan.text = "Tanaman Pada Lahan ${tanaman.typeLahan.name}"
        holder.tvLuasLahan.text = "Luas Lahan: ${tanaman.luasLahan} m²/${angkaIndonesia(convertToHektar(tanaman.luasLahan.toDouble()))}Ha"
        holder.tvLuasTanam.text = "Luas Tanam: ${tanaman.luastanam} m²/${angkaIndonesia(convertToHektar(tanaman.luastanam.toDouble()))}Ha"

        // Persentase Tanam
        val luasLahan = tanaman.luasLahan.toDoubleOrNull() ?: 0.0
        val luasTanam = tanaman.luastanam.toDoubleOrNull() ?: 0.0
        val prediksiPanen = tanaman.prediksipanen.toDoubleOrNull() ?: 0.0
        val jumlahPanen = tanaman.jumlahPanen?.toDoubleOrNull() ?: 0.0

        val persenTanam = if (luasLahan > 0) (luasTanam / luasLahan) * 100 else 0.0
        val persenPanen = if(jumlahPanen > 0) (jumlahPanen/prediksiPanen) * 100 else 0.0
        holder.tvPersentaseTanam.text = "Memenuhi ${angkaIndonesia(persenTanam)}% Luas Lahan"
        if(jumlahPanen>0){
            holder.tvProduksi.text = "Jumlah Panen: ${angkaIndonesia(jumlahPanen)}KG/${angkaIndonesia(convertToTon(jumlahPanen))}Ton"
            holder.tvPersenProduksi.text = "Persentase Capaian Panen: ${angkaIndonesia(persenPanen)}% dari Target Panen ${angkaIndonesia(prediksiPanen)}KG"
        }else{
            holder.tvProduksi.visibility = View.GONE
            holder.tvPersenProduksi.visibility = View.GONE
        }
        holder.btRincian.setOnClickListener {
            onRincianClick(tanaman)
        }
    }
}
