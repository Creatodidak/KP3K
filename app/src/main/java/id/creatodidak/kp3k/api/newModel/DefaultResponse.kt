package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class DefaultResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("error")
	val error: String? = null
)
