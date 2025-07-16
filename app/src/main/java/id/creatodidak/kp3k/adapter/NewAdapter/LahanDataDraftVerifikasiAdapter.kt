package id.creatodidak.kp3k.adapter.NewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.copy
import com.bumptech.glide.Glide
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.helper.KonfirmasiTolak
import id.creatodidak.kp3k.helper.angkaIndonesia
import id.creatodidak.kp3k.helper.askUser
import id.creatodidak.kp3k.helper.convertToHektar
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.getMyNamePangkat
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LahanDataDraftVerifikasiAdapter(
    private val lahans: List<LahanEntity>,
    private val onVerifikasi: (LahanEntity) -> Unit,
    private val onDeleteClick: (LahanEntity) -> Unit,
    private val onKirimDataKeServerUpdateClick: (LahanEntity) -> Unit,
    private val onKirimDataKeServerCreateClick: (LahanEntity) -> Unit,
    private val onEdit: (LahanEntity) -> Unit,
    private val onDeleteOnServer: (LahanEntity) -> Unit,
    private val lifecycleOwner: LifecycleOwner // <- terima di sini
) : RecyclerView.Adapter<LahanDataDraftVerifikasiAdapter.lahanViewHolder>() {

    inner class lahanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType = view.findViewById<TextView>(R.id.tvType)
        val tvOwner = view.findViewById<TextView>(R.id.tvOwner)
        val tvLuas = view.findViewById<TextView>(R.id.tvLuas)
        val tvProvinsi = view.findViewById<TextView>(R.id.tvProvinsi)
        val tvKabupaten = view.findViewById<TextView>(R.id.tvKabupaten)
        val tvKecamatan = view.findViewById<TextView>(R.id.tvKecamatan)
        val tvDesa = view.findViewById<TextView>(R.id.tvDesa)
        val tvLatitude = view.findViewById<TextView>(R.id.tvLatitude)
        val tvLongitude = view.findViewById<TextView>(R.id.tvLongitude)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvDiajukanPada = view.findViewById<TextView>(R.id.tvDiajukanPada)
        val btTolak = view.findViewById<Button>(R.id.btTolakVerifikasiOD)
        val btSetujui = view.findViewById<Button>(R.id.btSetujuiVerifikasiOD)
        val btKirimData = view.findViewById<Button>(R.id.btKirimDataKeServerOD)
        val btHapusDraft = view.findViewById<Button>(R.id.btHapusDraftOD)
        val lyVerifikasi = view.findViewById<LinearLayout>(R.id.lyVerifikasi)
        val lyDraft = view.findViewById<LinearLayout>(R.id.lyDraft)
        val lyRejectedOD = view.findViewById<LinearLayout>(R.id.lyRejectedOD)
        val btHapusOD = view.findViewById<Button>(R.id.btHapusOD)
        val btEdit = view.findViewById<Button>(R.id.btEditOD)
        val ivMapPreview = view.findViewById<ImageView>(R.id.ivMapPreview)
        val tvDiverifikasiPada = view.findViewById<TextView>(R.id.tvDiverifikasiPada)
        val tvKeterangan = view.findViewById<TextView>(R.id.tvKeterangan)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): lahanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_lahan_verifikasi_draft_list, parent, false) // Ganti dengan nama file layout kamu
        return lahanViewHolder(view)
    }

    override fun onBindViewHolder(holder: lahanViewHolder, position: Int) {
        val lahan = lahans[position]
        val db = DatabaseInstance.getDatabase(holder.itemView.context)

        holder.tvType.text = lahan.type.toString()
        holder.tvDiverifikasiPada.visibility = View.GONE
        holder.tvKeterangan.visibility = View.GONE

        lifecycleOwner.lifecycleScope.launch {
            val provinsi =
                withContext(Dispatchers.IO) { db.wilayahDao().getProvinsiById(lahan.provinsi_id) }
            val kabupaten =
                withContext(Dispatchers.IO) { db.wilayahDao().getKabupatenById(lahan.kabupaten_id) }
            val kecamatan =
                withContext(Dispatchers.IO) { db.wilayahDao().getKecamatanById(lahan.kecamatan_id) }
            val desa = withContext(Dispatchers.IO) { db.wilayahDao().getDesaById(lahan.desa_id) }
            val owner = withContext(Dispatchers.IO) { db.ownerDao().getOwnerById(lahan.owner_id) }
            holder.tvProvinsi.text = provinsi.nama
            holder.tvKabupaten.text = kabupaten.nama
            holder.tvKecamatan.text = kecamatan.nama
            holder.tvDesa.text = desa.nama
            holder.tvOwner.text = "${owner?.nama} - ${owner?.nama_pok}"
        }
        holder.tvLuas.text = "${angkaIndonesia(lahan.luas.toDouble())} m2 / ${angkaIndonesia(convertToHektar(lahan.luas.toDouble()))} ha"
        holder.tvStatus.text = lahan.status
        holder.tvDiajukanPada.text = formatTanggalKeIndonesia(lahan.createAt.toIsoString()).toUpperCase(Locale.ROOT)
        holder.tvLatitude.text = lahan.latitude
        holder.tvLongitude.text = lahan.longitude

        val urls = "https://maps.googleapis.com/maps/api/staticmap?center=${lahan.latitude},${lahan.longitude}&zoom=14&size=400x300&markers=color:red|${lahan.latitude},${lahan.longitude}&key=AIzaSyBzhus4Xzth17gJxIJjXC1fOD1JYhmIsBo"
        Glide.with(holder.itemView.context)
            .load(urls)
            .placeholder(R.drawable.notfound)
            .error(R.drawable.notfound)
            .into(holder.ivMapPreview)

        when (lahan.status) {
            "UNVERIFIED" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.VISIBLE
                holder.lyDraft.visibility = View.GONE

            }
            "OFFLINEUPDATE" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.VISIBLE
                holder.btHapusDraft.visibility = View.GONE
                holder.btKirimData.setOnClickListener {
                    askUser(holder.itemView.context, "Konfirmasi", "Anda yakin ingin mengirim data ini ke server?"){
                        onKirimDataKeServerUpdateClick(lahan)
                    }
                }
            }
            "OFFLINECREATE" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.VISIBLE
                holder.btKirimData.setOnClickListener {
                    askUser(
                        holder.itemView.context,
                        "Konfirmasi",
                        "Anda yakin ingin mengirim data ini ke server?"
                    ) {
                        onKirimDataKeServerCreateClick(lahan)
                    }
                }
            }
            "REJECTED" -> {
                holder.lyRejectedOD.visibility = View.VISIBLE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.GONE
                holder.tvKeterangan.visibility = View.VISIBLE
                holder.tvDiverifikasiPada.visibility = View.VISIBLE
                holder.tvKeterangan.text = lahan.alasan
                holder.tvDiverifikasiPada.text = formatTanggalKeIndonesia(lahan.updateAt.toIsoString()).toUpperCase(Locale.ROOT)
            }
            else -> {
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.GONE
            }
        }

        holder.btHapusOD.setOnClickListener {
            askUser(
                holder.itemView.context,
                "Konfirmasi",
                "Anda yakin ingin menghapus data ini?"
            ) {
                onDeleteOnServer(lahan)
            }
        }
        holder.btEdit.setOnClickListener { onEdit(lahan) }
        holder.btSetujui.setOnClickListener {
            askUser(holder.itemView.context, "Konfirmasi", "Anda yakin ingin menerima data ini?"){
                val newData = lahan.copy(status = "VERIFIED", alasan = "Disetujui oleh ${getMyNamePangkat(holder.itemView.context)}")
                onVerifikasi(newData)
            }
        }
        holder.btTolak.setOnClickListener {
            KonfirmasiTolak.show(holder.itemView.context,
                onBatal = {},
                onLanjut = {d ->
                    val newData = lahan.copy(status = "REJECTED", alasan = "Ditolak oleh ${getMyNamePangkat(holder.itemView.context)} karena $d")
                    onVerifikasi(newData)
                }
            )
        }
        holder.btHapusDraft.setOnClickListener {
            askUser(
                holder.itemView.context,
                "Konfirmasi",
                "Anda yakin ingin menghapus data ini?"
            ){
                onDeleteClick(lahan)
            }
        }
    }


    override fun getItemCount(): Int = lahans.size
}
