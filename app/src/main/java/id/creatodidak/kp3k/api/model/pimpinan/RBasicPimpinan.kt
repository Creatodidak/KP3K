package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class RBasicPimpinan(

	@field:SerializedName("luasLahanTumpangSari")
	val luasLahanTumpangSari: Int? = null,

	@field:SerializedName("jumlahPersonil")
	val jumlahPersonil: Int? = null,

	@field:SerializedName("lahanMono")
	val lahanMono: Int? = null,

	@field:SerializedName("lahantumpangsari")
	val lahantumpangsari: Int? = null,

	@field:SerializedName("luasLahanMono")
	val luasLahanMono: Int? = null,

	@field:SerializedName("jumlahLahan")
	val jumlahLahan: Int? = null,

	@field:SerializedName("pemilikLahan")
	val pemilikLahan: Int? = null
)
