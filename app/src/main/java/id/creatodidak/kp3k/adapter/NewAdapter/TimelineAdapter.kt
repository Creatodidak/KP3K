package id.creatodidak.kp3k.adapter.NewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.newModel.PerkembanganResponseItem
import id.creatodidak.kp3k.database.Entity.PerkembanganEntity
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.toIsoString
import java.util.Locale
class TimelineAdapter(
    private val perkembanganList: List<PerkembanganEntity>,
    private val onCallClick: (PerkembanganEntity) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvFirst: CardView = itemView.findViewById(R.id.cvFirst)
        val firstTvTanggal: TextView = itemView.findViewById(R.id.firstTvTanggal)
        val firstTvTinggiTanaman: TextView = itemView.findViewById(R.id.firstTvTinggiTanaman)
        val firstTvKondisiTanah: TextView = itemView.findViewById(R.id.firstTvKondisiTanah)
        val firstTvCurahHujan: TextView = itemView.findViewById(R.id.firstTvCurahHujan)
        val firstTvPhTanah: TextView = itemView.findViewById(R.id.firstTvPhTanah)
        val firstTvWarnaDaun: TextView = itemView.findViewById(R.id.firstTvWarnaDaun)
        val firstTvKondisiAir: TextView = itemView.findViewById(R.id.firstTvKondisiAir)
        val firstTvPupuk: TextView = itemView.findViewById(R.id.firstTvPupuk)
        val firstTvPestisida: TextView = itemView.findViewById(R.id.firstTvPestisida)
        val firstTvHama: TextView = itemView.findViewById(R.id.firstTvHama)
        val firstTvGangguanAlam: TextView = itemView.findViewById(R.id.firstTvGangguanAlam)
        val firstTvGangguanLainnya: TextView = itemView.findViewById(R.id.firstTvGangguanLainnya)
        val firstTvKeterangan: TextView = itemView.findViewById(R.id.firstTvKeterangan)
        val firstTvRekomendasi: TextView = itemView.findViewById(R.id.firstTvRekomendasi)

        val iv1: ImageView = itemView.findViewById(R.id.iv1)
        val iv2: ImageView = itemView.findViewById(R.id.iv2)
        val iv3: ImageView = itemView.findViewById(R.id.iv3)
        val iv4: ImageView = itemView.findViewById(R.id.iv4)

        // Timeline
        val dotStatus: ImageView = itemView.findViewById(R.id.dotStatus)

        // Next layout section
        val lyNexts: LinearLayout = itemView.findViewById(R.id.lyNexts)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTinggiTanaman: TextView = itemView.findViewById(R.id.tvTinggiTanaman)
        val tvKondisiTanah: TextView = itemView.findViewById(R.id.tvKondisiTanah)
        val tvCurahHujan: TextView = itemView.findViewById(R.id.tvCurahHujan)
        val tvPhTanah: TextView = itemView.findViewById(R.id.tvPhTanah)
        val tvShowRincian: TextView = itemView.findViewById(R.id.tvShowRincian)
        val imgTanaman: ImageView = itemView.findViewById(R.id.imgTanaman)
        val lyHama: LinearLayout = itemView.findViewById(R.id.lyHama)
        val lyGangguanAlam: LinearLayout = itemView.findViewById(R.id.lyGangguanAlam)
        val lyGangguanLainnya: LinearLayout = itemView.findViewById(R.id.lyGangguanLainnya)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val d = perkembanganList[position]

        if(position == 0){
            holder.cvFirst.visibility = View.VISIBLE
            holder.lyNexts.visibility = View.GONE
        }else{
            holder.cvFirst.visibility = View.GONE
            holder.lyNexts.visibility = View.VISIBLE
        }

        holder.tvTanggal.text = formatTanggalKeIndonesia(d.createAt.toIsoString())
        holder.firstTvTanggal.text = formatTanggalKeIndonesia(d.createAt.toIsoString())
        holder.firstTvTinggiTanaman.text = "Tinggi Tanaman: $d.tinggitanaman cm"
        holder.firstTvKondisiTanah.text = "Kondisi Tanah: ${d.kondisitanah}"
        holder.firstTvPhTanah.text = "Ph Tanah: ${d.ph}"
        holder.firstTvWarnaDaun.text = "Warna Daun: ${d.warnadaun}"
        holder.firstTvKondisiAir.text = "Kondisi Air: ${d.kondisiair}"
        holder.firstTvPupuk.text = "Pupuk: ${d.pupuk}"
        holder.firstTvPestisida.text = "Pestisida: ${d.pestisida}"
        holder.firstTvCurahHujan.text = "Curah Hujan: ${d.curahhujan}"

        holder.tvTinggiTanaman.text = "Tinggi Tanaman: ${d.tinggitanaman} cm"
        holder.tvKondisiTanah.text = "Kondisi Tanah: ${d.kondisitanah}"
        holder.tvPhTanah.text = "Ph Tanah: ${d.ph}"
        holder.tvCurahHujan.text = "Curah Hujan: ${d.curahhujan}"

        if(d.hama == "YA"){
            holder.firstTvHama.text = "Tanaman terserang hama dengan rincian: ${d.keteranganhama}"
            holder.lyHama.visibility = View.VISIBLE
        }else{
            holder.firstTvHama.text = "Tanaman tidak terserang hama"
            holder.lyHama.visibility = View.GONE
        }

        if(d.gangguanalam == "YA") {
            holder.firstTvGangguanAlam.text =
                "Tanaman mengalami gangguan alam dengan rincian: ${d.keterangangangguanalam}"
            holder.lyGangguanAlam.visibility
        }else{
            holder.firstTvGangguanAlam.text = "Tidak ada faktor gangguan alam yang mengganggu tanaman"
            holder.lyGangguanAlam.visibility
        }

        if(d.gangguanlainnya == "YA"){
            holder.firstTvGangguanLainnya.text =
                "Tanaman mengalami gangguan lain dengan rincian: ${d.keterangangangguanlainnya}"
            holder.lyGangguanLainnya.visibility
        }else{
            holder.firstTvGangguanLainnya.text = "Tidak ada faktor gangguan lain yang mengganggu tanaman"
            holder.lyGangguanLainnya.visibility
        }

        holder.firstTvKeterangan.text = "Keterangan: ${d.keterangan}"
        holder.firstTvRekomendasi.text = "Rekomendasi: ${d.rekomendasi}"

        holder.tvShowRincian.setOnClickListener {
            onCallClick(d)
        }

        val foto1 = "${BASE_URL}media${d.foto1}"
        val foto2 = "${BASE_URL}media${d.foto2}"
        val foto3 = "${BASE_URL}media${d.foto3}"
        val foto4 = "${BASE_URL}media${d.foto4}"

        Glide.with(holder.itemView.context)
            .load(foto1)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.notfound)
            .into(holder.imgTanaman)

        Glide.with(holder.itemView.context)
            .load(foto1)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.notfound)
            .into(holder.iv1)

        Glide.with(holder.itemView.context)
            .load(foto2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.notfound)
            .into(holder.iv2)

        Glide.with(holder.itemView.context)
            .load(foto3)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.notfound)
            .into(holder.iv3)

        Glide.with(holder.itemView.context)
            .load(foto4)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.notfound)
            .into(holder.iv4)
    }

    override fun getItemCount(): Int = perkembanganList.size
}
