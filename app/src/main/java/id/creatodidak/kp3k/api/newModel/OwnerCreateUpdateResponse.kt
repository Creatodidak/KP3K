package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class OwnerCreateUpdateResponse(
	@field:SerializedName("msg")
	val msg: String,

	@field:SerializedName("data")
	val data: OwnerResponseItem? = null,

	@field:SerializedName("error")
	val error: String? = null
)
