package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class KabupatenSummaryMonthly(
	@field:SerializedName("nama")
	val nama: String,

	@field:SerializedName("towner")
	val towner: Double,

	@field:SerializedName("tlahan")
	val tlahan: Double,

	@field:SerializedName("tluaslahan")
	val tluaslahan: Double,

	@field:SerializedName("tproduksi")
	val tproduksi: Double,

	@field:SerializedName("data")
	val data: Map<String, BulanSummary>
)

data class BulanSummary(
	@field:SerializedName("totaltanam")
	val totaltanam: Double,

	@field:SerializedName("totaltargetpanen")
	val totaltargetpanen: Double,

	@field:SerializedName("totalpanen")
	val totalpanen: Double
)
