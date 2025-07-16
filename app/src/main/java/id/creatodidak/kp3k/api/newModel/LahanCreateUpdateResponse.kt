package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class LahanCreateUpdateResponse(
    @field:SerializedName("msg")
    val msg: String,

    @field:SerializedName("data")
    val data: LahanResponseItem? = null,

    @field:SerializedName("error")
    val error: String? = null
)
