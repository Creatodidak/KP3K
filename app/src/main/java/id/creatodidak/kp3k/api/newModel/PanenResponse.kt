package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class PanenResponse(

	@field:SerializedName("PanenResponse")
	val panenResponse: List<PanenResponseItem?>? = null
)

data class PanenResponseItem(

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("keterangan")
	val keterangan: String? = null,

	@field:SerializedName("jumlahpanen")
	val jumlahpanen: String? = null,

	@field:SerializedName("tanggalpanen")
	val tanggalpanen: String? = null,

	@field:SerializedName("analisa")
	val analisa: String? = null,

	@field:SerializedName("updateAt")
	val updateAt: String? = null,

	@field:SerializedName("createAt")
	val createAt: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("luaspanen")
	val luaspanen: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("tanaman_id")
	val tanamanId: Int? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("submitter")
	val submitter: String? = null
)
