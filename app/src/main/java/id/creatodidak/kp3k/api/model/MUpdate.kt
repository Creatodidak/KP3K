package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MUpdate(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("filename")
	val filename: String? = null,

	@field:SerializedName("log")
	val log: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null
)
