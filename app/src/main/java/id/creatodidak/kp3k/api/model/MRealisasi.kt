package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MRealisasi(

	@field:SerializedName("MRealisasi")
	val mRealisasi: List<MRealisasiItem?>? = null
)

data class KomoditasNames(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Realisisasipanen(

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("keterangan")
	val keterangan: String? = null,

	@field:SerializedName("jumlahpanen")
	val jumlahpanen: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("tanaman_id")
	val tanamanId: Int? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null
)

data class MRealisasiItem(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("komoditasName")
	val komoditasName: KomoditasNames? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: String? = null,

	@field:SerializedName("realisisasipanen")
	val realisisasipanen: Realisisasipanen? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("masatanam")
	val masatanam: String? = null,

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

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
