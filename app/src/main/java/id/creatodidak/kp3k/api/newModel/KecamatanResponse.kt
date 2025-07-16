package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class KecamatanResponse(

	@field:SerializedName("KecamatanResponse")
	val kecamatanResponse: List<KecamatanResponseItem?>? = null
)

data class KecamatanResponseItem(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kabupatenId")
	val kabupatenId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
