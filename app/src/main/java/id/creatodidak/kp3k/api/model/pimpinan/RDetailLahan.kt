package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class RDetailLahan(

	@field:SerializedName("owner")
	val owner: Owner? = null,

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("desa")
	val desa: String? = null,

	@field:SerializedName("owner_id")
	val ownerId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("realisasitanam")
	val realisasitanam: List<Any?>? = null,

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

	@field:SerializedName("bpkp")
	val bpkp: Bpkp? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Pers(

	@field:SerializedName("polsek")
	val polsek: String? = null,

	@field:SerializedName("sockettoken")
	val sockettoken: Sockettoken? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("jabatan")
	val jabatan: String? = null,

	@field:SerializedName("pangkat")
	val pangkat: String? = null,

	@field:SerializedName("nrp")
	val nrp: String? = null,

	@field:SerializedName("passwordiv")
	val passwordiv: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("polda")
	val polda: String? = null,

	@field:SerializedName("foto")
	val foto: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("nohp")
	val nohp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("myPolres")
	val myPolres: MyPolres? = null,

	@field:SerializedName("polres")
	val polres: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class MyPolres(

	@field:SerializedName("provinsi")
	val provinsi: String? = null,

	@field:SerializedName("polsek")
	val polsek: List<PolsekItem?>? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null
)

data class Owner(

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

data class Bpkp(

	@field:SerializedName("desa_id")
	val desaId: String? = null,

	@field:SerializedName("pers")
	val pers: Pers? = null,

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

data class PolsekItem(

	@field:SerializedName("provinsi")
	val provinsi: String? = null,

	@field:SerializedName("provinsi_id")
	val provinsiId: String? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("polres_id")
	val polresId: String? = null,

	@field:SerializedName("kabupaten_id")
	val kabupatenId: String? = null,

	@field:SerializedName("kode")
	val kode: String? = null,

	@field:SerializedName("kecamatan")
	val kecamatan: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("kabupaten")
	val kabupaten: String? = null,

	@field:SerializedName("kecamatan_id")
	val kecamatanId: String? = null,

	@field:SerializedName("polres")
	val polres: String? = null
)
