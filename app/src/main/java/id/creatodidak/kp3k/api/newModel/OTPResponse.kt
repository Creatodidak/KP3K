package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class OTPResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("data")
	val data: Data? = null
)

data class Kecamatan(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kabupatenId")
	val kabupatenId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: Kabupaten? = null
)

data class Provinsi(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Data(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("satkerId")
	val satkerId: Int? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("pangkat")
	val pangkat: String? = null,

	@field:SerializedName("firebase")
	val firebase: String? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("desaBinaanId")
	val desaBinaanId: Int? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("foto")
	val foto: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("satker")
	val satker: Satker? = null,

	@field:SerializedName("desaBinaan")
	val desaBinaan: DesaBinaan? = null,

	@field:SerializedName("nohp")
	val nohp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DesaBinaan(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("kecamatanId")
	val kecamatanId: Int? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: Kecamatan? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Satker(

	@field:SerializedName("parent")
	val parent: Parent? = null,

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

data class Parent(

	@field:SerializedName("parent")
	val parent: Parent2? = null,

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

data class Parent2(

	@field:SerializedName("parent")
	val parent: Any? = null,

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

data class Kabupaten(

	@field:SerializedName("provinsi")
	val provinsi: Provinsi? = null,

	@field:SerializedName("provinsiId")
	val provinsiId: Int? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class OTPRequest(
	@field:SerializedName("nrp") val nrp: String? = null,
	@field:SerializedName("otp") val otp: String? = null
)