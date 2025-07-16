package id.creatodidak.kp3k.adapter.NewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.database.Entity.OwnerEntity

class OwnerDataAdapter(
    private val owners: List<OwnerEntity>,
    private val onCallClick: (OwnerEntity) -> Unit,
    private val onItemClick: (OwnerEntity) -> Unit,
    private val onEditClick: (OwnerEntity) -> Unit,
    private val onDeleteClick: (OwnerEntity) -> Unit,
    private val onKirimDataKeServerUpdateClick: (OwnerEntity) -> Unit,
    private val onKirimDataKeServerCreateClick: (OwnerEntity) -> Unit
) : RecyclerView.Adapter<OwnerDataAdapter.OwnerViewHolder>() {

    inner class OwnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lyShowOwnerDetail: LinearLayout = view.findViewById(R.id.lyShowOwnerDetail)
        val tvNamaPokOD: TextView = view.findViewById(R.id.tvNamaPokOD)
        val tvNamaOD: TextView = view.findViewById(R.id.tvNamaOD)
        val ivCallOD: ImageView = view.findViewById(R.id.ivCallOD)
        val tvHapusOD: TextView = view.findViewById(R.id.tvHapusOD)
        val tvEditOD: TextView = view.findViewById(R.id.tvEditOD)
        val tvBatalOD: TextView = view.findViewById(R.id.tvBatalOD)
        val lyActionOD: LinearLayout = view.findViewById(R.id.lyActionOD)
        val ivOpenMenuOD: ImageView = view.findViewById(R.id.ivOpenMenuOD)
        val tvStatusOD: TextView = view.findViewById(R.id.tvStatusOD)
        val btKirimDataKeServerOD: Button = view.findViewById(R.id.btKirimDataKeServerOD)
        val ivEditOD: ImageView = view.findViewById(R.id.ivEditOD)
        val btHapusDraftOD: Button = view.findViewById(R.id.btHapusDraftOD)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_owner_list, parent, false) // Ganti dengan nama file layout kamu
        return OwnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: OwnerViewHolder, position: Int) {
        val owner = owners[position]
        holder.tvNamaPokOD.text = owner.nama_pok
        holder.tvNamaOD.text = owner.nama
        holder.lyActionOD.visibility = View.GONE
        holder.ivOpenMenuOD.visibility = View.GONE
        holder.tvStatusOD.visibility = View.VISIBLE
        holder.btKirimDataKeServerOD.visibility = View.GONE
        holder.btHapusDraftOD.visibility = View.GONE

        when (owner.status) {
            "VERIFIED" -> {
                holder.tvStatusOD.visibility = View.GONE
                holder.ivOpenMenuOD.visibility = View.VISIBLE
                holder.ivOpenMenuOD.setOnClickListener {
                    holder.lyActionOD.visibility = View.VISIBLE
                }
                holder.tvBatalOD.setOnClickListener {
                    holder.lyActionOD.visibility = View.GONE
                }

                holder.tvHapusOD.setOnClickListener {
                    holder.lyActionOD.visibility = View.GONE
                    onDeleteClick(owner)
                }

                holder.tvEditOD.setOnClickListener {
                    holder.lyActionOD.visibility = View.GONE
                    onEditClick(owner)
                }

                holder.ivCallOD.visibility = View.VISIBLE
                holder.ivCallOD.setOnClickListener {
                    onCallClick(owner)
                }
            }
            "UNVERIFIED" -> {
                holder.tvStatusOD.visibility = View.VISIBLE
                holder.tvStatusOD.text = "Belum diverifikasi oleh Admin"
            }
            "OFFLINEUPDATE" -> {
                holder.tvStatusOD.text = owner.status
                holder.tvStatusOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.setOnClickListener {
                    onKirimDataKeServerUpdateClick(owner)
                }
                holder.ivCallOD.visibility = View.GONE
                holder.btHapusDraftOD.visibility = View.VISIBLE
                holder.btHapusDraftOD.setOnClickListener {
                    onDeleteClick(owner)
                }
            }
            "OFFLINECREATE" -> {
                holder.tvStatusOD.text = owner.status
                holder.tvStatusOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.setOnClickListener {
                    onKirimDataKeServerCreateClick(owner)
                }
                holder.ivCallOD.visibility = View.GONE
                holder.btHapusDraftOD.visibility = View.VISIBLE
                holder.btHapusDraftOD.setOnClickListener {
                    onDeleteClick(owner)
                }
            }
            "REJECTED" -> {
                holder.tvStatusOD.text = owner.status
                holder.tvStatusOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.visibility = View.VISIBLE
                holder.btKirimDataKeServerOD.setOnClickListener {
                    onKirimDataKeServerUpdateClick(owner)
                }
                holder.ivEditOD.visibility = View.VISIBLE
                holder.ivEditOD.setOnClickListener {
                    onEditClick(owner)
                }
                holder.ivCallOD.visibility = View.GONE
                holder.btHapusDraftOD.visibility = View.VISIBLE
                holder.btHapusDraftOD.setOnClickListener {
                    onDeleteClick(owner)
                }
            }
        }

        holder.tvNamaOD.setOnClickListener {
            onItemClick(owner)
        }
        holder.tvNamaPokOD.setOnClickListener {
            onItemClick(owner)
        }

        holder.lyShowOwnerDetail.setOnClickListener {
            onItemClick(owner)
        }
    }

    override fun getItemCount(): Int = owners.size
}
