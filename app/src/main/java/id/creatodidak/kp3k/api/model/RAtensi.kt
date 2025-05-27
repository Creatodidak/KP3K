package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class RAtensi(

	@field:SerializedName("RAtensi")
	val rAtensi: List<RAtensiItem?>? = null
)

data class RAtensiItem(

	@field:SerializedName("pengirim")
	val pengirim: String? = null,

	@field:SerializedName("readtimes")
	val readtimes: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("judul")
	val judul: String? = null,
	@field:SerializedName("jabatan")
	val jabatan: String? = null,
	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("isi")
	val isi: String? = null
)

data class getAtensiVal(
	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("kab")
	val kab: String? = null
)

data class addAtensiItem(
	@SerializedName("judul") val judul: String,
	@SerializedName("isi") val isi: String,
	@SerializedName("username") val username: String,
	@SerializedName("jabatan") val jabatan: String,
	@SerializedName("kab") val kab: String? = null,
	@SerializedName("role") val role: String,
)