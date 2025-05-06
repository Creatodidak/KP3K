package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

sealed class LahanItem {
	data class Monokultur(val data: LahanmonokulturItem) : LahanItem()
	data class Tumpangsari(val data: LahantumpangsariItem) : LahanItem()
}

data class MMyLahan(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("riwayat")
	val riwayat: String? = null,

	@field:SerializedName("lahantumpangsari")
	val lahantumpangsari: List<LahantumpangsariItem?>? = null,

	@field:SerializedName("perubahan")
	val perubahan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("lahanmonokultur")
	val lahanmonokultur: List<LahanmonokulturItem?>? = null,

	@field:SerializedName("petugas")
	val petugas: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class LahantumpangsariItem(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("owner_id")
	val ownerId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("luas")
	val luas: String? = null,

	@field:SerializedName("ownertumpangsari")
	val ownertumpangsari: Ownertumpangsari? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("dttumpangsari")
	val dttumpangsari: List<DttumpangsariItem?>? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
)

data class Ownermonokultur(

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
	val id: Int? = null
)

data class LahanmonokulturItem(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("owner_id")
	val ownerId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("dtmonokultur")
	val dtmonokultur: List<DtMonokultur?>? = null,

	@field:SerializedName("luas")
	val luas: String? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("ownermonokultur")
	val ownermonokultur: Ownermonokultur? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
)

data class Ownertumpangsari(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("telepon")
	val telepon: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("gapki")
	val gapki: String? = null,

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
	val id: Int? = null
)

data class DttumpangsariItem(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("prediksipanen")
	val prediksipanen: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("kodelahan")
	val kodelahan: String? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DtMonokultur(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("prediksipanen")
	val prediksipanen: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("kodelahan")
	val kodelahan: String? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)
