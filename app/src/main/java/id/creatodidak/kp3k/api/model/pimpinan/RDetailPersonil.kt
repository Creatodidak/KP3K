package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class RDetailPersonil(

	@field:SerializedName("polsek")
	val polsek: String? = null,

	@field:SerializedName("sockettoken")
	val sockettoken: Sockettoken? = null,

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

	@field:SerializedName("binaan")
	val binaan: Binaan? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("polda")
	val polda: String? = null,

	@field:SerializedName("foto")
	val foto: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("satker")
	val satker: Satker? = null,

	@field:SerializedName("nohp")
	val nohp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("polres")
	val polres: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Binaan(

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("kec")
	val kec: String? = null,

	@field:SerializedName("prov")
	val prov: String? = null,

	@field:SerializedName("kab")
	val kab: String? = null
)

data class Satker(

	@field:SerializedName("provinsi")
	val provinsi: String? = null,

	@field:SerializedName("polsek")
	val polsek: Any? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null
)

data class Sockettoken(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
