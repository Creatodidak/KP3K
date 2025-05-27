package id.creatodidak.kp3k.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.api.model.RAtensiItem
import id.creatodidak.kp3k.dashboard.ui.home.ambilIdTerbaca
import id.creatodidak.kp3k.databinding.ItemAtensiBinding

class AtensiAdapter(
    private val onReadClicked: (id: Int) -> Unit
) : ListAdapter<RAtensiItem, AtensiAdapter.AtensiViewHolder>(DIFF_CALLBACK) {

    inner class AtensiViewHolder(val binding: ItemAtensiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtensiViewHolder {
        val binding = ItemAtensiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AtensiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AtensiViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding

        // Null-safe binding
        b.tvJudul.text = item.judul.orEmpty()
        b.tvIsi.text = item.isi.orEmpty()
        b.tvPengirim.text = "Dari: ${item.jabatan.orEmpty()}"

        val context = b.root.context
        val readSet = ambilIdTerbaca(context)
        val isRead = item.id?.toString() in readSet

        b.btnReaded.visibility = if (isRead) View.GONE else View.VISIBLE

        b.btnReaded.setOnClickListener {
            item.id?.let { onReadClicked(it) } // only call if ID is not null
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RAtensiItem>() {
            override fun areItemsTheSame(oldItem: RAtensiItem, newItem: RAtensiItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RAtensiItem, newItem: RAtensiItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
