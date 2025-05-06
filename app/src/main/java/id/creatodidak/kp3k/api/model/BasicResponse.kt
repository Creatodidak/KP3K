package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    @SerializedName("kode")
    val kode: Int,

    @SerializedName("msg")
    val msg: String
)
