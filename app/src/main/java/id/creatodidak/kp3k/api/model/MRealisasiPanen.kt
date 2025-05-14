package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MRealisasiPanen(

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("keterangan")
	val keterangan: String? = null,

	@field:SerializedName("jumlahpanen")
	val jumlahpanen: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("tanaman_id")
	val tanamanId: Int? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null
)
