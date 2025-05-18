package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class SocketDataPeta(

	@field:SerializedName("dataLahan")
	val dataLahan: List<DataLahanItem?>? = null,

	@field:SerializedName("dataPersonil")
	val dataPersonil: List<DataPersonilItem?>? = null
)

data class SocketLahanfixItems(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("owner_id")
	val ownerId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("luas")
	val luas: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataLahanItem(

	@field:SerializedName("provinsi")
	val provinsi: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("lahanfix")
	val lahanfix: List<SocketLahanfixItems?>? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class DataPersonilItem(

	@field:SerializedName("provinsi")
	val provinsi: String? = null,

	@field:SerializedName("personil")
	val personil: List<PersonilItem?>? = null,

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

data class PersonilItem(

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

	@field:SerializedName("tracking")
	val tracking: TrackingItem? = null,

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
	val status: String? = null,

	@field:SerializedName("sockettoken")
	val token: TokenItem? = null
)

data class TokenItem(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
data class TrackingItem(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("last")
	val last: String? = null,
)

