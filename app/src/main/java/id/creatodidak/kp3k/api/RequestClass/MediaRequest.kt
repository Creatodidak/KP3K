package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class DeleteMediaRequest(
    @SerializedName("urls") val urls: List<String>
)