package id.creatodidak.kp3k.adapter.lahantugas

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahanmonokulturItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class LahanTugasMonokulturAdapter(
    private val data: List<LahanmonokulturItem?>,
    private val onMapClick: (String) -> Unit
) : RecyclerView.Adapter<LahanTugasMonokulturAdapter.LahanViewHolder>() {

    inner class LahanViewHolder(val binding: ItemLahanTugasSayaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LahanViewHolder {
        val binding = ItemLahanTugasSayaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LahanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LahanViewHolder, position: Int) {
        val item = data[position]

        // Log untuk memeriksa data
        Log.d("ADAPTER_MONO", "Bind pos $position, nama: ${item?.ownermonokultur?.nama}, luas: ${item?.luas}")

        val b = holder.binding
        val namaPemilik = item?.ownermonokultur?.nama ?: "-"
        val luas = item?.luas ?: "0"
        val alamat = listOfNotNull(
            item?.desa.let { "DESA $it" },
            item?.kecamatan.let { "KECAMATAN $it" },
            item?.kabupaten.let { "KABUPATEN $it" }
        ).joinToString(", ")

        b.tvJenisLahan.text = "MONOKULTUR"

        b.tvNamaPemilik.text = "LAHAN MILIK $namaPemilik"
        val luasMeter = item?.luas?.toDoubleOrNull() ?: 0.0
        val luasHektar = luasMeter / 10000.0
        b.tvLuasLahan.text = "Luas Lahan ${formatDuaDesimal(luasHektar)} Ha"

        val totalLuasTanam = item?.dtmonokultur?.sumOf {
            it?.luastanam?.toDoubleOrNull() ?: 0.0
        } ?: 0.0

        val luasLahan = luas.toDoubleOrNull() ?: 0.0
        val persenTanam = if (luasLahan != 0.0 && totalLuasTanam > 0) {
            (totalLuasTanam / luasLahan) * 100
        } else {
            0.0 // Jika tidak ada tanaman atau luas lahan 0
        }

        b.tvProgressTanam.text = "Progress Tanam ${formatDuaDesimal(totalLuasTanam / 10000)} Ha / ${formatDuaDesimal(luasLahan / 10000)} Ha (${formatDuaDesimal(persenTanam)}%)"

        b.tvAlamatLahan.text = alamat
        Glide.with(b.root.context)
            .load(R.drawable.monokultur)
            .into(b.ivJenisLahan)

        b.tvShowOnMap.setOnClickListener {
            val coords = "${item?.latitude}||${item?.longitude}"
            onMapClick(coords)
        }
    }


    override fun getItemCount(): Int = data.size
}
