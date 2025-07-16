package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class NotificationRequest(
    @SerializedName("notifications")
    val notifications: List<NotificationItem>
)

data class NotificationItem(
    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("tokens")
    val tokens: List<String>,

    @SerializedName("channel_id")
    val channelId: String,

    @SerializedName("data")
    val data: NotificationData
)

data class NotificationData(
    @SerializedName("type")
    val type: String,

    @SerializedName("meeting_id")
    val meetingId: String,

    @SerializedName("caller_name")
    val callerName: String
)
