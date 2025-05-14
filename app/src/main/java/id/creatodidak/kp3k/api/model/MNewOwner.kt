package id.creatodidak.kp3k.api.model

import com.google.gson.annotations.SerializedName

data class MNewOwner(

	@field:SerializedName("MNewOwner")
	val mNewOwner: List<MNewOwnerItem?>? = null
)

data class RealisasitanamItems(

	@field:SerializedName("luastanam")
	val luastanam: String? = null,

	@field:SerializedName("foto4")
	val foto4: String? = null,

	@field:SerializedName("varietas")
	val varietas: String? = null,

	@field:SerializedName("tanggaltanam")
	val tanggaltanam: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

	@field:SerializedName("masatanam")
	val masatanam: String? = null,

	@field:SerializedName("prediksipanen")
	val prediksipanen: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("komoditas")
	val komoditas: String? = null,

	@field:SerializedName("foto1")
	val foto1: String? = null,

	@field:SerializedName("create_at")
	val createAt: String? = null,

	@field:SerializedName("kodelahan")
	val kodelahan: String? = null,

	@field:SerializedName("foto3")
	val foto3: String? = null,

	@field:SerializedName("foto2")
	val foto2: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class MNewOwnerItem(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("telepon")
	val telepon: String? = null,

	@field:SerializedName("lahanfix")
	val lahanfix: List<LahanfixItems?>? = null,

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

	@field:SerializedName("alasan")
	val alasan: String? = null,

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

data class LahanfixItems(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("owner_id")
	val ownerId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("realisasitanam")
	val realisasitanam: List<RealisasitanamItems?>? = null,

	@field:SerializedName("luas")
	val luas: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("alasan")
	val alasan: String? = null,

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

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
