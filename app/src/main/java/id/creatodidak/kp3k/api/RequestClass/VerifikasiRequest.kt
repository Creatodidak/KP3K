package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class VerifikasiRequest(
    @SerializedName("status")
    val status: String,

    @SerializedName("alasan")
    val alasan: String,

    @SerializedName("komoditas")
    val komoditas: String
)