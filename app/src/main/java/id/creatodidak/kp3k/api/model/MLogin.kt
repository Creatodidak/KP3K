package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MLogin(

	@field:SerializedName("kecamatanbinaan")
	val kecamatanbinaan: String? = null,

	@field:SerializedName("polsek")
	val polsek: Polsek? = null,

	@field:SerializedName("kecamatanbinaan_id")
	val kecamatanbinaanId: String? = null,

	@field:SerializedName("desabinaan_id")
	val desabinaanId: String? = null,

	@field:SerializedName("kabupatenbinaan_id")
	val kabupatenbinaanId: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("kabupatenbinaan")
	val kabupatenbinaan: String? = null,

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("polres")
	val polres: Polres? = null,

	@field:SerializedName("token")
	val token: String? = null,

	@field:SerializedName("desabinaan")
	val desabinaan: String? = null
)

data class Polsek(

	@field:SerializedName("nama")
	val nama: String? = null
)

data class User(

	@field:SerializedName("polsek")
	val polsek: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("pangkat")
	val pangkat: String? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("polda")
	val polda: String? = null,

	@field:SerializedName("foto")
	val foto: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("nohp")
	val nohp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("polres")
	val polres: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Polres(

	@field:SerializedName("nama")
	val nama: String? = null
)

data class LoginRequest(
	val username: String,
	val password: String,
	val type: String
)

data class TokenRegister(
	val nrp: String,
	val token: String,
)
data class PINRegister(
	val nrp: String,
	val pin: String,
)