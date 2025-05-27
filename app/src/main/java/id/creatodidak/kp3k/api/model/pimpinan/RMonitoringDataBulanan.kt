package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class KabupatenSummaryMonthly(
	@field:SerializedName("nama")
	val nama: String,

	@field:SerializedName("towner")
	val towner: Int,

	@field:SerializedName("tlahan")
	val tlahan: Int,

	@field:SerializedName("tluaslahan")
	val tluaslahan: Long,

	@field:SerializedName("tproduksi")
	val tproduksi: Int,

	@field:SerializedName("data")
	val data: Map<String, BulanSummary>
)

data class BulanSummary(
	@field:SerializedName("totaltanam")
	val totaltanam: Int,

	@field:SerializedName("totaltargetpanen")
	val totaltargetpanen: Int,

	@field:SerializedName("totalpanen")
	val totalpanen: Int
)
