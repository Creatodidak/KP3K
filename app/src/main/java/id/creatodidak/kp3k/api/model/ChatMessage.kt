package id.creatodidak.kp3k.api.model

data class ChatMessage(
    var content: String,
    val isFromBot: Boolean,
    var isTyping: Boolean = false // ⬅️ default false
)

