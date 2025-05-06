package id.creatodidak.kp3k.adapter.pemiliklahan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.MDataPerkembanganItem
import id.creatodidak.kp3k.databinding.ItemPerkembanganTanamanBinding
import id.creatodidak.kp3k.databinding.ItemRealisasiTanamBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal
import id.creatodidak.kp3k.helper.getAgeBetweenDates
import id.creatodidak.kp3k.helper.getAgeFromDate

class DataPerkembanganAdapter(
    private val data: List<MDataPerkembanganItem>,
    private val tanggalTanam: String
) : RecyclerView.Adapter<DataPerkembanganAdapter.DataPerkembanganViewHolder>() {

    inner class DataPerkembanganViewHolder(val binding: ItemPerkembanganTanamanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataPerkembanganViewHolder {
        val binding = ItemPerkembanganTanamanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataPerkembanganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataPerkembanganViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        b.tvDataPerkembanganKe.text = "Data Perkembangan Ke-${position + 1}"
        b.tvTinggiTanaman.text = "${item.tinggitanaman} Cm"
        if(tanggalTanam != "-"){
            b.tvUmurTanam.text = getAgeBetweenDates(item.createAt.toString(), tanggalTanam)
        }else{
            b.tvUmurTanam.text = "Tidak diketahui"
        }
        b.tvKondisiTanah.text = item.kondisitanah.uppercase()
        b.tvWarnaDaun.text = item.warnadaun.uppercase()
        b.tvCurahHujan.text = item.curahhujan.uppercase()
        b.tvSeranganHama.text = item.hama.uppercase()
        Log.d("DEBUG_HAMA", "Hama: ${item.hama}, Keterangan: ${item.keteranganhama}, Tanggal Tanam: $tanggalTanam")
        b.tvKeteranganHama.text = item.keteranganhama.uppercase()
        b.tvKeteranganLainnya.text = item.keterangan
        val fileUrl = "${BuildConfig.BASE_URL}file/"
        if(!item.foto1.isNullOrEmpty()){
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto1))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto1)
        }

        if(!item.foto2.isNullOrEmpty()){
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto2))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto2)
        }

        if(!item.foto3.isNullOrEmpty()) {
            Glide.with(b.root.context)
                .load(fileUrl+url(item.foto3))
                .placeholder(R.drawable.bgpaparan)
                .into(b.ivFoto3)
        }

        if(!item.foto4.isNullOrEmpty()) {
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
