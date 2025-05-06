package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MRealisasiTanam(

	@field:SerializedName("datatanam")
	val datatanam: List<DatatanamItem?>? = null,

	@field:SerializedName("totalLuasTanam")
	val totalLuasTanam: Int? = null,

	@field:SerializedName("totalPrediksiPanen")
	val totalPrediksiPanen: Int? = null
)

data class KomoditasName(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class DatatanamItem(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("video")
	val video: Any? = null,

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
