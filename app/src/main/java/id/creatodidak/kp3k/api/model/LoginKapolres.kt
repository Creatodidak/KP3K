package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class LoginKapolres(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user")
	val user: UserKapolres? = null,

	@field:SerializedName("token")
	val token: String? = null
)

data class UserKapolres(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("polres_id")
	val polresId: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("polres")
	val polres: String? = null,

	@field:SerializedName("penugasan")
	val penugasan: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
