package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahanOwnerItem
import id.creatodidak.kp3k.api.model.LahanfixItem
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class ListLahanOwnerAdapter(
    private val data: List<LahanfixItem?>,
    private val onMapClick: (String) -> Unit,
    private val onWrapperClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
) : RecyclerView.Adapter<ListLahanOwnerAdapter.LahanViewHolder>() {

    inner class LahanViewHolder(val binding: ItemLahanTugasSayaTumpangsariBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LahanViewHolder {
        val binding = ItemLahanTugasSayaTumpangsariBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LahanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LahanViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        val luas = item?.luas ?: "0"
        val alamat = listOfNotNull(
            item?.desa?.let { "DESA $it" },
            item?.kecamatan?.let { "KECAMATAN $it" },
            item?.kabupaten?.let { "KABUPATEN $it" }
        ).joinToString(", ")


        b.tvNamaPemilik.text = "LAHAN ${position+1}"
        val luasMeter = item?.luas?.toDoubleOrNull() ?: 0.0
        val luasHektar = luasMeter / 10000.0
        b.tvLuasLahan.text = "Luas Lahan ${formatDuaDesimal(luasHektar)} Ha"


        val totalLuasTanam = item?.realisasitanam
            ?.filter { it?.status == "VERIFIED" }
            ?.sumOf { it?.luastanam?.toDoubleOrNull() ?: 0.0 } ?: 0.0


        val luasLahan = luas.toDoubleOrNull() ?: 0.0
        val persenTanam = if (luasLahan != 0.0) (totalLuasTanam / luasLahan) * 100 else 0.0
        b.tvProgressTanam.text = "Progress Tanam ${formatDuaDesimal(totalLuasTanam / 10000)} Ha / ${formatDuaDesimal(luasLahan / 10000)} Ha (${formatDuaDesimal(persenTanam)}%)"

        b.tvJenisLahan.text = item?.type

        b.tvAlamatLahan.text = alamat

        b.tvShowOnMap.setOnClickListener {
            val coords = "${item?.latitude}||${item?.longitude}"
            onMapClick(coords)
        }


        when (item?.status?.uppercase()) {
            "UNVERIFIED" -> {
                b.tvStatusLahan.text = item.status
                b.tvStatusLahan.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_blue_dark))
                b.tvHapusLahan.visibility = View.GONE
                b.wrapperLahan.setOnClickListener {
                    AlertDialog.Builder(b.root.context)
                        .setTitle("Peringatan")
                        .setMessage("Lahan ini belum diverifikasi oleh Admin, silahkan hubungi Admin Polres Anda")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            "VERIFIED" -> {
                b.tvStatusLahan.text = item.status
                b.tvStatusLahan.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark))
                b.tvHapusLahan.visibility = View.GONE
                b.wrapperLahan.setOnClickListener {
                    val idlahan = item.kode
                    val luas = luasMeter
                    val urutan = (position+1).toString()
                    val tertanam = totalLuasTanam
                    val senddata = "${idlahan}|${luas}|${tertanam}|${urutan}"
                    onWrapperClick(senddata)
                }
            }
            "REJECTED" -> {
                b.tvStatusLahan.text = "${item.status}\n${item.alasan}"
                b.tvStatusLahan.setTextColor(ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark))
                b.tvHapusLahan.visibility = View.VISIBLE
                b.wrapperLahan.setOnClickListener {
                    AlertDialog.Builder(b.root.context)
                        .setTitle("Peringatan")
                        .setMessage("Lahan ini telah ditolak oleh Admin")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            else -> {

            }
        }

        b.tvHapusLahan.setOnClickListener {
            AlertDialog.Builder(b.root.context)
                .setTitle("Konfirmasi")
                .setMessage("Apakah Anda yakin ingin menghapus lahan ini?")
                .setPositiveButton("YA, HAPUS") { _, _ ->
                    onDeleteClick(item?.kode ?: "")
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

    }

    override fun getItemCount(): Int = data.size
}
