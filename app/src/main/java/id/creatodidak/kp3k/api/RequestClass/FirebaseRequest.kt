package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class FirebaseRequest(
    @SerializedName("nrp")
    val nrp: String,

    @SerializedName("token")
    val token: String,
)

