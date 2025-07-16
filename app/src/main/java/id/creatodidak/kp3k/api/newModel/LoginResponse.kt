package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null
)

data class NewLoginRequest(
	@SerializedName("nrp") val nrp: String,
	@SerializedName("password") val password: String,
)
