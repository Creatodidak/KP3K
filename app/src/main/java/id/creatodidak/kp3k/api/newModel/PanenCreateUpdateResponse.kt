package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class PanenCreateUpdateResponse(
    @field:SerializedName("msg")
    val msg: String,

    @field:SerializedName("data")
    val data: PanenResponseItem? = null,

    @field:SerializedName("error")
    val error: String? = null
)
