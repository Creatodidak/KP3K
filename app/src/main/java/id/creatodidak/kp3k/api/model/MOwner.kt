package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MOwner(

	@field:SerializedName("MOwner")
	val mOwner: List<MOwnerItem?>? = null
)

data class MOwnerItem(
	@field:SerializedName("desa_id") val desaId: String? = null,
	@field:SerializedName("desa") val desa: String? = null,
	@field:SerializedName("total_luas") val totalLuas: Int? = null,
	@field:SerializedName("telepon") val telepon: String? = null,
	@field:SerializedName("type") val type: String? = null,
	@field:SerializedName("kabupaten") val kabupaten: String? = null,
	@field:SerializedName("kecamatan_id") val kecamatanId: String? = null,
	@field:SerializedName("gapki") val gapki: String? = null,
	@field:SerializedName("alamat") val alamat: String? = null,
	@field:SerializedName("nik") val nik: String? = null,
	@field:SerializedName("nama") val nama: String? = null,
	@field:SerializedName("nama_pok") val namaPok: String? = null,
	@field:SerializedName("kode") val kode: String? = null,
	@field:SerializedName("kabupaten_id") val kabupatenId: String? = null,
	@field:SerializedName("kecamatan") val kecamatan: String? = null,
	@field:SerializedName("id") val id: Int? = null,
	@field:SerializedName("status") val status: String? = null
)

data class AddOwner(
	@SerializedName("gapki") val gapki: String,
	@SerializedName("type") val type: String,
	@SerializedName("nama_pok") val nama_pok: String,
	@SerializedName("nama") val nama: String,
	@SerializedName("nik") val nik: String,
	@SerializedName("alamat") val alamat: String,
	@SerializedName("telepon") val telepon: String,
	@SerializedName("desa_id") val desa_id: String
)