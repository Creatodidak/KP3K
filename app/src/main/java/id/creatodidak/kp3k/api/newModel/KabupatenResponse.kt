package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class KabupatenResponse(

	@field:SerializedName("KabupatenResponse")
	val kabupatenResponse: List<KabupatenResponseItem?>? = null
)

data class KabupatenResponseItem(

	@field:SerializedName("provinsiId")
	val provinsiId: Int? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
