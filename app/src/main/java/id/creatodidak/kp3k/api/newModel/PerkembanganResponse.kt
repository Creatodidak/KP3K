package id.creatodidak.kp3k.api.newModel

import com.google.gson.annotations.SerializedName

data class PerkembanganResponse(

	@field:SerializedName("PerkembanganResponse")
	val perkembanganResponse: List<PerkembanganResponseItem?>? = null
)

data class PerkembanganResponseItem(

	@field:SerializedName("pupuk")
	val pupuk: String? = null,

	@field:SerializedName("submitter")
	val submitter: String? = null,

	@field:SerializedName("keterangan")
	val keterangan: String? = null,

	@field:SerializedName("kondisiair")
	val kondisiair: String? = null,

	@field:SerializedName("keteranganhama")
	val keteranganhama: String? = null,

	@field:SerializedName("pestisida")
	val pestisida: String? = null,

	@field:SerializedName("gangguanalam")
	val gangguanalam: String? = null,

	@field:SerializedName("updateAt")
	val updateAt: String? = null,

	@field:SerializedName("createAt")
	val createAt: String,

	@field:SerializedName("keterangangangguanalam")
	val keterangangangguanalam: String? = null,

	@field:SerializedName("warnadaun")
	val warnadaun: String? = null,

	@field:SerializedName("gangguanlainnya")
	val gangguanlainnya: String? = null,

	@field:SerializedName("rekomendasi")
	val rekomendasi: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("keterangangangguanlainnya")
	val keterangangangguanlainnya: String? = null,

	@field:SerializedName("tinggitanaman")
	val tinggitanaman: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("ph")
	val ph: String? = null,

	@field:SerializedName("kondisitanah")
	val kondisitanah: String? = null,

	@field:SerializedName("curahhujan")
	val curahhujan: String? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("tanaman_id")
	val tanamanId: Int? = null,

	@field:SerializedName("hama")
	val hama: String? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
