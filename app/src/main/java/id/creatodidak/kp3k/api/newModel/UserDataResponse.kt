package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class UserDataResponse(
	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("satkerId")
	val satkerId: Int? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("pangkat")
	val pangkat: String? = null,

	@field:SerializedName("firebase")
	val firebase: Any? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("desaBinaanId")
	val desaBinaanId: Int? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("foto")
	val foto: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("satker")
	val satker: Satker? = null,

	@field:SerializedName("desaBinaan")
	val desaBinaan: DesaBinaan? = null,

	@field:SerializedName("nohp")
	val nohp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

