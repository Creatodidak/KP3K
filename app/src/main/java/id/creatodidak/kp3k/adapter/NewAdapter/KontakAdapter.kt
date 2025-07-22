package id.creatodidak.kp3k.adapter.NewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.R
import id.creatodidak.kp3k.api.newModel.Contact

class KontakAdapter(
    private val contactList: List<Contact>,
    private val onCallClick: (Contact) -> Unit
) : RecyclerView.Adapter<KontakAdapter.KontakViewHolder>() {

    inner class KontakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaKontak)
        val tvJabatan: TextView = itemView.findViewById(R.id.tvJabatanKontak)
        val ivMakeCall: ImageView = itemView.findViewById(R.id.ivMakeCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontakViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ly_contact_list, parent, false)
        return KontakViewHolder(view)
    }

    override fun onBindViewHolder(holder: KontakViewHolder, position: Int) {
        val contact = contactList[position]
        holder.tvNama.text = contact.nama
        holder.tvJabatan.text = contact.jabatan

        holder.ivMakeCall.setOnClickListener {
            onCallClick(contact)
        }
    }

    override fun getItemCount(): Int = contactList.size
}
