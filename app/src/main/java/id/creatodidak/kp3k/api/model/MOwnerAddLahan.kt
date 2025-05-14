package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MOwnerAddLahan(

	@field:SerializedName("owner")
	val owner: List<OwnerItem?>? = null,

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("riwayat")
	val riwayat: String? = null,

	@field:SerializedName("perubahan")
	val perubahan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("petugas")
	val petugas: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class OwnerItem(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("telepon")
	val telepon: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("nama_pok")
	val namaPok: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class newLahan(
	val owner_id : String,
	val type: String,
	val luas : String,
	val latitude : String,
	val longitude : String,
)