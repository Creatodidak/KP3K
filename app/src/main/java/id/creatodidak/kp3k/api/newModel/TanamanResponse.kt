package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class TanamanResponse(

	@field:SerializedName("TanamanResponse")
	val tanamanResponse: List<TanamanResponseItem?>? = null
)

data class TanamanResponseItem(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("sumber")
	val sumber: String? = null,

	@field:SerializedName("rencanatanggalpanen")
	val rencanatanggalpanen: String? = null,

	@field:SerializedName("lahan_id")
	val lahanId: Int? = null,

	@field:SerializedName("updateAt")
	val updateAt: String? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: String? = null,

	@field:SerializedName("createAt")
	val createAt: String? = null,

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

	@field:SerializedName("keteranganSumber")
	val keteranganSumber: String? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("submitter")
	val submitter: String? = null,

	@field:SerializedName("tanamanke")
	val tanamanke: String? = null
)
