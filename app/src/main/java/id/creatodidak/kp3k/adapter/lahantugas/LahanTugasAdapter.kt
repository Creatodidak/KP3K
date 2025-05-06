package id.creatodidak.kp3k.adapter.lahantugas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.model.LahanItem
import id.creatodidak.kp3k.api.model.LahantumpangsariItem
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaBinding
import id.creatodidak.kp3k.databinding.ItemLahanTugasSayaTumpangsariBinding
import id.creatodidak.kp3k.helper.formatDuaDesimal

class LahanTugasAdapter(
    private val items: List<LahanItem>,
    private val onMapClick: (String) -> Unit,
    private val onCardClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MONOKULTUR = 0
        private const val TYPE_TUMPANGSARI = 1
    }

    inner class MonokulturViewHolder(val binding: ItemLahanTugasSayaBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TumpangsariViewHolder(val binding: ItemLahanTugasSayaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is LahanItem.Monokultur -> TYPE_MONOKULTUR
            is LahanItem.Tumpangsari -> TYPE_TUMPANGSARI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemLahanTugasSayaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            TYPE_MONOKULTUR -> MonokulturViewHolder(binding)
            TYPE_TUMPANGSARI -> TumpangsariViewHolder(binding)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val b = when (holder) {
            is MonokulturViewHolder -> holder.binding
            is TumpangsariViewHolder -> holder.binding
            else -> return
        }

        when (val item = items[position]) {
            is LahanItem.Monokultur -> {
                val data = item.data
                val namaPemilik = data.ownermonokultur?.nama ?: "-"
                val luasMeter = data.luas?.toDoubleOrNull() ?: 0.0
                val luasHektar = luasMeter / 10000.0
                val alamat = listOfNotNull(
                    data.desa.let { "DESA $it" },
                    data.kecamatan.let { "KECAMATAN $it" },
                    data.kabupaten.let { "KABUPATEN $it" }
                ).joinToString(", ")


                b.tvJenisLahan.text = "MONOKULTUR"
                b.tvNamaPemilik.text = "LAHAN MILIK $namaPemilik"
                b.tvLuasLahan.text = "Luas Lahan ${formatDuaDesimal(luasHektar)} Ha"

                val totalTanam = data.dtmonokultur?.sumOf {
                    it?.luastanam?.toDoubleOrNull() ?: 0.0
                } ?: 0.0
                val persen = if (luasMeter > 0) (totalTanam / luasMeter) * 100 else 0.0

                b.tvProgressTanam.text =
                    "Progress Tanam ${formatDuaDesimal(totalTanam / 10000)} Ha / ${formatDuaDesimal(luasMeter / 10000)} Ha (${formatDuaDesimal(persen)}%)"
                b.tvAlamatLahan.text = alamat
                Glide.with(b.root.context).load(R.drawable.monokultur).into(b.ivJenisLahan)

                b.tvShowOnMap.setOnClickListener {
                    val coords = "${data.latitude}||${data.longitude}"
                    onMapClick(coords)
                }

                b.lahanWrapper.setOnClickListener {
                    val idlahan = data.kode
                    val nama = data.ownermonokultur?.namaPok
                    val senddata = "${idlahan}|${nama}|${luasMeter}|${totalTanam}"
                    onCardClick(senddata)
                }
            }

            is LahanItem.Tumpangsari -> {
                val data = item.data
                val namaPemilik = data.ownertumpangsari?.namaPok ?: "-"
                val luasMeter = data.luas?.toDoubleOrNull() ?: 0.0
                val luasHektar = luasMeter / 10000.0
                val alamat = listOfNotNull(data.desa, data.kecamatan, data.kabupaten).joinToString(", ")

                b.tvJenisLahan.text = "TUMPANGSARI"
                b.tvNamaPemilik.text = "LAHAN MILIK $namaPemilik"
                b.tvLuasLahan.text = "Luas Lahan ${formatDuaDesimal(luasHektar)} Ha"

                val totalTanam = data.dttumpangsari?.sumOf {
                    it?.luastanam?.toDoubleOrNull() ?: 0.0
                } ?: 0.0
                val persen = if (luasMeter > 0) (totalTanam / luasMeter) * 100 else 0.0

                b.tvProgressTanam.text =
                    "Progress Tanam ${formatDuaDesimal(totalTanam / 10000)} Ha / ${formatDuaDesimal(luasMeter / 10000)} Ha (${formatDuaDesimal(persen)}%)"
                b.tvAlamatLahan.text = alamat
                Glide.with(b.root.context).load(R.drawable.multikultur).into(b.ivJenisLahan)

                b.tvShowOnMap.setOnClickListener {
                    val coords = "${data.latitude}||${data.longitude}"
                    onMapClick(coords)
                }

                b.lahanWrapper.setOnClickListener {
                    val idlahan = data.kode
                    val nama = data.ownertumpangsari?.namaPok
                    val senddata = "${idlahan}|${nama}|${luasMeter}|${totalTanam}"
                    onCardClick(senddata)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
