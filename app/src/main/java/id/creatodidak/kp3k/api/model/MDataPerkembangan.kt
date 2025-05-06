package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MDataPerkembangan(

	@field:SerializedName("MDataPerkembangan")
	val mDataPerkembangan: List<MDataPerkembanganItem?>
)

data class MDataPerkembanganItem(

	@field:SerializedName("foto4")
	val foto4: String,

	@field:SerializedName("keterangan")
	val keterangan: String,

	@field:SerializedName("keteranganhama")
	val keteranganhama: String,

	@field:SerializedName("tinggitanaman")
	val tinggitanaman: String,

	@field:SerializedName("warnadaun")
	val warnadaun: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("kondisitanah")
	val kondisitanah: String,

	@field:SerializedName("curahhujan")
	val curahhujan: String,

	@field:SerializedName("foto1")
	val foto1: String,

	@field:SerializedName("create_at")
	val createAt: String,

	@field:SerializedName("tanaman_id")
	val tanamanId: Int,

	@field:SerializedName("hama")
	val hama: String,

	@field:SerializedName("foto3")
	val foto3: String,

	@field:SerializedName("foto2")
	val foto2: String
)
