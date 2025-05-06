package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MBasicData(

	@field:SerializedName("totalKalbar")
	val totalKalbar: TotalKalbar,

	@field:SerializedName("targetdancapaian")
	val targetdancapaian: List<TargetdancapaianItem>,

	@field:SerializedName("jumlahpetani")
	val jumlahpetani: Int,

	@field:SerializedName("jumlahbpkp")
	val jumlahbpkp: Int
)

data class TargetdancapaianItem(

	@field:SerializedName("tumpangsari")
	val tumpangsari: Tumpangsari,

	@field:SerializedName("monokultur")
	val monokultur: Monokultur,

	@field:SerializedName("namaKab")
	val namaKab: String
)

data class TotalKalbar(

	@field:SerializedName("tumpangsari")
	val tumpangsari: Tumpangsari,

	@field:SerializedName("monokultur")
	val monokultur: Monokultur
)

data class Monokultur(

	@field:SerializedName("luaslahan")
	val luaslahan: Int,

	@field:SerializedName("totaltargetpanen")
	val totaltargetpanen: Int,

	@field:SerializedName("totalpanen")
	val totalpanen: Int,

	@field:SerializedName("totaltanam")
	val totaltanam: Int
)

data class Tumpangsari(

	@field:SerializedName("luaslahan")
	val luaslahan: Int,

	@field:SerializedName("totaltargetpanen")
	val totaltargetpanen: Any,

	@field:SerializedName("totalpanen")
	val totalpanen: Int,

	@field:SerializedName("totaltanam")
	val totaltanam: Int
)
