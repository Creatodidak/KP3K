package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class DesaResponse(

	@field:SerializedName("DesaResponse")
	val desaResponse: List<DesaResponseItem?>? = null
)

data class DesaResponseItem(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kecamatanId")
	val kecamatanId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
