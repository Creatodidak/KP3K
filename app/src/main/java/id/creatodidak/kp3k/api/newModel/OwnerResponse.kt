package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class OwnerResponse(
	@field:SerializedName("OwnerResponse")
	val ownerResponse: List<OwnerResponseItem?>? = null
)

data class OwnerResponseItem(

	@field:SerializedName("desa_id")
	val desaId: Int? = null,

	@field:SerializedName("telepon")
	val telepon: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("gapki")
	val gapki: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: Int? = null,

	@field:SerializedName("createAt")
	val createAt: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("nik")
	val nik: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: Int? = null,

	@field:SerializedName("nama_pok")
	val namaPok: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("submitter")
	val submitter: String? = null
)
