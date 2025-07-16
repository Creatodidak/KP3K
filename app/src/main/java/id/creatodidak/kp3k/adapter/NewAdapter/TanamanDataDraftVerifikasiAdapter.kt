package id.creatodidak.kp3k.adapter.NewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.creatodidak.kp3k.BuildConfig.BASE_URL
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.helper.KonfirmasiTolak
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.convertToTon
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getMyNamePangkat
import id.creatodidak.kp3k.helper.toIsoString
import id.creatodidak.kp3k.newversion.DataTanaman.ShowDataTanamanByCategory.NewTanamanEntity
import java.io.File
import java.util.Locale

class TanamanDataDraftVerifikasiAdapter(
    private val tanamans: List<NewTanamanEntity>,
    private val onVerifikasi: (NewTanamanEntity) -> Unit,
    private val onDeleteClick: (NewTanamanEntity) -> Unit,
    private val onKirimDataKeServerUpdateClick: (NewTanamanEntity) -> Unit,
    private val onKirimDataKeServerCreateClick: (NewTanamanEntity) -> Unit,
    private val onEdit: (NewTanamanEntity) -> Unit,
    private val onDeleteOnServer: (NewTanamanEntity) -> Unit,
) : RecyclerView.Adapter<TanamanDataDraftVerifikasiAdapter.TanamanViewHolder>() {

    inner class TanamanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvCreateAt = view.findViewById<TextView>(R.id.tvCreateAt)
        val btTolak = view.findViewById<Button>(R.id.btTolakVerifikasi)
        val btSetujui = view.findViewById<Button>(R.id.btSetujuiVerifikasi)
        val btKirimData = view.findViewById<Button>(R.id.btKirimDataKeServer)
        val btHapusDraft = view.findViewById<Button>(R.id.btHapusDraft)
        val lyVerifikasi = view.findViewById<LinearLayout>(R.id.lyVerifikasi)
        val lyDraft = view.findViewById<LinearLayout>(R.id.lyDraft)
        val lyRejected = view.findViewById<LinearLayout>(R.id.lyRejected)
        val btHapus = view.findViewById<Button>(R.id.btHapus)
        val btEdit = view.findViewById<Button>(R.id.btEdit)
        val tvLahan: TextView = itemView.findViewById(R.id.tvLahan)
        val tvTypeLahan: TextView = itemView.findViewById(R.id.tvTypeLahan)
        val tvLuasLahan: TextView = itemView.findViewById(R.id.tvLuasLahan)
        val tvTanggalTanam: TextView = itemView.findViewById(R.id.tvTanggalTanam)
        val tvLuasTanam: TextView = itemView.findViewById(R.id.tvLuasTanam)
        val tvPersentaseTanam: TextView = itemView.findViewById(R.id.tvPersentaseTanam)
        val tvMasaTanam: TextView = itemView.findViewById(R.id.tvMasaTanam)
        val tvPrediksiJumlahPanen: TextView = itemView.findViewById(R.id.tvPrediksiJumlahPanen)
        val tvPrediksiTanggalPanen: TextView = itemView.findViewById(R.id.tvPrediksiTanggalPanen)
        val tvVarietasBibit: TextView = itemView.findViewById(R.id.tvVarietasBibit)
        val tvSumberBibit: TextView = itemView.findViewById(R.id.tvSumberBibit)
        val tvKeteranganSumberBibit: TextView = itemView.findViewById(R.id.tvKeteranganSumberBibit)
        val tvShowcaseName: TextView = itemView.findViewById(R.id.tvShowcaseName)
        val iv1: ImageView = itemView.findViewById(R.id.iv1)
        val iv2: ImageView = itemView.findViewById(R.id.iv2)
        val iv3: ImageView = itemView.findViewById(R.id.iv3)
        val iv4: ImageView = itemView.findViewById(R.id.iv4)
        val tvDiverifikasiPada = view.findViewById<TextView>(R.id.tvDiverifikasiPada)
        val tvKeterangan = view.findViewById<TextView>(R.id.tvKeterangan)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TanamanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_tanaman_verifikasi_draft_list, parent, false) // Ganti dengan nama file layout kamu
        return TanamanViewHolder(view)
    }

    override fun onBindViewHolder(h: TanamanViewHolder, position: Int) {
        val tanaman = tanamans[position]
        val luaslahan = tanaman.luasLahan.toDouble()
        val luasTanam = tanaman.luastanam.toDouble()
        val persentaseTanam = (luasTanam / luaslahan) * 100
        h.tvShowcaseName.text = tanaman.showCaseName
        h.tvLahan.text = tanaman.lahanWithOwner
        h.tvTypeLahan.text = tanaman.typeLahan.name
        h.tvLuasLahan.text = "${tanaman.luasLahan}m²/${angkaIndonesia(convertToHektar(luaslahan))}Ha"
        h.tvTanggalTanam.text = formatTanggalKeIndonesia(tanaman.tanggaltanam.toIsoString()).toUpperCase()
        h.tvLuasTanam.text = "${tanaman.luastanam}m²/${angkaIndonesia(convertToHektar(luasTanam))}Ha"
        h.tvPersentaseTanam.text = "${angkaIndonesia(persentaseTanam)}%"
        h.tvMasaTanam.text = "Masa Tanam Ke - ${tanaman.masatanam}"
        h.tvPrediksiJumlahPanen.text = "${tanaman.prediksipanen} Kg/${angkaIndonesia(convertToTon(tanaman.prediksipanen.toDouble()))}Ton"
        h.tvPrediksiTanggalPanen.text = formatTanggalKeIndonesia(tanaman.rencanatanggalpanen.toIsoString()).toUpperCase()
        h.tvVarietasBibit.text = tanaman.varietas
        h.tvSumberBibit.text = tanaman.sumber.name
        h.tvKeteranganSumberBibit.text = tanaman.keteranganSumber
        h.tvStatus.text = tanaman.status
        h.tvCreateAt.text = formatTanggalKeIndonesia(tanaman.createAt.toIsoString()).toUpperCase(Locale.ROOT)

        if(tanaman.status.contains("OFFLINE")){
            val foto1 = File(tanaman.foto1)
            val foto2 = File(tanaman.foto2)
            val foto3 = File(tanaman.foto3)
            val foto4 = File(tanaman.foto4)

            Glide.with(h.itemView.context)
                .load(foto1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.notfound)
                .into(h.iv1)
            Glide.with(h.itemView.context)
                .load(foto2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.notfound)
                .into(h.iv2)
            Glide.with(h.itemView.context)
                .load(foto3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.notfound)
                .into(h.iv3)
            Glide.with(h.itemView.context)
                .load(foto4)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.notfound)
                .into(h.iv4)
        }else{
            val onlineUrl = "${BASE_URL}media"
            val foto1 = onlineUrl + tanaman.foto1
            val foto2 = onlineUrl + tanaman.foto2
            val foto3 = onlineUrl + tanaman.foto3
            val foto4 = onlineUrl + tanaman.foto4
            Glide.with(h.itemView.context)
                .load(foto1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.iv1)
            Glide.with(h.itemView.context)
                .load(foto2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.iv2)
            Glide.with(h.itemView.context)
                .load(foto3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.iv3)
            Glide.with(h.itemView.context)
                .load(foto4)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.iv4)
        }

        when (tanaman.status) {
            "UNVERIFIED" -> {
                h.lyRejected.visibility = View.GONE
                h.lyVerifikasi.visibility = View.VISIBLE
                h.lyDraft.visibility = View.GONE

            }
            "OFFLINEUPDATE" -> {
                h.lyRejected.visibility = View.GONE
                h.lyVerifikasi.visibility = View.GONE
                h.lyDraft.visibility = View.VISIBLE
                h.btHapusDraft.visibility = View.GONE
                h.btKirimData.setOnClickListener {
                    askUser(h.itemView.context, "Konfirmasi", "Anda yakin ingin mengirim data ini ke server?"){
                        onKirimDataKeServerUpdateClick(tanaman)
                    }
                }
            }
            "OFFLINECREATE" -> {
                h.lyRejected.visibility = View.GONE
                h.lyVerifikasi.visibility = View.GONE
                h.lyDraft.visibility = View.VISIBLE
                h.btKirimData.setOnClickListener {
                    askUser(
                        h.itemView.context,
                        "Konfirmasi",
                        "Anda yakin ingin mengirim data ini ke server?"
                    ) {
                        onKirimDataKeServerCreateClick(tanaman)
                    }
                }
            }
            "REJECTED" -> {
                h.lyRejected.visibility = View.VISIBLE
                h.lyVerifikasi.visibility = View.GONE
                h.lyDraft.visibility = View.GONE
                h.tvKeterangan.visibility = View.VISIBLE
                h.tvDiverifikasiPada.visibility = View.VISIBLE
                h.tvKeterangan.text = tanaman.alasan
                h.tvDiverifikasiPada.text = formatTanggalKeIndonesia(tanaman.updateAt.toIsoString()).toUpperCase(Locale.ROOT)
            }
            else -> {
                h.lyVerifikasi.visibility = View.GONE
                h.lyDraft.visibility = View.GONE
            }
        }

        h.btHapus.setOnClickListener {
            askUser(
                h.itemView.context,
                "Konfirmasi",
                "Anda yakin ingin menghapus data ini?"
            ) {
                onDeleteOnServer(tanaman)
            }
        }
        h.btEdit.setOnClickListener { onEdit(tanaman) }
        h.btSetujui.setOnClickListener {
            askUser(h.itemView.context, "Konfirmasi", "Anda yakin ingin menerima data ini?"){
                val newData = tanaman.copy(status = "VERIFIED", alasan = "Disetujui oleh ${getMyNamePangkat(h.itemView.context)}")
                onVerifikasi(newData)
            }
        }
        h.btTolak.setOnClickListener {
            KonfirmasiTolak.show(h.itemView.context,
                onBatal = {},
                onLanjut = {d ->
                    val newData = tanaman.copy(status = "REJECTED", alasan = "Ditolak oleh ${getMyNamePangkat(h.itemView.context)} karena $d")
                    onVerifikasi(newData)
                }
            )
        }
        h.btHapusDraft.setOnClickListener {
            askUser(
                h.itemView.context,
                "Konfirmasi",
                "Anda yakin ingin menghapus data ini?"
            ){
                onDeleteClick(tanaman)
            }
        }
    }


    override fun getItemCount(): Int = tanamans.size
}
