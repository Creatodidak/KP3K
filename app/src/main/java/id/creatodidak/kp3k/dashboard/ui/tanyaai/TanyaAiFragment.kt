package id.creatodidak.kp3k.dashboard.ui.tanyaai

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.creatodidak.kp3k.BuildConfig
import id.creatodidak.kp3k.adapter.pemiliklahan.ChatAdapter
import id.creatodidak.kp3k.api.model.ChatMessage
import id.creatodidak.kp3k.databinding.FragmentTanyaAiBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TanyaAiFragment : Fragment() {
    private var _binding: FragmentTanyaAiBinding? = null
    private val binding get() = _binding!!
    private val AI_URL = BuildConfig.AI_URL
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<ChatMessage>()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTanyaAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAccessCodeDialog {
            initChatUI() // hanya jalan kalau akses benar
        }
//        val layoutManager = LinearLayoutManager(requireContext()).apply {
//            stackFromEnd = true
//        }
//
//        binding.rvChat.layoutManager = layoutManager
//        chatAdapter = ChatAdapter(chatList)
//        binding.rvChat.adapter = chatAdapter
//
//        addMessage(ChatMessage("Halo, ada yang bisa saya bantu?", isFromBot = true))
//
//        binding.btSendChat.setOnClickListener {
//            val message = binding.etChatMessage.text.toString().trim()
//            if (message.isNotEmpty()) {
//                addMessage(ChatMessage(message, isFromBot = false))
//                sendMessageToAI()
//                binding.etChatMessage.text.clear()
//            }
//        }
    }

    private fun initChatUI() {
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        binding.rvChat.layoutManager = layoutManager
        chatAdapter = ChatAdapter(chatList)
        binding.rvChat.adapter = chatAdapter

        addMessage(ChatMessage("Halo, ada yang bisa saya bantu?", isFromBot = true))

        binding.btSendChat.setOnClickListener {
            val message = binding.etChatMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(ChatMessage(message, isFromBot = false))
                sendMessageToAI()
                binding.etChatMessage.text.clear()
            }
        }
    }


    private fun showAccessCodeDialog(onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Masukkan Kode Akses")

        val input = EditText(requireContext())
        input.hint = "_ _ _ _"
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setPadding(50, 40, 50, 40)

        builder.setView(input)

        builder.setPositiveButton("Lanjut") { dialog, _ ->
            val enteredCode = input.text.toString().trim()
            val kodeBenar = "AGRITECH"

            if (enteredCode == kodeBenar) {
                onSuccess()
            } else {
                Toast.makeText(requireContext(), "Kode akses salah!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
            findNavController().popBackStack()
        }

        builder.setCancelable(false)
        builder.show()
    }


    private fun addMessage(msg: ChatMessage) {
        chatList.add(msg)
        chatAdapter.notifyItemInserted(chatList.size - 1)
        binding.rvChat.scrollToPosition(chatList.size - 1)
    }

    private fun sendMessageToAI() {
        binding.btSendChat.isEnabled = false

        val messagesArray = JSONArray()
        for (chat in chatList) {
            if (!chat.isTyping && chat.content.isNotBlank()) {
                val role = if (chat.isFromBot) "assistant" else "user"
                messagesArray.put(JSONObject().put("role", role).put("content", chat.content))
            }
        }

        val requestBody = JSONObject().put("messages", messagesArray).toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(AI_URL + "chat")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TANYA_AI", "Gagal kirim ke /chat", e)
                requireActivity().runOnUiThread {
                    binding.btSendChat.isEnabled = true
                    addMessage(ChatMessage("Gagal mengirim ke server.", isFromBot = true))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = JSONObject(body ?: "")
                val jobId = json.optString("jobId")

                Log.d("TANYA_AI", "Dapat jobId: $jobId")

                if (jobId.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val typingMsg = ChatMessage(content = "AgriTECH menganalisa...", isFromBot = true, isTyping = true)
                        chatList.add(typingMsg)
                        chatAdapter.notifyItemInserted(chatList.size - 1)
                        binding.rvChat.scrollToPosition(chatList.size - 1)

                        listenForStream(jobId)
                    }, 1000)
                } else {
                    Log.e("TANYA_AI", "jobId kosong")
                    requireActivity().runOnUiThread {
                        binding.btSendChat.isEnabled = true
                        addMessage(ChatMessage("Gagal mendapatkan jawaban dari AgriTech.", isFromBot = true))
                    }
                }
            }
        })
    }

    private fun listenForStream(jobId: String) {
        val url = "${AI_URL.trimEnd('/')}/stream/$jobId"
        Log.d("TANYA_AI", "SSE connect ke: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        EventSources.createFactory(client).newEventSource(request, object : EventSourceListener() {
            var currentBotMessage: ChatMessage? = null

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Log.d("SSE_RAW", "SSE chunk = ${data.replace("\n", "\\n")}")

                if (data == "[SELESAI]") {
                    requireActivity().runOnUiThread {
                        binding.btSendChat.isEnabled = true
                        currentBotMessage?.let {
                            it.isTyping = false
                            chatAdapter.notifyItemChanged(chatList.lastIndex)
                        }
                    }
                    eventSource.cancel()
                    return
                }

                val patched = data.replace(Regex("""(?<=\d)\."""), ".\n")

                requireActivity().runOnUiThread {
                    if (currentBotMessage == null) {
                        // 🔥 Hapus message yang isTyping
                        val typingIndex = chatList.indexOfLast { it.isTyping }
                        if (typingIndex != -1) {
                            chatList.removeAt(typingIndex)
                            chatAdapter.notifyItemRemoved(typingIndex)
                        }

                        // Tambahkan message baru dari stream
                        currentBotMessage = ChatMessage(content = patched, isFromBot = true)
                        chatList.add(currentBotMessage!!)
                        chatAdapter.notifyItemInserted(chatList.size - 1)
                    }
                    else {
                        currentBotMessage!!.content += patched
                        chatAdapter.notifyItemChanged(chatList.lastIndex)
                    }

                    binding.rvChat.scrollToPosition(chatList.size - 1)
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("SSE", "SSE failed", t)
                requireActivity().runOnUiThread {
                    binding.btSendChat.isEnabled = true
                    addMessage(ChatMessage("Gagal menerima respons dari AgriTech.", isFromBot = true))
                }
                eventSource.cancel()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
