package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class PejabatLoginResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("error")
	val error: String? = null,

	@field:SerializedName("data")
	val data: DataPejabat? = null
)

data class DataPejabat(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("satkerId")
	val satkerId: Int? = null,

	@field:SerializedName("satker")
	val satker: Satker? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("wilayah")
	val wilayah: Int? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("username")
	val username: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)