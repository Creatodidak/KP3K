package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class ProvinsiResponse(

	@field:SerializedName("ProvinsiResponse")
	val provinsiResponse: List<ProvinsiResponseItem?>? = null
)

data class ProvinsiResponseItem(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
