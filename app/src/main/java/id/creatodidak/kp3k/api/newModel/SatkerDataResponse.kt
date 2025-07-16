package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class SatkerDataResponse(

	@field:SerializedName("SatkerDataResponse")
	val satkerDataResponse: List<SatkerDataResponseItem?>? = null
)

data class SatkerDataResponseItem(

	@field:SerializedName("provinsiId")
	val provinsiId: Int? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("level")
	val level: String? = null,

	@field:SerializedName("kabupatenId")
	val kabupatenId: Int? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("parentId")
	val parentId: Int? = null
)
