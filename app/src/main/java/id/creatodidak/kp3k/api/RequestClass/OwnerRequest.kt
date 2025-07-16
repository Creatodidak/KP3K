package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class OwnerAddRequest(
    @SerializedName("type")
    val type: String,

    @SerializedName("gapki")
    val gapki: String,

    @SerializedName("nama_pok")
    val namaPok: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("nik")
    val nik: String,

    @SerializedName("alamat")
    val alamat: String,

    @SerializedName("telepon")
    val telepon: String,

    @SerializedName("provinsi_id")
    val provinsiId: String,

    @SerializedName("kabupaten_id")
    val kabupatenId: String,

    @SerializedName("kecamatan_id")
    val kecamatanId: String,

    @SerializedName("desa_id")
    val desaId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("alasan")
    val alasan: String? = null,

    @SerializedName("komoditas")
    val komoditas: String,

    @SerializedName("submitter")
    val submitter: String,

    @SerializedName("role")
    val role: String
)

data class OwnerPatchRequest(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("gapki")
    val gapki: String? = null,

    @SerializedName("nama_pok")
    val namaPok: String? = null,

    @SerializedName("nama")
    val nama: String? = null,

    @SerializedName("nik")
    val nik: String? = null,

    @SerializedName("alamat")
    val alamat: String? = null,

    @SerializedName("telepon")
    val telepon: String? = null,

    @SerializedName("provinsi_id")
    val provinsiId: String? = null,

    @SerializedName("kabupaten_id")
    val kabupatenId: String? = null,

    @SerializedName("kecamatan_id")
    val kecamatanId: String? = null,

    @SerializedName("desa_id")
    val desaId: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("alasan")
    val alasan: String? = null,

    @SerializedName("komoditas")
    val komoditas: String,

    @SerializedName("submitter")
    val submitter: String,

    @SerializedName("role")
    val role: String
)