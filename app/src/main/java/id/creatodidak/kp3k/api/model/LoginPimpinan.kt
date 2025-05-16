package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class LoginPimpinan(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user")
	val user: UserData? = null,

	@field:SerializedName("token")
	val token: String? = null
)

data class UserData(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
