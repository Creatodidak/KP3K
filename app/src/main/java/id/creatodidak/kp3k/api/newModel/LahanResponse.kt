package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class LahanResponse(

	@field:SerializedName("LahanResponse")
	val lahanResponse: List<LahanResponseItem?>? = null
)

data class LahanResponseItem(

	@field:SerializedName("desa_id")
	val desaId: Int? = null,

	@field:SerializedName("owner_id")
	val ownerId: Int? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("luas")
	val luas: String? = null,

	@field:SerializedName("updateAt")
	val updateAt: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: Int? = null,

	@field:SerializedName("createAt")
	val createAt: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: Int? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("submitter")
	val submitter: String? = null,

	@field:SerializedName("lahanke")
	val lahanke: String? = null
)
