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
import id.creatodidak.kp3k.newversion.DataPanen.ShowDataPanenByCategory.NewPanenEntity
import java.io.File
import java.util.Locale

class PanenDataDraftVerifikasiAdapter(
    private val panens: List<NewPanenEntity>,
    private val onVerifikasi: (NewPanenEntity) -> Unit,
    private val onDeleteClick: (NewPanenEntity) -> Unit,
    private val onKirimDataKeServerUpdateClick: (NewPanenEntity) -> Unit,
    private val onKirimDataKeServerCreateClick: (NewPanenEntity) -> Unit,
    private val onEdit: (NewPanenEntity) -> Unit,
    private val onDeleteOnServer: (NewPanenEntity) -> Unit,
) : RecyclerView.Adapter<PanenDataDraftVerifikasiAdapter.TanamanViewHolder>() {

    inner class TanamanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvShowcaseName = view.findViewById<TextView>(R.id.tvShowcaseName)
        val tvTanaman = view.findViewById<TextView>(R.id.tvTanaman)
        val tvLahan = view.findViewById<TextView>(R.id.tvLahan)
        val tvTanggalPanen = view.findViewById<TextView>(R.id.tvTanggalPanen)
        val tvLuasPanen = view.findViewById<TextView>(R.id.tvLuasPanen)
        val tvJumlahPanen = view.findViewById<TextView>(R.id.tvJumlahPanen)
        val tvPersentase = view.findViewById<TextView>(R.id.tvPersentase)
        val tvKeteranganPanen = view.findViewById<TextView>(R.id.tvKeteranganPanen)
        val tvCreateAt = view.findViewById<TextView>(R.id.tvCreateAt)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvDiverifikasiPada = view.findViewById<TextView>(R.id.tvDiverifikasiPada)
        val tvKeterangan = view.findViewById<TextView>(R.id.tvKeterangan)
        val tvAnalisa = view.findViewById<TextView>(R.id.tvAnalisa)

        val btTolak = view.findViewById<Button>(R.id.btTolakVerifikasi)
        val btSetujui = view.findViewById<Button>(R.id.btSetujuiVerifikasi)
        val btKirimData = view.findViewById<Button>(R.id.btKirimDataKeServer)
        val btHapusDraft = view.findViewById<Button>(R.id.btHapusDraft)
        val btHapus = view.findViewById<Button>(R.id.btHapus)
        val btEdit = view.findViewById<Button>(R.id.btEdit)

        val lyVerifikasi = view.findViewById<LinearLayout>(R.id.lyVerifikasi)
        val lyDraft = view.findViewById<LinearLayout>(R.id.lyDraft)
        val lyRejected = view.findViewById<LinearLayout>(R.id.lyRejected)

        val iv1: ImageView = view.findViewById(R.id.iv1)
        val iv2: ImageView = view.findViewById(R.id.iv2)
        val iv3: ImageView = view.findViewById(R.id.iv3)
        val iv4: ImageView = view.findViewById(R.id.iv4)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TanamanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_panen_verifikasi_draft_list, parent, false) // Ganti dengan nama file layout kamu
        return TanamanViewHolder(view)
    }

    override fun onBindViewHolder(h: TanamanViewHolder, position: Int) {
        val panen = panens[position]
        val target = panen.tanaman?.prediksipanen?.toDoubleOrNull() ?: 0.0
        val capaian = panen.jumlahpanen.toDoubleOrNull()?: 0.0
        val persentase = (target / capaian) * 100
        val luaspanen = panen.luaspanen.toDoubleOrNull() ?: 0.0

        h.tvShowcaseName.text = panen.showCaseName
        h.tvTanaman.text = "Tanaman Ke - ${panen.tanaman?.tanamanke} Masa Tanam - ${panen.tanaman?.masatanam}"
        h.tvLahan.text = "Lahan milik ${panen.owner?.nama} - ${panen.owner?.nama_pok} (${panen.lahan?.type})"
        h.tvTanggalPanen.text = formatTanggalKeIndonesia(panen.tanggalpanen.toIsoString()).toUpperCase(Locale.ROOT)
        h.tvLuasPanen.text = "${angkaIndonesia(luaspanen)}m2/${angkaIndonesia(convertToHektar(luaspanen))}ha"
        h.tvJumlahPanen.text = "${angkaIndonesia(capaian)}Kg/${angkaIndonesia(convertToTon(capaian))}Ton"
        h.tvPersentase.text = "Tercapai ${angkaIndonesia(persentase)}%"
        h.tvKeteranganPanen.text = panen.keterangan
        h.tvStatus.text = panen.status
        h.tvDiverifikasiPada.text = formatTanggalKeIndonesia(panen.updateAt.toIsoString()).toUpperCase(Locale.ROOT)
        h.tvKeterangan.text = panen.alasan
        h.tvCreateAt.text = formatTanggalKeIndonesia(panen.createAt.toIsoString()).toUpperCase(Locale.ROOT)
        h.tvAnalisa.text = panen.analisa

        if(panen.status == "OFFLINECREATE"){
            val foto1 = File(panen.foto1)
            val foto2 = File(panen.foto2)
            val foto3 = File(panen.foto3)
            val foto4 = File(panen.foto4)

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
            val foto1 = "${BASE_URL}media${panen.foto1}"
            val foto2 = "${BASE_URL}media${panen.foto2}"
            val foto3 = "${BASE_URL}media${panen.foto3}"
            val foto4 = "${BASE_URL}media${panen.foto4}"

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

        when (panen.status) {
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
                        onKirimDataKeServerUpdateClick(panen)
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
                        onKirimDataKeServerCreateClick(panen)
                    }
                }
            }
            "REJECTED" -> {
                h.lyRejected.visibility = View.VISIBLE
                h.lyVerifikasi.visibility = View.GONE
                h.lyDraft.visibility = View.GONE
                h.tvKeterangan.visibility = View.VISIBLE
                h.tvDiverifikasiPada.visibility = View.VISIBLE
                h.tvKeterangan.text = panen.alasan
                h.tvDiverifikasiPada.text = formatTanggalKeIndonesia(panen.updateAt.toIsoString()).toUpperCase(Locale.ROOT)
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
                onDeleteOnServer(panen)
            }
        }
        h.btEdit.setOnClickListener { onEdit(panen) }
        h.btSetujui.setOnClickListener {
            askUser(h.itemView.context, "Konfirmasi", "Anda yakin ingin menerima data ini?"){
                val newData = panen.copy(status = "VERIFIED", alasan = "Disetujui oleh ${getMyNamePangkat(h.itemView.context)}")
                onVerifikasi(newData)
            }
        }
        h.btTolak.setOnClickListener {
            KonfirmasiTolak.show(h.itemView.context,
                onBatal = {},
                onLanjut = {d ->
                    val newData = panen.copy(status = "REJECTED", alasan = "Ditolak oleh ${getMyNamePangkat(h.itemView.context)} karena $d")
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
                onDeleteClick(panen)
            }
        }
    }


    override fun getItemCount(): Int = panens.size
}
