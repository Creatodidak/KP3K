package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MDataTanam(

	@field:SerializedName("data")
	val data: Data? = null
)

data class DatatanamItemx(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("video")
	val video: String? = null,

	@field:SerializedName("komoditasName")
	val komoditasName: KomoditasName? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: String? = null,

	@field:SerializedName("prediksipanen")
	val prediksipanen: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("kodelahan")
	val kodelahan: String? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null
)

data class KomoditasNamex(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Data(

	@field:SerializedName("totalLuasTanam")
	val totalLuasTanam: Int? = null,

	@field:SerializedName("totalPrediksiPanen")
	val totalPrediksiPanen: Int? = null
)

data class ResultItemx(

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

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("datatanam")
	val datatanam: List<DatatanamItem?>? = null,

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
