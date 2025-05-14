package id.creatodidak.kp3k.adapter.pemiliklahan

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.DatatanamItem
import id.creatodidak.kp3k.api.model.LahanOwnerItem
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.api.model.MRealisasiItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.databinding.ItemRealisasiTanamBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal
import id.creatodidak.kp3k.helper.getAgeFromDate

class DataTanamAdapter(
    private val data: List<MRealisasiItem?>,
    private val onWrapperClick: (String) -> Unit,
    private val onPanenClick: (String) -> Unit,
    private val onRevisiClick: (String) -> Unit
) : RecyclerView.Adapter<DataTanamAdapter.DataTanamViewHolder>() {

    inner class DataTanamViewHolder(val binding: ItemRealisasiTanamBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataTanamViewHolder {
        val binding = ItemRealisasiTanamBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataTanamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataTanamViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        b.tvDataTanamKe.text = "Penanaman Ke-${position + 1} Masa Tanam ${item?.masatanam}"
        b.tvLuasTanam.text = "${formatDuaDesimal((item?.luastanam?.toDoubleOrNull() ?: 0.0)/10000)} Ha"
        if(item?.tanggaltanam == null){
            b.tvUmurTanam.text = "Tidak diketahui"
        }else{
            b.tvUmurTanam.text = getAgeFromDate(item.tanggaltanam)
        }
        b.tvVarietas.text = item?.varietas ?: "-"
        b.tvKomoditas.text = item?.komoditasName?.nama ?: "-"
        b.tvEstimasi.text = "${formatDuaDesimal((item?.prediksipanen?.toDoubleOrNull() ?: 0.0 )/1000)} Ton"
        val fileUrl = "${BuildConfig.BASE_URL}file/"
        if(!item?.foto1.isNullOrEmpty()){
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto1))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto1)
        }

        b.tvPerkembangan.setOnClickListener {
            val data = "${item?.id}|${position+1}|${item?.tanggaltanam}"
            onWrapperClick(data)
        }

        b.tvPanen.setOnClickListener {
            val data = "${item?.id}|${position+1}|${item?.tanggaltanam}|${item?.masatanam}"
            onPanenClick(data)
        }

        when (item?.status?.uppercase()) {
            "UNVERIFIED" -> {
                b.tvStatusTanam.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_blue_dark))
                b.lyActionTanam.visibility = View.GONE
                b.tvStatusTanam.text = item.status
                b.tvRevisiDataTanam.visibility = View.GONE
            }
            "VERIFIED" -> {
                b.tvStatusTanam.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark))
                b.lyActionTanam.visibility = View.VISIBLE
                b.tvStatusTanam.text = item.status
                b.tvRevisiDataTanam.visibility = View.GONE
                if(item.realisisasipanen == null){
                    b.tvPerkembangan.text = "ISI DATA PERKEMBANGAN"
                    b.tvPanen.text = "ISI DATA PANEN"
                }else{
                    if(item.realisisasipanen.status === "VERIFIED"){
                        b.tvPerkembangan.text = "DATA PERKEMBANGAN"
                        b.tvPanen.text = "DATA PANEN"
                    }else{
                        b.tvPerkembangan.text = "LIHAT DATA PERKEMBANGAN"
                        b.tvPanen.text = "LIHAT DATA PANEN"
                    }
                }
            }
            "REJECTED" -> {
                b.tvStatusTanam.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark))
                b.lyActionTanam.visibility = View.GONE
                b.tvStatusTanam.text = "${item.status} - ${item.alasan}"
                b.tvRevisiDataTanam.visibility = View.VISIBLE
                b.tvRevisiDataTanam.setOnClickListener {
                    val data = "${item.id}"
                    onRevisiClick(data)
                }
            }
            else -> {
                b.tvStatusTanam.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.black))
                b.lyActionTanam.visibility = View.GONE
            }
        }

        if(!item?.foto2.isNullOrEmpty()){
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto2))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto2)
        }

        if(!item?.foto3.isNullOrEmpty()) {
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto3))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto3)
        }

        if(!item?.foto4.isNullOrEmpty()) {
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto4))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto4)
        }
    }

    fun url(fullPath: String): String {
        val keyword = "uploads/"
        val index = fullPath.indexOf(keyword)
        return if (index != -1) {
            fullPath.substring(index + keyword.length)
        } else {
            fullPath // fallback kalau tidak mengandung "uploads/"
        }
    }

    override fun getItemCount(): Int = data.size
}
