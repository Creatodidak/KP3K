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
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.DatabaseInstance
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.helper.formatTanggalKeIndonesia
import id.creatodidak.kp3k.helper.toIsoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class OwnerDataDraftVerifikasiAdapter(
    private val owners: List<OwnerEntity>,
    private val onDisetujui: (OwnerEntity) -> Unit,
    private val onDitolak: (OwnerEntity) -> Unit,
    private val onDeleteClick: (OwnerEntity) -> Unit,
    private val onKirimDataKeServerUpdateClick: (OwnerEntity) -> Unit,
    private val onKirimDataKeServerCreateClick: (OwnerEntity) -> Unit,
    private val onEdit: (OwnerEntity) -> Unit,
    private val onDeleteOnServer: (OwnerEntity) -> Unit,
    private val lifecycleOwner: LifecycleOwner // <- terima di sini
) : RecyclerView.Adapter<OwnerDataDraftVerifikasiAdapter.OwnerViewHolder>() {

    inner class OwnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType = view.findViewById<TextView>(R.id.tvTypeOD)
        val tvGapki = view.findViewById<TextView>(R.id.tvGapkiOD)
        val tvNamaPok = view.findViewById<TextView>(R.id.tvNamaPokOD)
        val tvNamaCp = view.findViewById<TextView>(R.id.tvNamaCpOD)
        val tvNik = view.findViewById<TextView>(R.id.tvNikOD)
        val tvAlamat = view.findViewById<TextView>(R.id.tvAlamatOD)
        val tvTelepon = view.findViewById<TextView>(R.id.tvTeleponOD)
        val tvProvinsi = view.findViewById<TextView>(R.id.tvProvinsiOD)
        val tvKabupaten = view.findViewById<TextView>(R.id.tvKabupatenOD)
        val tvKecamatan = view.findViewById<TextView>(R.id.tvKecamatanOD)
        val tvDesa = view.findViewById<TextView>(R.id.tvDesaOD)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatusOD)
        val tvCreateAt = view.findViewById<TextView>(R.id.tvCreateAtOD)
        val btTolak = view.findViewById<Button>(R.id.btTolakVerifikasiOD)
        val btSetujui = view.findViewById<Button>(R.id.btSetujuiVerifikasiOD)
        val btKirimData = view.findViewById<Button>(R.id.btKirimDataKeServerOD)
        val btHapusDraft = view.findViewById<Button>(R.id.btHapusDraftOD)
        val lyVerifikasi = view.findViewById<LinearLayout>(R.id.lyVerifikasi)
        val lyDraft = view.findViewById<LinearLayout>(R.id.lyDraft)
        val lyRejectedOD = view.findViewById<LinearLayout>(R.id.lyRejectedOD)
        val btHapusOD = view.findViewById<Button>(R.id.btHapusOD)
        val btEdit = view.findViewById<Button>(R.id.btEditOD)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_owner_verifikasi_draft_list, parent, false) // Ganti dengan nama file layout kamu
        return OwnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: OwnerViewHolder, position: Int) {
        val owner = owners[position]
        val db = DatabaseInstance.getDatabase(holder.itemView.context).wilayahDao()

        holder.tvType.text = owner.type.toString()
        holder.tvGapki.text = owner.gapki.toString()
        holder.tvNamaPok.text = owner.nama_pok
        holder.tvNamaCp.text = owner.nama
        holder.tvNik.text = owner.nik
        holder.tvAlamat.text = owner.alamat
        holder.tvTelepon.text = owner.telepon
        lifecycleOwner.lifecycleScope.launch {
            val provinsi =
                withContext(Dispatchers.IO) { db.getProvinsiById(owner.provinsi_id) }
            val kabupaten =
                withContext(Dispatchers.IO) { db.getKabupatenById(owner.kabupaten_id) }
            val kecamatan =
                withContext(Dispatchers.IO) { db.getKecamatanById(owner.kecamatan_id) }
            val desa = withContext(Dispatchers.IO) { db.getDesaById(owner.desa_id) }
            holder.tvProvinsi.text = provinsi.nama
            holder.tvKabupaten.text = kabupaten.nama
            holder.tvKecamatan.text = kecamatan.nama
            holder.tvDesa.text = desa.nama
        }
        holder.tvStatus.text = owner.status
        holder.tvCreateAt.text = formatTanggalKeIndonesia(owner.createAt.toIsoString()).toUpperCase(Locale.ROOT)

        when (owner.status) {
            "UNVERIFIED" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.VISIBLE
                holder.lyDraft.visibility = View.GONE

            }
            "OFFLINEUPDATE" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.VISIBLE
                holder.btKirimData.setOnClickListener { onKirimDataKeServerUpdateClick(owner) }
            }
            "OFFLINECREATE" -> {
                holder.lyRejectedOD.visibility = View.GONE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.VISIBLE
                holder.btKirimData.setOnClickListener { onKirimDataKeServerCreateClick(owner) }
            }
            "REJECTED" -> {
                holder.lyRejectedOD.visibility = View.VISIBLE
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.GONE
            }
            else -> {
                holder.lyVerifikasi.visibility = View.GONE
                holder.lyDraft.visibility = View.GONE
            }
        }

        holder.btHapusOD.setOnClickListener { onDeleteOnServer(owner) }
        holder.btEdit.setOnClickListener { onEdit(owner) }
        holder.btSetujui.setOnClickListener { onDisetujui(owner) }
        holder.btTolak.setOnClickListener { onDitolak(owner) }
        holder.btHapusDraft.setOnClickListener { onDeleteClick(owner) }
    }


    override fun getItemCount(): Int = owners.size
}
