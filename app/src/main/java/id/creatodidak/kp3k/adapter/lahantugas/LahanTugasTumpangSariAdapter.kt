package id.creatodidak.kp3k.adapter.lahantugas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class LahanTugasTumpangSariAdapter(
    private val data: List<LahantumpangsariItem?>,
    private val onMapClick: (String) -> Unit
) : RecyclerView.Adapter<LahanTugasTumpangSariAdapter.LahanViewHolder>() {

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

        val namaPemilik = item?.ownertumpangsari?.namaPok ?: "-"
        val luas = item?.luas ?: "0"
        val alamat = listOfNotNull(
            item?.desa.let { "DESA $it" },
            item?.kecamatan.let { "KECAMATAN $it" },
            item?.kabupaten.let { "KABUPATEN $it" }
        ).joinToString(", ")
        b.tvJenisLahan.text = "TUMPANG SARI"
        b.tvNamaPemilik.text = "LAHAN MILIK $namaPemilik"
        val luasMeter = item?.luas?.toDoubleOrNull() ?: 0.0
        val luasHektar = luasMeter / 10000.0
        b.tvLuasLahan.text = "Luas Lahan ${formatDuaDesimal(luasHektar)} Ha"


        val totalLuasTanam = item?.dttumpangsari?.sumOf {
            it?.luastanam?.toDoubleOrNull() ?: 0.0
        } ?: 0.0

        val luasLahan = luas.toDoubleOrNull() ?: 0.0
        val persenTanam = if (luasLahan != 0.0) (totalLuasTanam / luasLahan) * 100 else 0.0
        b.tvProgressTanam.text = "Progress Tanam ${formatDuaDesimal(totalLuasTanam / 10000)} Ha / ${formatDuaDesimal(luasLahan / 10000)} Ha (${formatDuaDesimal(persenTanam)}%)"

        Glide.with(b.root.context)
            .load(R.drawable.multikultur)
            .into(b.ivJenisLahan)

        b.tvAlamatLahan.text = alamat

        b.tvShowOnMap.setOnClickListener {
            val coords = "${item?.latitude}||${item?.longitude}"
            onMapClick(coords)
        }
    }

    override fun getItemCount(): Int = data.size
}
