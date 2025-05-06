package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahanOwnerItem
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class ListLahanOwnerAdapter(
    private val data: List<LahanOwnerItem?>,
    private val onMapClick: (String) -> Unit,
    private val onWrapperClick: (String) -> Unit
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


        val totalLuasTanam = item?.datatanam?.sumOf {
            it?.luastanam?.toDoubleOrNull() ?: 0.0
        } ?: 0.0

        val luasLahan = luas.toDoubleOrNull() ?: 0.0
        val persenTanam = if (luasLahan != 0.0) (totalLuasTanam / luasLahan) * 100 else 0.0
        b.tvProgressTanam.text = "Progress Tanam ${formatDuaDesimal(totalLuasTanam / 10000)} Ha / ${formatDuaDesimal(luasLahan / 10000)} Ha (${formatDuaDesimal(persenTanam)}%)"

        if (item?.ownerId?.contains("TS") == true) {
            Glide.with(b.root.context)
                .load(R.drawable.multikultur)
                .into(b.ivJenisLahan)
            b.tvJenisLahan.text = "TUMPANG SARI"

        }else{
            b.tvJenisLahan.text = "MONOKULTUR"
            Glide.with(b.root.context)
                .load(R.drawable.monokultur)
                .into(b.ivJenisLahan)
        }

        b.tvAlamatLahan.text = alamat

        b.tvShowOnMap.setOnClickListener {
            val coords = "${item?.latitude}||${item?.longitude}"
            onMapClick(coords)
        }
        b.wrapperLahan.setOnClickListener {
            val idlahan = item?.kode
            val luas = luasMeter
            val urutan = (position+1).toString()
            val tertanam = totalLuasTanam
            val senddata = "${idlahan}|${luas}|${tertanam}|${urutan}"
            onWrapperClick(senddata)
        }

    }

    override fun getItemCount(): Int = data.size
}
