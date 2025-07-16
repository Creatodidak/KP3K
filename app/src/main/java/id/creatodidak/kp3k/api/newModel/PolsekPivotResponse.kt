package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class PolsekPivotResponse(

	@field:SerializedName("PolsekPivotResponse")
	val polsekPivotResponse: List<PolsekPivotResponseItem?>? = null
)

data class PolsekPivotResponseItem(

	@field:SerializedName("satkerId")
	val satkerId: Int? = null,

	@field:SerializedName("kecamatanId")
	val kecamatanId: Int? = null
)
