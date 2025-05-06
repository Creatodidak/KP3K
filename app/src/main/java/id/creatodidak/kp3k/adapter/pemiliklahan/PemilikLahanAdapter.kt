package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.api.model.MOwnerItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.databinding.ItemMitraListBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class PemilikLahanAdapter(
    private val data: List<MOwnerItem?>,
    private val onCardClick: (String) -> Unit,
    private val onCallClick: (String) -> Unit,
) : RecyclerView.Adapter<PemilikLahanAdapter.LahanViewHolder>() {

    inner class LahanViewHolder(val binding: ItemMitraListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LahanViewHolder {
        val binding = ItemMitraListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LahanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LahanViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        b.tvOwnerNama.text = item?.nama
        b.tvOwnerNamaKelompok.text = item?.namaPok
        b.tvOwnerNik.text = item?.nik
        b.tvOwnerAlamat.text = item?.alamat
        b.tvOwnerTelepon.text = item?.telepon
        b.tvOwnerStatus.text = item?.status
        b.tvOwnerType.text = item?.type
        val luasHektar = (item?.totalLuas?.toDouble() ?: 0.0) / 10000.0
        b.tvOwnerLuas.text = "${formatDuaDesimal(luasHektar)}Ha"
        b.LLTelepon.setOnClickListener{
            onCallClick(item?.telepon ?: "")
        }
        b.LLLihatLahan.setOnClickListener{
            val id = item?.kode?: ""
            val nama = item?.namaPok ?: ""
            onCardClick("$id|$nama")
        }

    }

    override fun getItemCount(): Int = data.size
}
