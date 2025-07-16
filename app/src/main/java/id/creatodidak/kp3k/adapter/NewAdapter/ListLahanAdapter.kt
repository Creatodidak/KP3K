package id.creatodidak.kp3k.adapter.NewAdapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.newversion.DataLahan.ShowDataLahanByCategory.NewLahanEntity

class ListLahanAdapter(
    private val data: List<NewLahanEntity>,
    private val onRincianClick: (NewLahanEntity) -> Unit,
    private val onMapsClick: (NewLahanEntity) -> Unit
) : RecyclerView.Adapter<ListLahanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvNamaPok: TextView = itemView.findViewById(R.id.tvNamaPok)
        val tvTypeLahan: TextView = itemView.findViewById(R.id.tvTypeLahan)
        val tvLuasLahan: TextView = itemView.findViewById(R.id.tvLuasLahan)
        val tvKoordinat: TextView = itemView.findViewById(R.id.tvKoordinat)
        val btMaps: LinearLayout = itemView.findViewById(R.id.btMaps)
        val btRincian: LinearLayout = itemView.findViewById(R.id.btRincian)
        val cvData: CardView = itemView.findViewById(R.id.cvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_lahan_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lahan = data[position]
        val layoutParams = holder.cvData.layoutParams as ViewGroup.MarginLayoutParams
        if (position == 0) {
            layoutParams.topMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, holder.itemView.context.resources.displayMetrics
            ).toInt()
        } else {
            layoutParams.topMargin = 0
        }
        holder.cvData.layoutParams = layoutParams
        holder.tvTitle.text = lahan.showCaseName
        holder.tvNamaPok.text = "Kelompok: ${lahan.owner_pok}"
        holder.tvTypeLahan.text = "Lahan ${lahan.type.toString().capitalize()}"
        holder.tvLuasLahan.text = "Luas ${angkaIndonesia(convertToHektar(lahan.luas.toDouble()))} Ha"
        holder.tvKoordinat.text = "Koordinat\n${lahan.latitude}, ${lahan.longitude}"

        holder.btMaps.setOnClickListener {
            onMapsClick(lahan)
        }

        holder.btRincian.setOnClickListener {
            onRincianClick(lahan)
        }
    }
}
