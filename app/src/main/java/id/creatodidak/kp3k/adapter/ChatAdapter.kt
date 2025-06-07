package id.creatodidak.kp3k.adapter.pemiliklahan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.creatodidak.kp3k.api.model.ChatMessage
import id.creatodidak.kp3k.api.model.pimpinan.DataKontak
import id.creatodidak.kp3k.databinding.ChatLayoutBinding
import id.creatodidak.kp3k.databinding.KontakLayoutBinding

class ChatAdapter(
    private val data: List<ChatMessage>,
//    private val onDataClick: (String) -> Unit,
) : RecyclerView.Adapter<ChatAdapter.DataChatViewHolder>() {

    inner class DataChatViewHolder(val binding: ChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataChatViewHolder {
        val binding = ChatLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataChatViewHolder, position: Int) {
        val item = data[position]
        val b = holder.binding

        if (item.isFromBot) {
            b.lyGptChat.visibility = ViewGroup.VISIBLE

            val formattedContent = item.content.replace(Regex("""(?<!^)(?=\d+\.\s)"""), "\n")


            b.tvGptChat.text = if (item.isTyping) {
                "AgriTECH Menjawab..."
            } else {
                formattedContent
            }

            b.lyUserChat.visibility = ViewGroup.GONE
        }
        else{
            b.lyUserChat.visibility = ViewGroup.VISIBLE
            b.tvUserChat.text = item.content
            b.lyGptChat.visibility = ViewGroup.GONE
        }

    }

    override fun getItemCount(): Int = data.size
}
