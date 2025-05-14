package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.api.model.MNewOwner
import id.creatodidak.kp3k.api.model.MNewOwnerItem
import id.creatodidak.kp3k.api.model.MOwnerItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.databinding.ItemMitraListBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class PemilikLahanAdapter(
    private val data: List<MNewOwnerItem?>,
    private val onCardClick: (String) -> Unit,
    private val onCallClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
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
        val totalLuas = item?.lahanfix
            ?.filter { it?.status == "VERIFIED" }
            ?.sumOf { it?.luas?.toDoubleOrNull() ?: 0.0 } ?: 0.0


        val luasHektar = totalLuas / 10000.0
        b.tvOwnerLuas.text = "${formatDuaDesimal(luasHektar)}Ha"
        b.LLTelepon.setOnClickListener{
            onCallClick(item?.telepon ?: "")
        }
        b.LLLihatLahan.setOnClickListener{
            val id = item?.kode?: ""
            val nama = item?.namaPok ?: ""
            onCardClick("$id|$nama")
        }

        when (item?.status?.uppercase()) {
            "UNVERIFIED" -> {
                b.tvOwnerStatus.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_blue_dark))
                b.lyActOwner.visibility = View.GONE
                b.tvHapusOwner.visibility = View.GONE
                b.tvAlasanOwner.visibility = View.GONE
            }
            "VERIFIED" -> {
                b.tvOwnerStatus.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark))
                b.lyActOwner.visibility = View.VISIBLE
                b.tvAlasanOwner.visibility = View.GONE
                b.tvHapusOwner.visibility = View.GONE
            }
            "REJECTED" -> {
                b.tvAlasanOwner.text = item.alasan
                b.tvAlasanOwner.visibility = View.VISIBLE
                b.tvHapusOwner.visibility = View.VISIBLE
                b.tvOwnerStatus.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark))
                b.lyActOwner.visibility = View.GONE
            }
            else -> {

            }
        }

        b.tvHapusOwner.setOnClickListener {
            AlertDialog.Builder(b.root.context)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin menghapus pemilik lahan ini?")
                .setPositiveButton("YA, HAPUS") { _, _ ->
                    onDeleteClick(item?.kode ?: "")
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    override fun getItemCount(): Int = data.size
}
